package com.apex.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LoanOriginationRequest(
    @NotBlank(message = "Borrower subsidiary code is required")
    String borrowerSubsidiary,

    @NotBlank(message = "Lender subsidiary code is required")
    String lenderSubsidiary,

    String purchaseRequisitionId,

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000000.00", message = "Minimum loan amount is IDR 1,000,000")
    BigDecimal principalAmount,

    int tenorMonths
) {}

