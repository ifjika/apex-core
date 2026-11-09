package com.apex.finance.service;

import com.apex.finance.domain.EntryType;
import com.apex.finance.domain.LedgerEntry;
import com.apex.finance.repository.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service managing cryptographically chained append-only double-entry general ledger records.
 */
@Service
public class CryptographicLedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    public CryptographicLedgerService(LedgerEntryRepository ledgerEntryRepository) {
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    public record ChainVerificationReport(
        long totalEntriesVerified,
        boolean isTamperFree,
        String failureDetails
    ) {}

    /**
     * Appends a balanced debit-credit journal pair bound sequentially by SHA-256 hash chains.
     */
    @Transactional
    public List<LedgerEntry> appendBalancedJournal(
        String transactionReference,
        String debitAccount,
        String creditAccount,
        BigDecimal amount
    ) {
        String lastHash = ledgerEntryRepository.findTopByOrderBySequenceNumberDesc()
            .map(LedgerEntry::getCurrentHash)
            .orElse(LedgerEntry.GENESIS_HASH);

        // 1. Debit Entry
        String debitId = UUID.randomUUID().toString();
        LedgerEntry debitEntry = new LedgerEntry(
            debitId,
            transactionReference,
            debitAccount,
            EntryType.DEBIT,
            amount,
            lastHash
        );
        LedgerEntry savedDebit = ledgerEntryRepository.saveAndFlush(debitEntry);

        // 2. Credit Entry (chained to the newly created debit entry)
        String creditId = UUID.randomUUID().toString();
        LedgerEntry creditEntry = new LedgerEntry(
            creditId,
            transactionReference,
            creditAccount,
            EntryType.CREDIT,
            amount,
            savedDebit.getCurrentHash()
        );
        LedgerEntry savedCredit = ledgerEntryRepository.saveAndFlush(creditEntry);

        return List.of(savedDebit, savedCredit);
    }

    /**
     * Audits the entire ledger chain from entry 1 to N, confirming cryptographic continuity.
     */
    @Transactional(readOnly = true)
    public ChainVerificationReport verifyChainIntegrity() {
        List<LedgerEntry> entries = ledgerEntryRepository.findAllByOrderBySequenceNumberAsc();

        if (entries.isEmpty()) {
            return new ChainVerificationReport(0, true, "Ledger is currently empty.");
        }

        String expectedPrevHash = LedgerEntry.GENESIS_HASH;

        for (int i = 0; i < entries.size(); i++) {
            LedgerEntry current = entries.get(i);

            // Check linked hash continuity
            if (!current.getPreviousHash().equalsIgnoreCase(expectedPrevHash)) {
                return new ChainVerificationReport(
                    i,
                    false,
                    String.format(
                        "Broken hash link at sequence [%d]. Expected prevHash [%s] but found [%s]",
                        current.getSequenceNumber(), expectedPrevHash, current.getPreviousHash()
                    )
                );
            }

            // Check current entry payload tamper detection
            if (!current.isValidHash()) {
                return new ChainVerificationReport(
                    i,
                    false,
                    String.format(
                        "Tampered data detected in entry [%s] at sequence [%d]. Hash does not match stored content.",
                        current.getId(), current.getSequenceNumber()
                    )
                );
            }

            expectedPrevHash = current.getCurrentHash();
        }

        return new ChainVerificationReport(
            entries.size(),
            true,
            "All " + entries.size() + " ledger entries cryptographically verified and unbroken."
        );
    }
}

