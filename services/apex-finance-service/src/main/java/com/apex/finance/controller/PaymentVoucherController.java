package com.apex.finance.controller;

import com.apex.common.exception.EntityNotFoundException;
import com.apex.finance.domain.PaymentVoucher;
import com.apex.finance.repository.PaymentVoucherRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance/vouchers")
public class PaymentVoucherController {

    private final PaymentVoucherRepository paymentVoucherRepository;

    public PaymentVoucherController(PaymentVoucherRepository paymentVoucherRepository) {
        this.paymentVoucherRepository = paymentVoucherRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentVoucher> getVoucherById(@PathVariable String id) {
        return paymentVoucherRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new EntityNotFoundException("PaymentVoucher", id));
    }
}

