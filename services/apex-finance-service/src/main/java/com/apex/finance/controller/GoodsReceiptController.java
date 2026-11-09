package com.apex.finance.controller;

import com.apex.common.exception.EntityNotFoundException;
import com.apex.finance.domain.GoodsReceipt;
import com.apex.finance.domain.GRLineItem;
import com.apex.finance.repository.GoodsReceiptRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/finance/goods-receipts")
public class GoodsReceiptController {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final AtomicLong grSequence = new AtomicLong(1);

    public GoodsReceiptController(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    public record GoodsReceiptCreateRequest(
        @NotBlank(message = "Purchase order ID is mandatory")
        String purchaseOrderId,
        @NotBlank(message = "Received by staff ID is mandatory")
        String receivedBy,
        @NotEmpty(message = "At least one item must be received")
        @Valid
        List<ItemReceiptRequest> items
    ) {
        public record ItemReceiptRequest(
            @NotBlank(message = "PO line item ID is mandatory")
            String poLineItemId,
            @Min(value = 0, message = "Received quantity cannot be negative")
            int receivedQuantity
        ) {}
    }

    @PostMapping
    public ResponseEntity<GoodsReceipt> recordGoodsReceipt(@Valid @RequestBody GoodsReceiptCreateRequest request) {
        String grId = UUID.randomUUID().toString();
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String grNumber = String.format("GR-%s-%04d", datePrefix, grSequence.getAndIncrement());

        GoodsReceipt gr = new GoodsReceipt(grId, grNumber, request.purchaseOrderId(), request.receivedBy());

        for (GoodsReceiptCreateRequest.ItemReceiptRequest itemReq : request.items()) {
            gr.addLineItem(new GRLineItem(itemReq.poLineItemId(), itemReq.receivedQuantity()));
        }

        GoodsReceipt saved = goodsReceiptRepository.save(gr);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoodsReceipt> getGoodsReceipt(@PathVariable String id) {
        return goodsReceiptRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("GoodsReceipt", id));
    }
}

