package com.apex.finance.controller;

import com.apex.finance.dto.LoanOriginationRequest;
import com.apex.finance.dto.LoanResponse;
import com.apex.finance.dto.TreasurySummaryResponse;
import com.apex.finance.service.TreasuryHoldingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/treasury")
public class TreasuryHoldingController {

    private final TreasuryHoldingService treasuryService;

    public TreasuryHoldingController(TreasuryHoldingService treasuryService) {
        this.treasuryService = treasuryService;
    }

    @GetMapping("/summary")
    public ResponseEntity<TreasurySummaryResponse> getSummary() {
        return ResponseEntity.ok(treasuryService.getTreasurySummary());
    }

    @PostMapping("/loans")
    public ResponseEntity<LoanResponse> originateLoan(@Valid @RequestBody LoanOriginationRequest request) {
        LoanResponse response = treasuryService.originateLoan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/loans")
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        return ResponseEntity.ok(treasuryService.getAllLoans());
    }

    @PostMapping("/loans/{id}/accrue-interest")
    public ResponseEntity<Void> accrueInterest(@PathVariable String id) {
        treasuryService.accrueMonthlyInterest(id);
        return ResponseEntity.ok().build();
    }
}

