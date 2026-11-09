package com.apex.finance.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "vi_line_items")
public class VILineItem {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_invoice_id", nullable = false)
    @JsonBackReference
    private VendorInvoice vendorInvoice;

    @Column(name = "po_line_item_id", length = 36, nullable = false)
    private String poLineItemId;

    @Column(name = "invoiced_quantity", nullable = false)
    private int invoicedQuantity;

    @Column(name = "unit_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "subtotal", precision = 19, scale = 4, nullable = false)
    private BigDecimal subtotal;

    public VILineItem() {}

    public VILineItem(String poLineItemId, int invoicedQuantity, BigDecimal unitPrice) {
        this.id = UUID.randomUUID().toString();
        this.poLineItemId = poLineItemId;
        this.invoicedQuantity = invoicedQuantity;
        this.unitPrice = unitPrice.setScale(4, RoundingMode.HALF_EVEN);
        this.subtotal = this.unitPrice.multiply(BigDecimal.valueOf(invoicedQuantity)).setScale(4, RoundingMode.HALF_EVEN);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public VendorInvoice getVendorInvoice() { return vendorInvoice; }
    public void setVendorInvoice(VendorInvoice vendorInvoice) { this.vendorInvoice = vendorInvoice; }
    public String getPoLineItemId() { return poLineItemId; }
    public void setPoLineItemId(String poLineItemId) { this.poLineItemId = poLineItemId; }
    public int getInvoicedQuantity() { return invoicedQuantity; }
    public void setInvoicedQuantity(int invoicedQuantity) { this.invoicedQuantity = invoicedQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}

