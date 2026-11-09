package com.apex.finance.repository;

import com.apex.finance.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, String> {
    Optional<LedgerEntry> findTopByOrderBySequenceNumberDesc();
    List<LedgerEntry> findAllByOrderBySequenceNumberAsc();
}

