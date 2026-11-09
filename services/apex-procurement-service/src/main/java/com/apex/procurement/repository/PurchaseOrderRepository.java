package com.apex.procurement.repository;

import com.apex.procurement.domain.OrderStatus;
import com.apex.procurement.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, String> {
    Optional<PurchaseOrder> findByPoNumber(String poNumber);
    List<PurchaseOrder> findByStatus(OrderStatus status);
    List<PurchaseOrder> findByVendorId(String vendorId);
}

