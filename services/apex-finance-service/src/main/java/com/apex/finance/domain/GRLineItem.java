package com.apex.finance.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "gr_line_items")
public class GRLineItem {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    @JsonBackReference
    private GoodsReceipt goodsReceipt;

    @Column(name = "po_line_item_id", length = 36, nullable = false)
    private String poLineItemId;

    @Column(name = "received_quantity", nullable = false)
    private int receivedQuantity;

    public GRLineItem() {}

    public GRLineItem(String poLineItemId, int receivedQuantity) {
        this.id = UUID.randomUUID().toString();
        this.poLineItemId = poLineItemId;
        this.receivedQuantity = receivedQuantity;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public GoodsReceipt getGoodsReceipt() { return goodsReceipt; }
    public void setGoodsReceipt(GoodsReceipt goodsReceipt) { this.goodsReceipt = goodsReceipt; }
    public String getPoLineItemId() { return poLineItemId; }
    public void setPoLineItemId(String poLineItemId) { this.poLineItemId = poLineItemId; }
    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }
}

