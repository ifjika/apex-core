package com.apex.finance.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendor_invoices")
public class VendorInvoice {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "invoice_number", length = 64, nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "vendor_id", length = 36, nullable = false)
    private String vendorId;

    @Column(name = "purchase_order_id", length = 36, nullable = false)
    private String purchaseOrderId;

    @Column(name = "goods_receipt_id", length = 36, nullable = false)
    private String goodsReceiptId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private InvoiceStatus status = InvoiceStatus.PENDING_MATCH;

    @Column(name = "tax_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "vendorInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<VILineItem> lineItems = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public VendorInvoice() {}

    public VendorInvoice(String id, String invoiceNumber, String vendorId, String purchaseOrderId, String goodsReceiptId, BigDecimal taxAmount) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.vendorId = vendorId;
        this.purchaseOrderId = purchaseOrderId;
        this.goodsReceiptId = goodsReceiptId;
        this.taxAmount = (taxAmount != null ? taxAmount : BigDecimal.ZERO).setScale(4, RoundingMode.HALF_EVEN);
        this.totalAmount = this.taxAmount;
        this.status = InvoiceStatus.PENDING_MATCH;
        this.createdAt = Instant.now();
    }

    public void addLineItem(VILineItem item) {
        item.setVendorInvoice(this);
        this.lineItems.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        BigDecimal itemsSum = this.lineItems.stream()
            .map(VILineItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = itemsSum.add(this.taxAmount).setScale(4, RoundingMode.HALF_EVEN);
    }

    public void markAsMatched(boolean withTolerance) {
        this.status = withTolerance ? InvoiceStatus.MATCHED_WITH_TOLERANCE : InvoiceStatus.MATCHED;
    }

    public void markAsDiscrepancy() {
        this.status = InvoiceStatus.DISCREPANCY;
    }

    public void markAsPaid() {
        this.status = InvoiceStatus.PAID;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getGoodsReceiptId() { return goodsReceiptId; }
    public void setGoodsReceiptId(String goodsReceiptId) { this.goodsReceiptId = goodsReceiptId; }
    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public List<VILineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<VILineItem> lineItems) { this.lineItems = lineItems; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

