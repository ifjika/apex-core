package com.apex.finance.controller;

import com.apex.finance.domain.LedgerEntry;
import com.apex.finance.repository.LedgerEntryRepository;
import com.apex.finance.service.CryptographicLedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/finance/ledger")
public class LedgerController {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final CryptographicLedgerService ledgerService;

    public LedgerController(
        LedgerEntryRepository ledgerEntryRepository,
        CryptographicLedgerService ledgerService
    ) {
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerService = ledgerService;
    }

    @GetMapping("/verify-chain")
    public ResponseEntity<CryptographicLedgerService.ChainVerificationReport> verifyChain() {
        CryptographicLedgerService.ChainVerificationReport report = ledgerService.verifyChainIntegrity();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/entries")
    public ResponseEntity<List<LedgerEntry>> getAllEntries() {
        return ResponseEntity.ok(ledgerEntryRepository.findAllByOrderBySequenceNumberAsc());
    }
}

