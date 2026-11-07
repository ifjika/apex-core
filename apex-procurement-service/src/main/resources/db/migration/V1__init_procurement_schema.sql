-- Schema: procurement initialization
CREATE TABLE IF NOT EXISTS vendors (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    tax_id VARCHAR(64) NOT NULL,
    bank_account_number VARCHAR(64) NOT NULL,
    bank_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_requisitions (
    id VARCHAR(36) PRIMARY KEY,
    pr_number VARCHAR(64) NOT NULL UNIQUE,
    cost_center_id VARCHAR(36) NOT NULL,
    requester_id VARCHAR(36) NOT NULL,
    current_approver_id VARCHAR(36),
    status VARCHAR(32) NOT NULL, -- DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ORDERED, CANCELLED
    total_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    rejection_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pr_line_items (
    id VARCHAR(36) PRIMARY KEY,
    requisition_id VARCHAR(36) NOT NULL REFERENCES purchase_requisitions(id) ON DELETE CASCADE,
    item_name VARCHAR(128) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL CHECK (unit_price > 0),
    subtotal NUMERIC(19, 4) NOT NULL CHECK (subtotal > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id VARCHAR(36) PRIMARY KEY,
    po_number VARCHAR(64) NOT NULL UNIQUE,
    requisition_id VARCHAR(36) NOT NULL,
    vendor_id VARCHAR(36) NOT NULL REFERENCES vendors(id),
    status VARCHAR(32) NOT NULL, -- ISSUED, ACKNOWLEDGED, PARTIALLY_FULFILLED, FULFILLED, CANCELLED
    total_amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS po_line_items (
    id VARCHAR(36) PRIMARY KEY,
    purchase_order_id VARCHAR(36) NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,
    pr_line_item_id VARCHAR(36) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL CHECK (unit_price > 0),
    subtotal NUMERIC(19, 4) NOT NULL CHECK (subtotal > 0)
);

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_proc_outbox_unpub ON outbox_events (published, created_at) WHERE published = FALSE;

-- Seed enterprise vendor suppliers
INSERT INTO vendors (id, code, name, tax_id, bank_account_number, bank_name, status) VALUES
('vend-infra-001', 'VEND-IND-501', 'PT Multipolar Technology Infrastructure', '01.234.567.8-012.000', '108001928374', 'Bank Mandiri', 'ACTIVE'),
('vend-cloud-002', 'VEND-IND-502', 'PT Telkom Data Ekosistem', '02.345.678.9-013.000', '882003948571', 'Bank Central Asia', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

