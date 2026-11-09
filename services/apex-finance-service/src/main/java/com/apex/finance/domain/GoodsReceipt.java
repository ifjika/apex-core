package com.apex.finance.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goods_receipts")
public class GoodsReceipt {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "gr_number", length = 64, nullable = false, unique = true)
    private String grNumber;

    @Column(name = "purchase_order_id", length = 36, nullable = false)
    private String purchaseOrderId;

    @Column(name = "received_by", length = 36, nullable = false)
    private String receivedBy;

    @Column(name = "received_date", nullable = false)
    private Instant receivedDate = Instant.now();

    @OneToMany(mappedBy = "goodsReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<GRLineItem> lineItems = new ArrayList<>();

    public GoodsReceipt() {}

    public GoodsReceipt(String id, String grNumber, String purchaseOrderId, String receivedBy) {
        this.id = id;
        this.grNumber = grNumber;
        this.purchaseOrderId = purchaseOrderId;
        this.receivedBy = receivedBy;
        this.receivedDate = Instant.now();
    }

    public void addLineItem(GRLineItem item) {
        item.setGoodsReceipt(this);
        this.lineItems.add(item);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getGrNumber() { return grNumber; }
    public void setGrNumber(String grNumber) { this.grNumber = grNumber; }
    public String getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(String purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
    public Instant getReceivedDate() { return receivedDate; }
    public void setReceivedDate(Instant receivedDate) { this.receivedDate = receivedDate; }
    public List<GRLineItem> getLineItems() { return lineItems; }
    public void setLineItems(List<GRLineItem> lineItems) { this.lineItems = lineItems; }
}

