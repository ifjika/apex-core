package com.apex.finance.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Table(name = "payment_vouchers")
public class PaymentVoucher {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "voucher_number", length = 64, nullable = false, unique = true)
    private String voucherNumber;

    @Column(name = "invoice_id", length = 36, nullable = false)
    private String invoiceId;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    private VoucherStatus status = VoucherStatus.ISSUED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public PaymentVoucher() {}

    public PaymentVoucher(String id, String voucherNumber, String invoiceId, BigDecimal amount) {
        this.id = id;
        this.voucherNumber = voucherNumber;
        this.invoiceId = invoiceId;
        this.amount = amount.setScale(4, RoundingMode.HALF_EVEN);
        this.status = VoucherStatus.ISSUED;
        this.createdAt = Instant.now();
    }

    public void markAsDisbursed() {
        this.status = VoucherStatus.DISBURSED;
    }

    public void cancel() {
        this.status = VoucherStatus.CANCELLED;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVoucherNumber() { return voucherNumber; }
    public void setVoucherNumber(String voucherNumber) { this.voucherNumber = voucherNumber; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public VoucherStatus getStatus() { return status; }
    public void setStatus(VoucherStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

