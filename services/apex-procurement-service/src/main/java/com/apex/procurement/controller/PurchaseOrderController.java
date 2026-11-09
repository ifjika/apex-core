package com.apex.procurement.controller;

import com.apex.procurement.domain.PurchaseOrder;
import com.apex.procurement.service.PurchaseOrderService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/procurement/orders")
public class PurchaseOrderController {

    private final PurchaseOrderService orderService;

    public PurchaseOrderController(PurchaseOrderService orderService) {
        this.orderService = orderService;
    }

    public record CreateOrderFromRequisitionRequest(
        @NotBlank(message = "Vendor ID is mandatory")
        String vendorId
    ) {}

    @PostMapping("/from-pr/{requisitionId}")
    public ResponseEntity<PurchaseOrder> createOrderFromRequisition(
        @PathVariable String requisitionId,
        @RequestBody CreateOrderFromRequisitionRequest request
    ) {
        PurchaseOrder order = orderService.convertRequisitionToOrder(requisitionId, request.vendorId());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrder> getOrderById(@PathVariable String id) {
        PurchaseOrder order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}

