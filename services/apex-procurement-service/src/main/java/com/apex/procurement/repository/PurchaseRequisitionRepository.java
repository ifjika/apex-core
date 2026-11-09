package com.apex.procurement.repository;

import com.apex.procurement.domain.PurchaseRequisition;
import com.apex.procurement.domain.RequisitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, String> {
    Optional<PurchaseRequisition> findByPrNumber(String prNumber);
    List<PurchaseRequisition> findByStatus(RequisitionStatus status);
    List<PurchaseRequisition> findByCostCenterId(String costCenterId);
}

