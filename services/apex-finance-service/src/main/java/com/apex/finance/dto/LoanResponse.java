package com.apex.finance.dto;

import com.apex.finance.domain.LoanStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record LoanResponse(
    String id,
    String loanReference,
    String borrowerSubsidiary,
    String lenderSubsidiary,
    String purchaseRequisitionId,
    BigDecimal principalAmount,
    BigDecimal interestRate,
    BigDecimal spreadRate,
    int tenorMonths,
    LoanStatus status,
    BigDecimal totalInterestProjected,
    BigDecimal holdingMarginProjected,
    Instant createdAt
) {}

