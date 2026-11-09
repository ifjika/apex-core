package com.apex.procurement.dto;

import com.apex.procurement.domain.PurchaseRequisition;
import com.apex.procurement.domain.RequisitionStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record RequisitionResponse(
    String id,
    String prNumber,
    String costCenterId,
    String requesterId,
    String currentApproverId,
    RequisitionStatus status,
    BigDecimal totalAmount,
    String currency,
    String rejectionReason,
    Long version,
    Instant createdAt,
    Instant updatedAt,
    List<LineItemDTO> lineItems
) implements Serializable {

    public record LineItemDTO(
        String id,
        String itemName,
        String description,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
    ) implements Serializable {}

    public static RequisitionResponse fromEntity(PurchaseRequisition entity) {
        List<LineItemDTO> items = entity.getLineItems().stream()
            .map(item -> new LineItemDTO(
                item.getId(),
                item.getItemName(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
            ))
            .toList();

        return new RequisitionResponse(
            entity.getId(),
            entity.getPrNumber(),
            entity.getCostCenterId(),
            entity.getRequesterId(),
            entity.getCurrentApproverId(),
            entity.getStatus(),
            entity.getTotalAmount(),
            entity.getCurrency(),
            entity.getRejectionReason(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            items
        );
    }
}

