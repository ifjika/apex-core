package com.apex.procurement.controller;

import com.apex.common.idempotency.Idempotent;
import com.apex.procurement.domain.PurchaseRequisition;
import com.apex.procurement.dto.RequisitionCreateRequest;
import com.apex.procurement.dto.RequisitionResponse;
import com.apex.procurement.service.PurchaseRequisitionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/procurement/requisitions")
public class PurchaseRequisitionController {

    private final PurchaseRequisitionService requisitionService;

    public PurchaseRequisitionController(PurchaseRequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    @PostMapping
    @Idempotent
    public ResponseEntity<RequisitionResponse> createRequisition(@Valid @RequestBody RequisitionCreateRequest request) {
        PurchaseRequisition requisition = requisitionService.createAndSubmitRequisition(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RequisitionResponse.fromEntity(requisition));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequisitionResponse> getRequisition(@PathVariable String id) {
        PurchaseRequisition requisition = requisitionService.getRequisitionById(id);
        return ResponseEntity.ok(RequisitionResponse.fromEntity(requisition));
    }

    public record ApprovalActionRequest(
        @NotBlank(message = "Approver ID is mandatory")
        String approverId
    ) {}

    public record RejectionActionRequest(
        @NotBlank(message = "Approver ID is mandatory")
        String approverId,
        @NotBlank(message = "Rejection reason is mandatory")
        String reason
    ) {}

    @PostMapping("/{id}/approve")
    public ResponseEntity<RequisitionResponse> approveRequisition(
        @PathVariable String id,
        @Valid @RequestBody ApprovalActionRequest request
    ) {
        PurchaseRequisition approved = requisitionService.approveRequisition(id, request.approverId());
        return ResponseEntity.ok(RequisitionResponse.fromEntity(approved));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RequisitionResponse> rejectRequisition(
        @PathVariable String id,
        @Valid @RequestBody RejectionActionRequest request
    ) {
        PurchaseRequisition rejected = requisitionService.rejectRequisition(id, request.approverId(), request.reason());
        return ResponseEntity.ok(RequisitionResponse.fromEntity(rejected));
    }
}

