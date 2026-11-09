package com.apex.finance.repository;

import com.apex.finance.domain.InvoiceStatus;
import com.apex.finance.domain.VendorInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorInvoiceRepository extends JpaRepository<VendorInvoice, String> {
    Optional<VendorInvoice> findByInvoiceNumber(String invoiceNumber);
    List<VendorInvoice> findByStatus(InvoiceStatus status);
    List<VendorInvoice> findByPurchaseOrderId(String purchaseOrderId);
}

