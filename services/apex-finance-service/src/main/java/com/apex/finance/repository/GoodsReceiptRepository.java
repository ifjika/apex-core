package com.apex.finance.repository;

import com.apex.finance.domain.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, String> {
    Optional<GoodsReceipt> findByGrNumber(String grNumber);
    Optional<GoodsReceipt> findByPurchaseOrderId(String purchaseOrderId);
}

