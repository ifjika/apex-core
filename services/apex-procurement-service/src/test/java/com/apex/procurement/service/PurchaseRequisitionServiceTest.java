package com.apex.procurement.service;

import com.apex.common.exception.BusinessException;
import com.apex.common.outbox.OutboxEvent;
import com.apex.common.outbox.OutboxEventRepository;
import com.apex.procurement.domain.PurchaseRequisition;
import com.apex.procurement.domain.RequisitionStatus;
import com.apex.procurement.dto.RequisitionCreateRequest;
import com.apex.procurement.repository.PurchaseRequisitionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseRequisitionServiceTest {

    @Mock
    private PurchaseRequisitionRepository requisitionRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PurchaseRequisitionService requisitionService;

    @Test
    @DisplayName("Should create, submit, and record outbox event for valid requisition")
    void shouldCreateAndSubmitRequisition() {
        when(requisitionRepository.save(any(PurchaseRequisition.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        RequisitionCreateRequest request = new RequisitionCreateRequest(
            "cc-eng-001",
            "emp-stf-001",
            "IDR",
            List.of(
                new RequisitionCreateRequest.LineItemRequest("Server Rack 42U", "Enterprise Cabinet", 2, new BigDecimal("15000000.00")),
                new RequisitionCreateRequest.LineItemRequest("PDU 16A", "Rackmount PDU", 4, new BigDecimal("2500000.00"))
            )
        );

        PurchaseRequisition result = requisitionService.createAndSubmitRequisition(request);

        assertThat(result.getStatus()).isEqualTo(RequisitionStatus.PENDING_APPROVAL);
        // Total: (2 * 15,000,000) + (4 * 2,500,000) = 40,000,000 IDR
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("40000000.0000"));

        verify(requisitionRepository, times(1)).save(any(PurchaseRequisition.class));
        verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Should approve pending requisition and advance status")
    void shouldApproveRequisition() {
        PurchaseRequisition pr = new PurchaseRequisition("pr-1", "PR-2026-0001", "cc-1", "emp-1", "IDR");
        pr.submitForApproval(); // moves from DRAFT to PENDING_APPROVAL

        when(requisitionRepository.findById("pr-1")).thenReturn(Optional.of(pr));
        when(requisitionRepository.save(any(PurchaseRequisition.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseRequisition approved = requisitionService.approveRequisition("pr-1", "emp-approver-99");

        assertThat(approved.getStatus()).isEqualTo(RequisitionStatus.APPROVED);
        assertThat(approved.getCurrentApproverId()).isEqualTo("emp-approver-99");

        verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Should reject requisition with mandatory reason and emit rejection event")
    void shouldRejectRequisitionWithReason() {
        PurchaseRequisition pr = new PurchaseRequisition("pr-2", "PR-2026-0002", "cc-1", "emp-1", "IDR");
        pr.submitForApproval();

        when(requisitionRepository.findById("pr-2")).thenReturn(Optional.of(pr));
        when(requisitionRepository.save(any(PurchaseRequisition.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseRequisition rejected = requisitionService.rejectRequisition("pr-2", "emp-approver-99", "Exceeds annual quarterly department allocation limit.");

        assertThat(rejected.getStatus()).isEqualTo(RequisitionStatus.REJECTED);
        assertThat(rejected.getRejectionReason()).contains("Exceeds annual quarterly");

        verify(outboxEventRepository, times(1)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("Should fail rejection when reason is absent or too short")
    void shouldFailRejectionWithoutReason() {
        PurchaseRequisition pr = new PurchaseRequisition("pr-3", "PR-2026-0003", "cc-1", "emp-1", "IDR");
        pr.submitForApproval();

        when(requisitionRepository.findById("pr-3")).thenReturn(Optional.of(pr));

        assertThatThrownBy(() -> requisitionService.rejectRequisition("pr-3", "emp-approver-99", "No"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Rejection reason must be specified");
    }
}

