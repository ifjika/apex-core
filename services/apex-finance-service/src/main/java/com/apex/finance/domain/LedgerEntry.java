package com.apex.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Append-only double-entry general ledger record bound by sequential SHA-256 hash chaining.
 */
@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_ledger_seq", columnList = "sequence_number")
})
public class LedgerEntry {

    public static final String GENESIS_HASH = "0".repeat(64);

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sequence_number", nullable = false, insertable = false, updatable = false)
    private Long sequenceNumber;

    @Column(name = "transaction_reference", length = 64, nullable = false)
    private String transactionReference;

    @Column(name = "account_code", length = 32, nullable = false)
    private String accountCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 6, nullable = false)
    private EntryType entryType;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Column(name = "previous_hash", length = 64, nullable = false)
    private String previousHash;

    @Column(name = "current_hash", length = 64, nullable = false)
    private String currentHash;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    public LedgerEntry() {}

    public LedgerEntry(
        String id,
        String transactionReference,
        String accountCode,
        EntryType entryType,
        BigDecimal amount,
        String previousHash
    ) {
        this.id = id;
        this.transactionReference = transactionReference;
        this.accountCode = accountCode;
        this.entryType = entryType;
        this.amount = amount;
        this.previousHash = (previousHash != null && !previousHash.isBlank()) ? previousHash : GENESIS_HASH;
        this.timestamp = Instant.now();
        this.currentHash = computeHash(this.previousHash, this.transactionReference, this.accountCode, this.entryType, this.amount, this.timestamp);
    }

    public static String computeHash(
        String prevHash,
        String txnRef,
        String account,
        EntryType type,
        BigDecimal amt,
        Instant time
    ) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String rawData = String.join("|",
                prevHash,
                txnRef,
                account,
                type.name(),
                amt.toPlainString(),
                time.toString()
            );
            byte[] hashBytes = digest.digest(rawData.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 cryptographic algorithm unavailable", e);
        }
    }

    public boolean isValidHash() {
        String expectedHash = computeHash(this.previousHash, this.transactionReference, this.accountCode, this.entryType, this.amount, this.timestamp);
        return expectedHash.equalsIgnoreCase(this.currentHash);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Long sequenceNumber) { this.sequenceNumber = sequenceNumber; }
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}

