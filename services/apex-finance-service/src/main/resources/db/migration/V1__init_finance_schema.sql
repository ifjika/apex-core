-- =============================================================================
-- APEX-Core Finance Schema Initialization (MySQL 8.0)
-- =============================================================================

CREATE TABLE IF NOT EXISTS goods_receipts (
    id VARCHAR(36) PRIMARY KEY,
    gr_number VARCHAR(64) NOT NULL UNIQUE,
    purchase_order_id VARCHAR(36) NOT NULL,
    received_by VARCHAR(36) NOT NULL,
    received_date TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS gr_line_items (
    id VARCHAR(36) PRIMARY KEY,
    goods_receipt_id VARCHAR(36) NOT NULL,
    po_line_item_id VARCHAR(36) NOT NULL,
    received_quantity INT NOT NULL,
    CONSTRAINT fk_gr_line_items_gr FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipts(id) ON DELETE CASCADE,
    CONSTRAINT chk_gr_received_qty CHECK (received_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vendor_invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(64) NOT NULL UNIQUE,
    vendor_id VARCHAR(36) NOT NULL,
    purchase_order_id VARCHAR(36) NOT NULL,
    goods_receipt_id VARCHAR(36) NOT NULL,
    status VARCHAR(32) NOT NULL, -- PENDING_MATCH, MATCHED, MATCHED_WITH_TOLERANCE, DISCREPANCY, PAID
    tax_amount DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    total_amount DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vi_line_items (
    id VARCHAR(36) PRIMARY KEY,
    vendor_invoice_id VARCHAR(36) NOT NULL,
    po_line_item_id VARCHAR(36) NOT NULL,
    invoiced_quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    CONSTRAINT fk_vi_line_items_vi FOREIGN KEY (vendor_invoice_id) REFERENCES vendor_invoices(id) ON DELETE CASCADE,
    CONSTRAINT chk_vi_qty CHECK (invoiced_quantity > 0),
    CONSTRAINT chk_vi_price CHECK (unit_price > 0),
    CONSTRAINT chk_vi_subtotal CHECK (subtotal > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_vouchers (
    id VARCHAR(36) PRIMARY KEY,
    voucher_number VARCHAR(64) NOT NULL UNIQUE,
    invoice_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(32) NOT NULL, -- ISSUED, DISBURSED, CANCELLED
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_payment_vouchers_vi FOREIGN KEY (invoice_id) REFERENCES vendor_invoices(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cryptographically chained append-only general ledger
CREATE TABLE IF NOT EXISTS ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    sequence_number BIGINT NOT NULL AUTO_INCREMENT UNIQUE,
    transaction_reference VARCHAR(64) NOT NULL,
    account_code VARCHAR(32) NOT NULL,
    entry_type VARCHAR(6) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    previous_hash VARCHAR(64) NOT NULL,
    current_hash VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_ledger_seq (sequence_number),
    CONSTRAINT chk_ledger_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
