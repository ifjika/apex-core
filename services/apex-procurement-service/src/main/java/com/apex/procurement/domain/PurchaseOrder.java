package com.apex.procurement.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Order Aggregate Root representing legally binding contractual commitments.
 */
@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "po_number", length = 64, nullable = false, unique = true)
    private String poNumber;

    @Column(name = "requisition_id", length = 36, nullable = false)
    private String requisitionId;

    @Column(name = "vendor_id", length = 36, nullable = false)
    private String vendorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private OrderStatus status = OrderStatus.ISSUED;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "IDR";

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<POLineItem> lineItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public PurchaseOrder() {}

    public PurchaseOrder(String id, String poNumber, String requisitionId, String vendorId, String currency) {
        this.id = id;
        this.poNumber = poNumber;
        this.requisitionId = requisitionId;
        this.vendorId = vendorId;
        this.currency = currency != null ? currency.toUpperCase() : "IDR";
        this.status = OrderStatus.ISSUED;
        this.totalAmount = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        this.version = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void addLineItem(POLineItem item) {
        item.setPurchaseOrder(this);
        this.lineItems.add(item);
        this.totalAmount = this.lineItems.stream()
            .map(POLineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_EVEN);
    }

    public void acknowledge() {
        this.status = OrderStatus.ACKNOWLEDGED;
        this.updatedAt = Instant.now();
    }

    public void markAsPartiallyFulfilled() {
        this.status = OrderStatus.PARTIALLY_FULFILLED;
        this.updatedAt = Instant.now();
    }

    public void markAsFulfilled() {
        this.status = OrderStatus.FULFILLED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    public String getRequisitionId() { return requisitionId; }
    public void setRequisitionId(String requisitionId) { this.requisitionId = requisitionId; }
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public List<POLineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<POLineItem> lineItems) { this.lineItems = lineItems; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

