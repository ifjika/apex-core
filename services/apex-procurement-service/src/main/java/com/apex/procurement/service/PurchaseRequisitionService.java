package com.apex.procurement.service;

import com.apex.common.domain.EventEnvelope;
import com.apex.common.exception.EntityNotFoundException;
import com.apex.common.outbox.OutboxEvent;
import com.apex.common.outbox.OutboxEventRepository;
import com.apex.procurement.domain.PRLineItem;
import com.apex.procurement.domain.PurchaseRequisition;
import com.apex.procurement.dto.RequisitionCreateRequest;
import com.apex.procurement.repository.PurchaseRequisitionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service managing Purchase Requisition business lifecycles and transactional outbox event creation.
 */
@Service
public class PurchaseRequisitionService {

    private final PurchaseRequisitionRepository requisitionRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final AtomicLong sequenceCounter = new AtomicLong(1);

    public PurchaseRequisitionService(
        PurchaseRequisitionRepository requisitionRepository,
        OutboxEventRepository outboxEventRepository,
        ObjectMapper objectMapper
    ) {
        this.requisitionRepository = requisitionRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Creates and immediately submits a new Purchase Requisition, saving both
     * the domain entity and the outbox event in a single atomic database transaction.
     */
    @Transactional
    public PurchaseRequisition createAndSubmitRequisition(RequisitionCreateRequest request) {
        String id = UUID.randomUUID().toString();
        String prNumber = generatePrNumber();

        PurchaseRequisition requisition = new PurchaseRequisition(
            id,
            prNumber,
            request.costCenterId(),
            request.requesterId(),
            request.currency()
        );

        for (RequisitionCreateRequest.LineItemRequest itemReq : request.lineItems()) {
            PRLineItem lineItem = new PRLineItem(
                itemReq.itemName(),
                itemReq.description(),
                itemReq.quantity(),
                itemReq.unitPrice()
            );
            requisition.addLineItem(lineItem);
        }

        requisition.submitForApproval();
        PurchaseRequisition saved = requisitionRepository.save(requisition);

        // Record outbox event for asynchronous HRIS approval route calculation & budget reservation
        saveOutboxEvent(
            "PurchaseRequisition",
            saved.getId(),
            "com.apex.procurement.pr.submitted.v1",
            saved
        );

        return saved;
    }

    @Transactional
    public PurchaseRequisition approveRequisition(String requisitionId, String approverId) {
        PurchaseRequisition requisition = requisitionRepository.findById(requisitionId)
            .orElseThrow(() -> new EntityNotFoundException("PurchaseRequisition", requisitionId));

        requisition.approve(approverId);
        PurchaseRequisition saved = requisitionRepository.save(requisition);

        saveOutboxEvent(
            "PurchaseRequisition",
            saved.getId(),
            "com.apex.procurement.pr.approved.v1",
            saved
        );

        return saved;
    }

    @Transactional
    public PurchaseRequisition rejectRequisition(String requisitionId, String approverId, String reason) {
        PurchaseRequisition requisition = requisitionRepository.findById(requisitionId)
            .orElseThrow(() -> new EntityNotFoundException("PurchaseRequisition", requisitionId));

        requisition.reject(approverId, reason);
        PurchaseRequisition saved = requisitionRepository.save(requisition);

        saveOutboxEvent(
            "PurchaseRequisition",
            saved.getId(),
            "com.apex.procurement.pr.rejected.v1",
            saved
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public PurchaseRequisition getRequisitionById(String requisitionId) {
        return requisitionRepository.findById(requisitionId)
            .orElseThrow(() -> new EntityNotFoundException("PurchaseRequisition", requisitionId));
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

    private String generatePrNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        long seq = sequenceCounter.getAndIncrement();
        return String.format("PR-%s-%04d", datePrefix, seq);
    }
}

