package com.apex.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Subsidiary liquidity account pooled at the Holding Treasury level.
 */
@Entity
@Table(name = "treasury_cash_pools")
public class TreasuryCashPool {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "subsidiary_code", length = 32, nullable = false, unique = true)
    private String subsidiaryCode;

    @Column(name = "subsidiary_name", length = 128, nullable = false)
    private String subsidiaryName;

    @Column(name = "current_balance", precision = 19, scale = 4, nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "yield_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal yieldRate = new BigDecimal("0.0600"); // 6.00% p.a.

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public TreasuryCashPool() {}

    public TreasuryCashPool(String id, String subsidiaryCode, String subsidiaryName, BigDecimal currentBalance, BigDecimal yieldRate) {
        this.id = id;
        this.subsidiaryCode = subsidiaryCode;
        this.subsidiaryName = subsidiaryName;
        this.currentBalance = currentBalance;
        this.yieldRate = yieldRate;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void deposit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.currentBalance = this.currentBalance.add(amount);
            this.updatedAt = Instant.now();
        }
    }

    public void withdraw(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            if (this.currentBalance.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient liquidity in cash pool for subsidiary: " + subsidiaryCode);
            }
            this.currentBalance = this.currentBalance.subtract(amount);
            this.updatedAt = Instant.now();
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubsidiaryCode() { return subsidiaryCode; }
    public void setSubsidiaryCode(String subsidiaryCode) { this.subsidiaryCode = subsidiaryCode; }
    public String getSubsidiaryName() { return subsidiaryName; }
    public void setSubsidiaryName(String subsidiaryName) { this.subsidiaryName = subsidiaryName; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public void setCurrentBalance(BigDecimal currentBalance) { this.currentBalance = currentBalance; }
    public BigDecimal getYieldRate() { return yieldRate; }
    public void setYieldRate(BigDecimal yieldRate) { this.yieldRate = yieldRate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

