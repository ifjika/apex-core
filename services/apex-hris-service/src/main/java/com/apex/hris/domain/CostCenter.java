package com.apex.hris.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Cost Center entity with optimistic locking and budget boundary validation.
 */
@Entity
@Table(name = "cost_centers")
public class CostCenter {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "department_id", length = 36, nullable = false)
    private String departmentId;

    @Column(name = "code", length = 32, nullable = false, unique = true)
    private String code;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "allocated_budget", precision = 19, scale = 4, nullable = false)
    private BigDecimal allocatedBudget = BigDecimal.ZERO;

    @Column(name = "reserved_budget", precision = 19, scale = 4, nullable = false)
    private BigDecimal reservedBudget = BigDecimal.ZERO;

    @Column(name = "spent_budget", precision = 19, scale = 4, nullable = false)
    private BigDecimal spentBudget = BigDecimal.ZERO;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public CostCenter() {}

    public CostCenter(String id, String departmentId, String code, String name, BigDecimal allocatedBudget) {
        this.id = id;
        this.departmentId = departmentId;
        this.code = code;
        this.name = name;
        this.allocatedBudget = allocatedBudget.setScale(4, RoundingMode.HALF_EVEN);
        this.reservedBudget = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        this.spentBudget = BigDecimal.ZERO.setScale(4, RoundingMode.HALF_EVEN);
        this.version = 0L;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Checks if the cost center has sufficient unreserved and unspent funds.
     */
    public boolean hasAvailableBudget(BigDecimal requestedAmount) {
        BigDecimal totalCommitted = reservedBudget.add(spentBudget);
        BigDecimal available = allocatedBudget.subtract(totalCommitted);
        return available.compareTo(requestedAmount) >= 0;
    }

    /**
     * Atomically reserves budget for pending procurement.
     */
    public void reserveBudget(BigDecimal amount) {
        if (!hasAvailableBudget(amount)) {
            throw new IllegalStateException("Insufficient budget available in Cost Center: " + code);
        }
        this.reservedBudget = this.reservedBudget.add(amount).setScale(4, RoundingMode.HALF_EVEN);
        this.updatedAt = Instant.now();
    }

    /**
     * Releases reserved budget when a requisition is rejected or cancelled.
     */
    public void releaseReservation(BigDecimal amount) {
        BigDecimal newReserved = this.reservedBudget.subtract(amount);
        if (newReserved.compareTo(BigDecimal.ZERO) < 0) {
            newReserved = BigDecimal.ZERO;
        }
        this.reservedBudget = newReserved.setScale(4, RoundingMode.HALF_EVEN);
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAllocatedBudget() { return allocatedBudget; }
    public void setAllocatedBudget(BigDecimal allocatedBudget) { this.allocatedBudget = allocatedBudget; }
    public BigDecimal getReservedBudget() { return reservedBudget; }
    public void setReservedBudget(BigDecimal reservedBudget) { this.reservedBudget = reservedBudget; }
    public BigDecimal getSpentBudget() { return spentBudget; }
    public void setSpentBudget(BigDecimal spentBudget) { this.spentBudget = spentBudget; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

