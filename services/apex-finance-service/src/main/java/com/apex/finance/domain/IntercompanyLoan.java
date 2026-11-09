package com.apex.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Intercompany loan agreement providing working capital for procurement.
 */
@Entity
@Table(name = "intercompany_loans")
public class IntercompanyLoan {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "loan_reference", length = 64, nullable = false, unique = true)
    private String loanReference;

    @Column(name = "borrower_subsidiary", length = 32, nullable = false)
    private String borrowerSubsidiary;

    @Column(name = "lender_subsidiary", length = 32, nullable = false)
    private String lenderSubsidiary;

    @Column(name = "purchase_requisition_id", length = 36)
    private String purchaseRequisitionId;

    @Column(name = "principal_amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal interestRate = new BigDecimal("0.0850"); // 8.50% p.a.

    @Column(name = "spread_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal spreadRate = new BigDecimal("0.0250"); // 2.50% p.a.

    @Column(name = "tenor_months", nullable = false)
    private int tenorMonths = 6;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(name = "total_interest_projected", precision = 19, scale = 4, nullable = false)
    private BigDecimal totalInterestProjected;

    @Column(name = "holding_margin_projected", precision = 19, scale = 4, nullable = false)
    private BigDecimal holdingMarginProjected;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public IntercompanyLoan() {}

    public IntercompanyLoan(String id, String loanReference, String borrowerSubsidiary, String lenderSubsidiary,
                            String purchaseRequisitionId, BigDecimal principalAmount, BigDecimal interestRate,
                            BigDecimal spreadRate, int tenorMonths) {
        this.id = id;
        this.loanReference = loanReference;
        this.borrowerSubsidiary = borrowerSubsidiary;
        this.lenderSubsidiary = lenderSubsidiary;
        this.purchaseRequisitionId = purchaseRequisitionId;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.spreadRate = spreadRate;
        this.tenorMonths = tenorMonths;
        this.status = LoanStatus.ACTIVE;
        this.createdAt = Instant.now();

        // Calculate projections: Principal * Rate * (Tenor / 12)
        BigDecimal tenorFraction = BigDecimal.valueOf(tenorMonths).divide(BigDecimal.valueOf(12), 6, RoundingMode.HALF_EVEN);
        this.totalInterestProjected = principalAmount.multiply(interestRate).multiply(tenorFraction).setScale(4, RoundingMode.HALF_EVEN);
        this.holdingMarginProjected = principalAmount.multiply(spreadRate).multiply(tenorFraction).setScale(4, RoundingMode.HALF_EVEN);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLoanReference() { return loanReference; }
    public void setLoanReference(String loanReference) { this.loanReference = loanReference; }
    public String getBorrowerSubsidiary() { return borrowerSubsidiary; }
    public void setBorrowerSubsidiary(String borrowerSubsidiary) { this.borrowerSubsidiary = borrowerSubsidiary; }
    public String getLenderSubsidiary() { return lenderSubsidiary; }
    public void setLenderSubsidiary(String lenderSubsidiary) { this.lenderSubsidiary = lenderSubsidiary; }
    public String getPurchaseRequisitionId() { return purchaseRequisitionId; }
    public void setPurchaseRequisitionId(String purchaseRequisitionId) { this.purchaseRequisitionId = purchaseRequisitionId; }
    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
    public BigDecimal getSpreadRate() { return spreadRate; }
    public void setSpreadRate(BigDecimal spreadRate) { this.spreadRate = spreadRate; }
    public int getTenorMonths() { return tenorMonths; }
    public void setTenorMonths(int tenorMonths) { this.tenorMonths = tenorMonths; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }
    public BigDecimal getTotalInterestProjected() { return totalInterestProjected; }
    public void setTotalInterestProjected(BigDecimal totalInterestProjected) { this.totalInterestProjected = totalInterestProjected; }
    public BigDecimal getHoldingMarginProjected() { return holdingMarginProjected; }
    public void setHoldingMarginProjected(BigDecimal holdingMarginProjected) { this.holdingMarginProjected = holdingMarginProjected; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

