package com.apex.finance.service;

import com.apex.common.exception.BusinessException;
import com.apex.common.exception.EntityNotFoundException;
import com.apex.finance.domain.EntryType;
import com.apex.finance.domain.IntercompanyLoan;
import com.apex.finance.domain.LoanStatus;
import com.apex.finance.domain.TreasuryCashPool;
import com.apex.finance.dto.LoanOriginationRequest;
import com.apex.finance.dto.LoanResponse;
import com.apex.finance.dto.TreasurySummaryResponse;
import com.apex.finance.repository.IntercompanyLoanRepository;
import com.apex.finance.repository.TreasuryCashPoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

/**
 * In-House Banking & Treasury Holding Engine.
 * Manages intercompany cash pooling, working capital loans, and interest margin accrual.
 */
@Service
public class TreasuryHoldingService {

    private final TreasuryCashPoolRepository poolRepository;
    private final IntercompanyLoanRepository loanRepository;
    private final CryptographicLedgerService ledgerService;

    // Standard Arm's Length Interest Rates (DJP & OECD Compliant)
    public static final BigDecimal BORROWER_LENDING_RATE = new BigDecimal("0.0850"); // 8.50% p.a.
    public static final BigDecimal HOLDING_SPREAD_MARGIN = new BigDecimal("0.0250"); // 2.50% p.a. NIM

    public TreasuryHoldingService(TreasuryCashPoolRepository poolRepository,
                                  IntercompanyLoanRepository loanRepository,
                                  CryptographicLedgerService ledgerService) {
        this.poolRepository = poolRepository;
        this.loanRepository = loanRepository;
        this.ledgerService = ledgerService;
    }

    /**
     * Originates an intercompany loan from a surplus subsidiary's pool to a borrower.
     */
    @Transactional
    public LoanResponse originateLoan(LoanOriginationRequest request) {
        if (request.borrowerSubsidiary().equalsIgnoreCase(request.lenderSubsidiary())) {
            throw new BusinessException("Borrower and lender subsidiaries must be distinct entities.", "INVALID_INTERCOMPANY_ENTITIES");
        }

        TreasuryCashPool lenderPool = poolRepository.findBySubsidiaryCode(request.lenderSubsidiary())
            .orElseThrow(() -> new EntityNotFoundException("TreasuryCashPool", request.lenderSubsidiary()));

        if (lenderPool.getCurrentBalance().compareTo(request.principalAmount()) < 0) {
            throw new BusinessException(
                String.format("Insufficient surplus liquidity in %s pool. Available: IDR %s, Requested: IDR %s",
                    request.lenderSubsidiary(), lenderPool.getCurrentBalance(), request.principalAmount()),
                "INSUFFICIENT_POOL_LIQUIDITY"
            );
        }

        // Deduct from lending pool
        lenderPool.withdraw(request.principalAmount());
        poolRepository.save(lenderPool);

        // Generate Loan Contract
        int tenor = request.tenorMonths() > 0 ? request.tenorMonths() : 6;
        String loanRef = "ICL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        IntercompanyLoan loan = new IntercompanyLoan(
            UUID.randomUUID().toString(),
            loanRef,
            request.borrowerSubsidiary(),
            request.lenderSubsidiary(),
            request.purchaseRequisitionId(),
            request.principalAmount(),
            BORROWER_LENDING_RATE,
            HOLDING_SPREAD_MARGIN,
            tenor
        );
        loanRepository.save(loan);

        // Post balanced double-entry to Cryptographic General Ledger
        ledgerService.postBalancedEntry(
            loanRef,
            "1105-INTERCOMPANY-LOAN-RECEIVABLE", // Asset at Holding
            "1001-HOLDING-CASH-POOL",           // Cash disbursed from Pool
            request.principalAmount()
        );

        return toResponse(loan);
    }

    /**
     * Accrues monthly interest income for Holding (Net Interest Margin recognition).
     */
    @Transactional
    public void accrueMonthlyInterest(String loanId) {
        IntercompanyLoan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new EntityNotFoundException("IntercompanyLoan", loanId));

        if (loan.getStatus() != LoanStatus.ACTIVE) {
            throw new BusinessException("Cannot accrue interest on inactive loan.", "LOAN_NOT_ACTIVE");
        }

        // Monthly Holding Net Interest Margin = (Principal * SpreadRate) / 12
        BigDecimal monthlyHoldingMargin = loan.getPrincipalAmount()
            .multiply(loan.getSpreadRate())
            .divide(BigDecimal.valueOf(12), 4, RoundingMode.HALF_EVEN);

        // Post interest income into Cryptographic General Ledger
        ledgerService.postBalancedEntry(
            loan.getLoanReference() + "-MINT",
            "1001-HOLDING-CASH-POOL",               // Cash collected
            "4105-INTERCOMPANY-INTEREST-INCOME",     // Holding Revenue
            monthlyHoldingMargin
        );
    }

    /**
     * Retrieves aggregated treasury liquidity and loan metrics across all corporate entities.
     */
    @Transactional(readOnly = true)
    public TreasurySummaryResponse getTreasurySummary() {
        List<TreasuryCashPool> pools = poolRepository.findAll();
        List<IntercompanyLoan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);

        BigDecimal totalLiquidity = pools.stream()
            .map(TreasuryCashPool::getCurrentBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalLoansDisbursed = activeLoans.stream()
            .map(IntercompanyLoan::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedAnnualHoldingIncome = activeLoans.stream()
            .map(l -> l.getPrincipalAmount().multiply(l.getSpreadRate()))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(4, RoundingMode.HALF_EVEN);

        List<TreasurySummaryResponse.SubsidiaryPoolDTO> poolDTOs = pools.stream()
            .map(p -> new TreasurySummaryResponse.SubsidiaryPoolDTO(
                p.getSubsidiaryCode(),
                p.getSubsidiaryName(),
                p.getCurrentBalance(),
                p.getYieldRate()
            ))
            .toList();

        return new TreasurySummaryResponse(
            totalLiquidity,
            totalLoansDisbursed,
            projectedAnnualHoldingIncome,
            activeLoans.size(),
            poolDTOs
        );
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll().stream().map(this::toResponse).toList();
    }

    private LoanResponse toResponse(IntercompanyLoan l) {
        return new LoanResponse(
            l.getId(),
            l.getLoanReference(),
            l.getBorrowerSubsidiary(),
            l.getLenderSubsidiary(),
            l.getPurchaseRequisitionId(),
            l.getPrincipalAmount(),
            l.getInterestRate(),
            l.getSpreadRate(),
            l.getTenorMonths(),
            l.getStatus(),
            l.getTotalInterestProjected(),
            l.getHoldingMarginProjected(),
            l.getCreatedAt()
        );
    }
}

