package com.apex.procurement.service;

import com.apex.common.domain.EventEnvelope;
import com.apex.common.exception.BusinessException;
import com.apex.common.exception.EntityNotFoundException;
import com.apex.common.outbox.OutboxEvent;
import com.apex.common.outbox.OutboxEventRepository;
import com.apex.procurement.domain.*;
import com.apex.procurement.repository.PurchaseOrderRepository;
import com.apex.procurement.repository.PurchaseRequisitionRepository;
import com.apex.procurement.repository.VendorRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service managing contractual Purchase Order generation and vendor issuance events.
 */
@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository orderRepository;
    private final PurchaseRequisitionRepository requisitionRepository;
    private final VendorRepository vendorRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final AtomicLong poSequence = new AtomicLong(1);

    public PurchaseOrderService(
        PurchaseOrderRepository orderRepository,
        PurchaseRequisitionRepository requisitionRepository,
        VendorRepository vendorRepository,
        OutboxEventRepository outboxEventRepository,
        ObjectMapper objectMapper
    ) {
        this.orderRepository = orderRepository;
        this.requisitionRepository = requisitionRepository;
        this.vendorRepository = vendorRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a fully approved Purchase Requisition into a legally binding Purchase Order.
     */
    @Transactional
    public PurchaseOrder convertRequisitionToOrder(String requisitionId, String vendorId) {
        PurchaseRequisition requisition = requisitionRepository.findById(requisitionId)
            .orElseThrow(() -> new EntityNotFoundException("PurchaseRequisition", requisitionId));

        if (requisition.getStatus() != RequisitionStatus.APPROVED) {
            throw new BusinessException(
                "Cannot issue purchase order from requisition with status: " + requisition.getStatus(),
                "INVALID_REQUISITION_STATUS"
            );
        }

        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new EntityNotFoundException("Vendor", vendorId));

        if (!vendor.isActive()) {
            throw new BusinessException("Vendor is inactive: " + vendor.getName(), "INACTIVE_VENDOR");
        }

        String poId = UUID.randomUUID().toString();
        String poNumber = generatePoNumber();

        PurchaseOrder order = new PurchaseOrder(
            poId,
            poNumber,
            requisition.getId(),
            vendor.getId(),
            requisition.getCurrency()
        );

        for (PRLineItem prItem : requisition.getLineItems()) {
            POLineItem poItem = new POLineItem(
                prItem.getId(),
                prItem.getItemName(),
                prItem.getQuantity(),
                prItem.getUnitPrice()
            );
            order.addLineItem(poItem);
        }

        requisition.markAsOrdered();
        requisitionRepository.save(requisition);
        PurchaseOrder savedOrder = orderRepository.save(order);

        // Record PO_ISSUED event in Outbox for Finance service budget accrual commitment
        saveOutboxEvent(
            "PurchaseOrder",
            savedOrder.getId(),
            "com.apex.procurement.po.issued.v1",
            savedOrder
        );

        return savedOrder;
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrderById(String orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("PurchaseOrder", orderId));
    }

    private void saveOutboxEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            EventEnvelope<Object> envelope = EventEnvelope.create(
                "apex.procurement.service",
                eventType,
                payload
            );
            String jsonPayload = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = new OutboxEvent(aggregateType, aggregateId, eventType, jsonPayload);
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox event payload", e);
        }
    }

    private String generatePoNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        long seq = poSequence.getAndIncrement();
        return String.format("PO-%s-%04d", datePrefix, seq);
    }
}

