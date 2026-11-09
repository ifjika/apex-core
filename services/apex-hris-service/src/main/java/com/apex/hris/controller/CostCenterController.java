package com.apex.hris.controller;

import com.apex.common.exception.EntityNotFoundException;
import com.apex.hris.domain.CostCenter;
import com.apex.hris.repository.CostCenterRepository;
import com.apex.hris.service.BudgetReservationService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/hris/cost-centers")
public class CostCenterController {

    private final CostCenterRepository costCenterRepository;
    private final BudgetReservationService budgetReservationService;

    public CostCenterController(CostCenterRepository costCenterRepository, BudgetReservationService budgetReservationService) {
        this.costCenterRepository = costCenterRepository;
        this.budgetReservationService = budgetReservationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CostCenter> getCostCenterById(@PathVariable String id) {
        return costCenterRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("CostCenter", id));
    }

    public record BudgetActionRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be strictly positive")
        BigDecimal amount
    ) {}

    @PostMapping("/{id}/reserve")
    public ResponseEntity<CostCenter> reserveBudget(
        @PathVariable String id,
        @RequestBody BudgetActionRequest request
    ) {
        CostCenter updated = budgetReservationService.reserveBudget(id, request.amount());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/release")
    public ResponseEntity<CostCenter> releaseBudget(
        @PathVariable String id,
        @RequestBody BudgetActionRequest request
    ) {
        CostCenter updated = budgetReservationService.releaseBudget(id, request.amount());
        return ResponseEntity.ok(updated);
    }
}

