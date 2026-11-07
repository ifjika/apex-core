package com.apex.procurement.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Enterprise vendor supplier record.
 */
@Entity
@Table(name = "vendors", schema = "procurement")
public class Vendor {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "tax_id", length = 64, nullable = false)
    private String taxId;

    @Column(name = "bank_account_number", length = 64, nullable = false)
    private String bankAccountNumber;

    @Column(name = "bank_name", length = 64, nullable = false)
    private String bankName;

    @Column(name = "status", length = 32, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Vendor() {}

    public Vendor(String id, String code, String name, String taxId, String bankAccountNumber, String bankName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.taxId = taxId;
        this.bankAccountNumber = bankAccountNumber;
        this.bankName = bankName;
        this.status = "ACTIVE";
        this.createdAt = Instant.now();
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

