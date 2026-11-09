package com.apex.procurement.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "po_line_items")
public class POLineItem {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    @JsonBackReference
    private PurchaseOrder purchaseOrder;

    @Column(name = "pr_line_item_id", length = 36, nullable = false)
    private String prLineItemId;

    @Column(name = "item_name", length = 128, nullable = false)
    private String itemName;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "unit_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    public POLineItem() {}

    public POLineItem(String prLineItemId, String itemName, int quantity, BigDecimal unitPrice) {
        this.id = UUID.randomUUID().toString();
        this.prLineItemId = prLineItemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice.setScale(4, RoundingMode.HALF_EVEN);
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_EVEN);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public PurchaseOrder getPurchaseOrder() { return purchaseOrder; }
    public void setPurchaseOrder(PurchaseOrder purchaseOrder) { this.purchaseOrder = purchaseOrder; }
    public String getPrLineItemId() { return prLineItemId; }
    public void setPrLineItemId(String prLineItemId) { this.prLineItemId = prLineItemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}

