package com.apex.hris.controller;

import com.apex.hris.dto.ApprovalRouteResponse;
import com.apex.hris.service.ApprovalRoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/hris/approval-routing")
public class ApprovalRoutingController {

    private final ApprovalRoutingService approvalRoutingService;

    public ApprovalRoutingController(ApprovalRoutingService approvalRoutingService) {
        this.approvalRoutingService = approvalRoutingService;
    }

    @GetMapping("/route")
    public ResponseEntity<ApprovalRouteResponse> getApprovalRoute(
        @RequestParam String requesterId,
        @RequestParam BigDecimal amount
    ) {
        ApprovalRouteResponse route = approvalRoutingService.calculateRoute(requesterId, amount);
        return ResponseEntity.ok(route);
    }
}

