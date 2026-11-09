-- =============================================================================
-- APEX-Core Procurement Schema Initialization (MySQL 8.0)
-- =============================================================================

CREATE TABLE IF NOT EXISTS vendors (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    tax_id VARCHAR(64) NOT NULL,
    bank_account_number VARCHAR(64) NOT NULL,
    bank_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS purchase_requisitions (
    id VARCHAR(36) PRIMARY KEY,
    pr_number VARCHAR(64) NOT NULL UNIQUE,
    cost_center_id VARCHAR(36) NOT NULL,
    requester_id VARCHAR(36) NOT NULL,
    current_approver_id VARCHAR(36),
    status VARCHAR(32) NOT NULL, -- DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ORDERED, CANCELLED
    total_amount DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    rejection_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pr_line_items (
    id VARCHAR(36) PRIMARY KEY,
    requisition_id VARCHAR(36) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    description TEXT,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_pr_line_items_req FOREIGN KEY (requisition_id) REFERENCES purchase_requisitions(id) ON DELETE CASCADE,
    CONSTRAINT chk_pr_qty CHECK (quantity > 0),
    CONSTRAINT chk_pr_price CHECK (unit_price > 0),
    CONSTRAINT chk_pr_subtotal CHECK (subtotal > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS purchase_orders (
    id VARCHAR(36) PRIMARY KEY,
    po_number VARCHAR(64) NOT NULL UNIQUE,
    requisition_id VARCHAR(36) NOT NULL,
    vendor_id VARCHAR(36) NOT NULL,
    status VARCHAR(32) NOT NULL, -- ISSUED, ACKNOWLEDGED, PARTIALLY_FULFILLED, FULFILLED, CANCELLED
    total_amount DECIMAL(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_purchase_orders_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS po_line_items (
    id VARCHAR(36) PRIMARY KEY,
    purchase_order_id VARCHAR(36) NOT NULL,
    pr_line_item_id VARCHAR(36) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    CONSTRAINT fk_po_line_items_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_po_qty CHECK (quantity > 0),
    CONSTRAINT chk_po_price CHECK (unit_price > 0),
    CONSTRAINT chk_po_subtotal CHECK (subtotal > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published_at TIMESTAMP(6) NULL,
    INDEX idx_proc_outbox_unpub (published, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed enterprise vendor suppliers
INSERT IGNORE INTO vendors (id, code, name, tax_id, bank_account_number, bank_name, status) VALUES
('vend-infra-001', 'VEND-IND-501', 'PT Multipolar Technology Infrastructure', '01.234.567.8-012.000', '108001928374', 'Bank Mandiri', 'ACTIVE'),
('vend-cloud-002', 'VEND-IND-502', 'PT Telkom Data Ekosistem', '02.345.678.9-013.000', '882003948571', 'Bank Central Asia', 'ACTIVE');
