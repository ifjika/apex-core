package com.apex.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record TreasurySummaryResponse(
    BigDecimal totalLiquidityInPool,
    BigDecimal totalActiveLoansDisbursed,
    BigDecimal projectedAnnualHoldingIncome,
    int activeLoanCount,
    List<SubsidiaryPoolDTO> pools
) {
    public record SubsidiaryPoolDTO(
        String subsidiaryCode,
        String subsidiaryName,
        BigDecimal balance,
        BigDecimal yieldRate
    ) {}
}

