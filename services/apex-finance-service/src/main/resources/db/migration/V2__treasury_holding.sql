-- =============================================================================
-- APEX-Core Finance Schema Migration V2: Treasury Holding & In-House Banking
-- =============================================================================

-- 1. Subsidiary Cash Pooling Accounts (Managed by Holding)
CREATE TABLE IF NOT EXISTS treasury_cash_pools (
    id VARCHAR(36) PRIMARY KEY,
    subsidiary_code VARCHAR(32) NOT NULL UNIQUE,
    subsidiary_name VARCHAR(128) NOT NULL,
    current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    yield_rate DECIMAL(5, 4) NOT NULL DEFAULT 0.0600, -- 6.00% p.a. deposit yield paid by holding
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Intercompany Working Capital Loan Contracts
CREATE TABLE IF NOT EXISTS intercompany_loans (
    id VARCHAR(36) PRIMARY KEY,
    loan_reference VARCHAR(64) NOT NULL UNIQUE,
    borrower_subsidiary VARCHAR(32) NOT NULL,
    lender_subsidiary VARCHAR(32) NOT NULL,
    purchase_requisition_id VARCHAR(36),
    principal_amount DECIMAL(19, 4) NOT NULL,
    interest_rate DECIMAL(5, 4) NOT NULL DEFAULT 0.0850, -- 8.50% p.a. charged to borrower
    spread_rate DECIMAL(5, 4) NOT NULL DEFAULT 0.0250,   -- 2.50% p.a. Net Interest Margin (NIM) to holding
    tenor_months INT NOT NULL DEFAULT 6,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    total_interest_projected DECIMAL(19, 4) NOT NULL,
    holding_margin_projected DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_loan_principal CHECK (principal_amount > 0),
    CONSTRAINT chk_loan_tenor CHECK (tenor_months > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed Initial Corporate Group Entities in Cash Pool
INSERT INTO treasury_cash_pools (id, subsidiary_code, subsidiary_name, current_balance, yield_rate)
VALUES 
    ('pool-agro-001', 'SUB-AGRO', 'PT Apex Agro Lestari (Surplus Entity)', 50000000000.0000, 0.0600),
    ('pool-mfg-002', 'SUB-MFG', 'PT Apex Manufaktur Teknik (Deficit Entity)', 2500000000.0000, 0.0600)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP(6);

