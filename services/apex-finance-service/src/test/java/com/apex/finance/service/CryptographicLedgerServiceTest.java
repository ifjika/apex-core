package com.apex.finance.service;

import com.apex.finance.domain.EntryType;
import com.apex.finance.domain.LedgerEntry;
import com.apex.finance.repository.LedgerEntryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CryptographicLedgerServiceTest {

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @InjectMocks
    private CryptographicLedgerService ledgerService;

    @Test
    @DisplayName("Should successfully append balanced debit and credit entries with linked SHA-256 hashes")
    void shouldAppendBalancedJournal() {
        when(ledgerEntryRepository.findTopByOrderBySequenceNumberDesc()).thenReturn(Optional.empty());
        when(ledgerEntryRepository.saveAndFlush(any(LedgerEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<LedgerEntry> entries = ledgerService.appendBalancedJournal(
            "PV-2026-001",
            "5101-INVENTORY",
            "2101-ACCOUNTS-PAYABLE",
            new BigDecimal("50000000.00")
        );

        assertThat(entries).hasSize(2);
        LedgerEntry debit = entries.get(0);
        LedgerEntry credit = entries.get(1);

        assertThat(debit.getEntryType()).isEqualTo(EntryType.DEBIT);
        assertThat(debit.getPreviousHash()).isEqualTo(LedgerEntry.GENESIS_HASH);
        assertThat(debit.isValidHash()).isTrue();

        assertThat(credit.getEntryType()).isEqualTo(EntryType.CREDIT);
        assertThat(credit.getPreviousHash()).isEqualTo(debit.getCurrentHash());
        assertThat(credit.isValidHash()).isTrue();

        verify(ledgerEntryRepository, times(2)).saveAndFlush(any(LedgerEntry.class));
    }

    @Test
    @DisplayName("Should verify chain integrity when all hash links are continuous and untampered")
    void shouldVerifyCleanChainIntegrity() {
        LedgerEntry entry1 = new LedgerEntry("id-1", "PV-001", "5101", EntryType.DEBIT, new BigDecimal("100.00"), LedgerEntry.GENESIS_HASH);
        entry1.setSequenceNumber(1L);

        LedgerEntry entry2 = new LedgerEntry("id-2", "PV-001", "2101", EntryType.CREDIT, new BigDecimal("100.00"), entry1.getCurrentHash());
        entry2.setSequenceNumber(2L);

        when(ledgerEntryRepository.findAllByOrderBySequenceNumberAsc()).thenReturn(List.of(entry1, entry2));

        CryptographicLedgerService.ChainVerificationReport report = ledgerService.verifyChainIntegrity();

        assertThat(report.isTamperFree()).isTrue();
        assertThat(report.totalEntriesVerified()).isEqualTo(2);
    }
}

