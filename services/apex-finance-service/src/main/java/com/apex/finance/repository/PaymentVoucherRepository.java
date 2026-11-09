package com.apex.finance.repository;

import com.apex.finance.domain.PaymentVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, String> {
    Optional<PaymentVoucher> findByVoucherNumber(String voucherNumber);
    Optional<PaymentVoucher> findByInvoiceId(String invoiceId);
}

