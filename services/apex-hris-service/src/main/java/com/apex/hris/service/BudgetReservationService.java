package com.apex.hris.service;

import com.apex.common.exception.BusinessException;
import com.apex.common.exception.EntityNotFoundException;
import com.apex.hris.domain.CostCenter;
import com.apex.hris.repository.CostCenterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service managing transactional budget allocation, reservation, and release for Cost Centers.
 */
@Service
public class BudgetReservationService {

    private final CostCenterRepository costCenterRepository;

    public BudgetReservationService(CostCenterRepository costCenterRepository) {
        this.costCenterRepository = costCenterRepository;
    }

    /**
     * Reserves funds within a Cost Center with optimistic concurrency protection.
     */
    @Transactional
    public CostCenter reserveBudget(String costCenterId, BigDecimal amount) {
        CostCenter costCenter = costCenterRepository.findById(costCenterId)
            .orElseThrow(() -> new EntityNotFoundException("CostCenter", costCenterId));

        if (!costCenter.hasAvailableBudget(amount)) {
            throw new BusinessException(
                String.format("Cost Center [%s] has insufficient funds to reserve %s.", costCenter.getCode(), amount),
                "INSUFFICIENT_COST_CENTER_BUDGET"
            );
        }

        costCenter.reserveBudget(amount);
        return costCenterRepository.save(costCenter);
    }

    /**
     * Releases previously reserved funds when a requisition is rejected or cancelled.
     */
    @Transactional
    public CostCenter releaseBudget(String costCenterId, BigDecimal amount) {
        CostCenter costCenter = costCenterRepository.findById(costCenterId)
            .orElseThrow(() -> new EntityNotFoundException("CostCenter", costCenterId));

        costCenter.releaseReservation(amount);
        return costCenterRepository.save(costCenter);
    }
}

