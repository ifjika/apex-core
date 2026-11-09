package com.apex.finance.controller;

import com.apex.common.exception.EntityNotFoundException;
import com.apex.common.idempotency.Idempotent;
import com.apex.finance.domain.VendorInvoice;
import com.apex.finance.domain.VILineItem;
import com.apex.finance.repository.VendorInvoiceRepository;
import com.apex.finance.service.InvoiceProcessingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance/invoices")
public class VendorInvoiceController {

    private final VendorInvoiceRepository invoiceRepository;
    private final InvoiceProcessingService invoiceProcessingService;

    public VendorInvoiceController(
        VendorInvoiceRepository invoiceRepository,
        InvoiceProcessingService invoiceProcessingService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceProcessingService = invoiceProcessingService;
    }

    public record IngestInvoiceRequest(
        @NotBlank(message = "Invoice number is mandatory")
        String invoiceNumber,
        @NotBlank(message = "Vendor ID is mandatory")
        String vendorId,
        @NotBlank(message = "Purchase order ID is mandatory")
        String purchaseOrderId,
        @NotBlank(message = "Goods receipt ID is mandatory")
        String goodsReceiptId,
        BigDecimal taxAmount,
        @DecimalMin(value = "0.01", message = "Expected PO amount must be strictly positive")
        BigDecimal expectedPoTotalAmount,
        @NotEmpty(message = "Line items are mandatory")
        @Valid
        List<LineItemInvoiceRequest> lineItems
    ) {
        public record LineItemInvoiceRequest(
            @NotBlank(message = "PO line item ID is mandatory")
            String poLineItemId,
            @Min(value = 1, message = "Invoiced quantity must be at least 1")
            int invoicedQuantity,
            @DecimalMin(value = "0.0001", message = "Unit price must be strictly positive")
            BigDecimal unitPrice
        ) {}
    }

    @PostMapping
    @Idempotent
    public ResponseEntity<InvoiceProcessingService.InvoiceProcessResult> submitAndMatchInvoice(
        @Valid @RequestBody IngestInvoiceRequest request
    ) {
        String invoiceId = UUID.randomUUID().toString();
        VendorInvoice invoice = new VendorInvoice(
            invoiceId,
            request.invoiceNumber(),
            request.vendorId(),
            request.purchaseOrderId(),
            request.goodsReceiptId(),
            request.taxAmount()
        );

        for (IngestInvoiceRequest.LineItemInvoiceRequest itemReq : request.lineItems()) {
            invoice.addLineItem(new VILineItem(
                itemReq.poLineItemId(),
                itemReq.invoicedQuantity(),
                itemReq.unitPrice()
            ));
        }

        InvoiceProcessingService.InvoiceProcessResult result =
            invoiceProcessingService.ingestAndMatchInvoice(invoice, request.expectedPoTotalAmount());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendorInvoice> getInvoice(@PathVariable String id) {
        return invoiceRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("VendorInvoice", id));
    }
}

