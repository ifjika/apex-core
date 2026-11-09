package com.apex.finance.service;

import com.apex.common.domain.EventEnvelope;
import com.apex.common.exception.DiscrepancyException;
import com.apex.common.exception.EntityNotFoundException;
import com.apex.finance.domain.*;
import com.apex.finance.matching.ThreeWayMatchingEngine;
import com.apex.finance.repository.GoodsReceiptRepository;
import com.apex.finance.repository.PaymentVoucherRepository;
import com.apex.finance.repository.VendorInvoiceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service managing automated 3-Way Matching, Payment Voucher creation, and Ledger posting.
 */
@Service
public class InvoiceProcessingService {

    private final VendorInvoiceRepository invoiceRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PaymentVoucherRepository paymentVoucherRepository;
    private final ThreeWayMatchingEngine matchingEngine;
    private final CryptographicLedgerService ledgerService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String matchingTopic;
    private final AtomicLong voucherSequence = new AtomicLong(1);

    public InvoiceProcessingService(
        VendorInvoiceRepository invoiceRepository,
        GoodsReceiptRepository goodsReceiptRepository,
        PaymentVoucherRepository paymentVoucherRepository,
        ThreeWayMatchingEngine matchingEngine,
        CryptographicLedgerService ledgerService,
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper,
        @Value("${apex.kafka.topics.matching-events:apex.finance.matching}") String matchingTopic
    ) {
        this.invoiceRepository = invoiceRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.paymentVoucherRepository = paymentVoucherRepository;
        this.matchingEngine = matchingEngine;
        this.ledgerService = ledgerService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.matchingTopic = matchingTopic;
    }

    public record InvoiceProcessResult(
        VendorInvoice invoice,
        PaymentVoucher voucher,
        ThreeWayMatchingEngine.MatchResult matchResult
    ) {}

    /**
     * Reconciles an invoice against warehouse goods receipts and issued POs.
     */
    @Transactional
    public InvoiceProcessResult ingestAndMatchInvoice(VendorInvoice invoice, BigDecimal expectedPoAmount) {
        GoodsReceipt goodsReceipt = goodsReceiptRepository.findById(invoice.getGoodsReceiptId())
            .orElseThrow(() -> new EntityNotFoundException("GoodsReceipt", invoice.getGoodsReceiptId()));

        VendorInvoice savedInvoice = invoiceRepository.save(invoice);

        // Execute automated 3-Way Matching Invariant
        ThreeWayMatchingEngine.MatchResult matchResult = matchingEngine.evaluate(goodsReceipt, savedInvoice, expectedPoAmount);

        if (matchResult.hasDiscrepancy()) {
            savedInvoice.markAsDiscrepancy();
            invoiceRepository.save(savedInvoice);
            throw new DiscrepancyException(
                "3-Way Matching discrepancy detected for invoice: " + savedInvoice.getInvoiceNumber(),
                matchResult.discrepancyReasons()
            );
        }

        // Matching succeeded (either exact match or within approved decimal tolerance)
        boolean withTolerance = matchResult.status() == ThreeWayMatchingEngine.MatchResult.MatchStatus.MATCHED_WITH_TOLERANCE;
        savedInvoice.markAsMatched(withTolerance);
        invoiceRepository.save(savedInvoice);

        // Generate formal Payment Voucher
        String voucherId = UUID.randomUUID().toString();
        String voucherNumber = generateVoucherNumber();
        PaymentVoucher voucher = new PaymentVoucher(voucherId, voucherNumber, savedInvoice.getId(), savedInvoice.getTotalAmount());
        PaymentVoucher savedVoucher = paymentVoucherRepository.save(voucher);

        // Post balanced double-entry to append-only cryptographic general ledger
        // DEBIT 5101 (Inventory / Expense), CREDIT 2101 (Accounts Payable)
        ledgerService.appendBalancedJournal(
            savedVoucher.getVoucherNumber(),
            "5101-INVENTORY",
            "2101-ACCOUNTS-PAYABLE",
            savedInvoice.getTotalAmount()
        );

        // Stream match completion event to Kafka
        publishMatchEvent(savedInvoice, savedVoucher, matchResult);

        return new InvoiceProcessResult(savedInvoice, savedVoucher, matchResult);
    }

    private void publishMatchEvent(VendorInvoice invoice, PaymentVoucher voucher, ThreeWayMatchingEngine.MatchResult result) {
        try {
            EventEnvelope<Object> envelope = EventEnvelope.create(
                "apex.finance.service",
                "com.apex.finance.match.completed.v1",
                new MatchCompletedPayload(
                    invoice.getId(),
                    voucher.getId(),
                    voucher.getVoucherNumber(),
                    result.status().name(),
                    result.varianceAmount()
                )
            );
            String payloadJson = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(matchingTopic, invoice.getPurchaseOrderId(), payloadJson);
        } catch (JsonProcessingException ignored) {}
    }

    private String generateVoucherNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        long seq = voucherSequence.getAndIncrement();
        return String.format("PV-%s-%04d", datePrefix, seq);
    }

    public record MatchCompletedPayload(
        String invoiceId,
        String voucherId,
        String voucherNumber,
        String matchStatus,
        BigDecimal varianceAmount
    ) {}
}

