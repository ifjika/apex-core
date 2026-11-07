package com.apex.procurement.domain;

import com.apex.common.exception.BusinessException;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Requisition Aggregate Root managing procurement ingestion lifecycle.
 */
@Entity
@Table(name = "purchase_requisitions", schema = "procurement")
public class PurchaseRequisition {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "pr_number", length = 64, nullable = false, unique = true)
    private String prNumber;

    @Column(name = "cost_center_id", length = 36, nullable = false)
    private String costCenterId;

    @Column(name = "requester_id", length = 36, nullable = false)
    private String requesterId;

    @Column(name = "current_approver_id", length = 36)
    private String currentApproverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private RequisitionStatus status = RequisitionStatus.DRAFT;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "IDR";

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "requisition", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<PRLineItem> lineItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public PurchaseRequisition() {}

    public PurchaseRequisition(String id, String prNumber, String costCenterId, String requesterId, String currency) {
        this.id = id;
        this.prNumber = prNumber;
        this.costCenterId = costCenterId;
        this.requesterId = requesterId;
        this.currency = currency != null ? currency.toUpperCase() : "IDR";
        this.status = RequisitionStatus.DRAFT;
        this.totalAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        this.version = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addLineItem(PRLineItem item) {
        item.setRequisition(this);
        this.lineItems.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        this.totalAmount = this.lineItems.stream()
            .map(PRLineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_EVEN);
        this.updatedAt = Instant.now();
    }

    public void submitForApproval() {
        if (this.status != RequisitionStatus.DRAFT) {
            throw new BusinessException("Only DRAFT requisitions can be submitted for review.", "INVALID_STATE_TRANSITION");
        }
        if (this.lineItems.isEmpty()) {
            throw new BusinessException("Cannot submit a requisition without line items.", "EMPTY_LINE_ITEMS");
        }
        this.status = RequisitionStatus.PENDING_APPROVAL;
        this.updatedAt = Instant.now();
    }

    public void approve(String approverId) {
        if (this.status != RequisitionStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only PENDING_APPROVAL requisitions can be approved.", "INVALID_STATE_TRANSITION");
        }
        this.currentApproverId = approverId;
        this.status = RequisitionStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    public void reject(String approverId, String reason) {
        if (this.status != RequisitionStatus.PENDING_APPROVAL) {
            throw new BusinessException("Only PENDING_APPROVAL requisitions can be rejected.", "INVALID_STATE_TRANSITION");
        }
        if (reason == null || reason.trim().length() < 5) {
            throw new BusinessException("Rejection reason must be specified (at least 5 characters).", "MANDATORY_REJECTION_REASON");
        }
        this.currentApproverId = approverId;
        this.rejectionReason = reason;
        this.status = RequisitionStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    public void markAsOrdered() {
        if (this.status != RequisitionStatus.APPROVED) {
            throw new BusinessException("Only APPROVED requisitions can be converted into orders.", "INVALID_STATE_TRANSITION");
        }
        this.status = RequisitionStatus.ORDERED;
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPrNumber() { return prNumber; }
    public void setPrNumber(String prNumber) { this.prNumber = prNumber; }
    public String getCostCenterId() { return costCenterId; }
    public void setCostCenterId(String costCenterId) { this.costCenterId = costCenterId; }
    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }
    public String getCurrentApproverId() { return currentApproverId; }
    public void setCurrentApproverId(String currentApproverId) { this.currentApproverId = currentApproverId; }
    public RequisitionStatus getStatus() { return status; }
    public void setStatus(RequisitionStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public List<PRLineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<PRLineItem> lineItems) { this.lineItems = lineItems; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

