package com.apex.hris.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Monetary authority threshold matrix mapping organizational roles to approval ranges.
 */
@Entity
@Table(name = "approval_matrices", schema = "hris")
public class ApprovalMatrix {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "role", length = 32, nullable = false)
    private String role;

    @Column(name = "min_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 19, scale = 4)
    private BigDecimal maxAmount; // NULL represents unbounded upper limit

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ApprovalMatrix() {}

    public ApprovalMatrix(String id, String role, BigDecimal minAmount, BigDecimal maxAmount) {
        this.id = id;
        this.role = role;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.createdAt = Instant.now();
    }

    /**
     * Determines if a monetary sum falls within the role's discretionary threshold.
     */
    public boolean coversAmount(BigDecimal amount) {
        if (amount == null) return false;
        boolean aboveMin = amount.compareTo(minAmount) >= 0;
        boolean belowMax = maxAmount == null || amount.compareTo(maxAmount) <= 0;
        return aboveMin && belowMax;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

