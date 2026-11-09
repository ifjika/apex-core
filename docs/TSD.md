# Technical Specification Document (TSD)

## APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

---

## 1. Database Architecture & Schemas

APEX-Core utilizes **MySQL 8.0** with independent logical databases to enforce bounded-context isolation within a single, memory-optimized database container (capped at 250MB RAM):

- Database `apex_hris_db`: Owned by `apex-hris-service`.
- Database `apex_procurement_db`: Owned by `apex-procurement-service`.
- Database `apex_finance_db`: Owned by `apex-finance-service`.

All tables are built with the `InnoDB` storage engine using `utf8mb4` character set and `utf8mb4_unicode_ci` collation.

---

### 1.1. Database: `apex_hris_db` (Organizational Domain)

```sql
CREATE DATABASE IF NOT EXISTS apex_hris_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE apex_hris_db;

CREATE TABLE IF NOT EXISTS departments (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cost_centers (
    id VARCHAR(36) PRIMARY KEY,
    department_id VARCHAR(36) NOT NULL,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    allocated_budget DECIMAL(19, 4) NOT NULL,
    reserved_budget DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    spent_budget DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_cost_centers_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT chk_allocated_positive CHECK (allocated_budget >= 0),
    CONSTRAINT chk_reserved_positive CHECK (reserved_budget >= 0),
    CONSTRAINT chk_spent_positive CHECK (spent_budget >= 0),
    CONSTRAINT chk_budget_consistency CHECK (reserved_budget + spent_budget <= allocated_budget)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS employees (
    id VARCHAR(36) PRIMARY KEY,
    employee_code VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    full_name VARCHAR(128) NOT NULL,
    department_id VARCHAR(36) NOT NULL,
    supervisor_id VARCHAR(36),
    approval_role VARCHAR(32) NOT NULL, -- e.g. SUPERVISOR, DEPT_MANAGER, DIVISION_DIRECTOR
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_employees_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_employees_supervisor FOREIGN KEY (supervisor_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS approval_matrices (
    id VARCHAR(36) PRIMARY KEY,
    role VARCHAR(32) NOT NULL,
    min_amount DECIMAL(19, 4) NOT NULL,
    max_amount DECIMAL(19, 4), -- NULL represents unlimited
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 1.2. Database: `apex_procurement_db` (Purchasing & Ingestion Domain)

```sql
CREATE DATABASE IF NOT EXISTS apex_procurement_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE apex_procurement_db;

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
    status VARCHAR(32) NOT NULL, -- DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ORDERED
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
    CONSTRAINT fk_pr_line_requisition FOREIGN KEY (requisition_id) REFERENCES purchase_requisitions(id) ON DELETE CASCADE,
    CONSTRAINT chk_pr_qty_pos CHECK (quantity > 0),
    CONSTRAINT chk_pr_price_pos CHECK (unit_price > 0),
    CONSTRAINT chk_pr_subtotal_pos CHECK (subtotal > 0)
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
    CONSTRAINT fk_po_vendor FOREIGN KEY (vendor_id) REFERENCES vendors(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS po_line_items (
    id VARCHAR(36) PRIMARY KEY,
    purchase_order_id VARCHAR(36) NOT NULL,
    pr_line_item_id VARCHAR(36) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(19, 4) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    CONSTRAINT fk_po_line_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT chk_po_qty_pos CHECK (quantity > 0),
    CONSTRAINT chk_po_price_pos CHECK (unit_price > 0),
    CONSTRAINT chk_po_subtotal_pos CHECK (subtotal > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload JSON NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    published_at TIMESTAMP(6) NULL,
    INDEX idx_outbox_unpub (published, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 1.3. Database: `apex_finance_db` (Settlement & Cryptographic Ledger Domain)

```sql
CREATE DATABASE IF NOT EXISTS apex_finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE apex_finance_db;

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
    CONSTRAINT fk_gr_line_gr FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipts(id) ON DELETE CASCADE,
    CONSTRAINT chk_gr_qty_non_neg CHECK (received_quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vendor_invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(64) NOT NULL UNIQUE,
    vendor_id VARCHAR(36) NOT NULL,
    purchase_order_id VARCHAR(36) NOT NULL,
    goods_receipt_id VARCHAR(36) NOT NULL,
    status VARCHAR(32) NOT NULL, -- PENDING_MATCH, MATCHED, DISCREPANCY, PAID
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
    CONSTRAINT fk_vi_line_invoice FOREIGN KEY (vendor_invoice_id) REFERENCES vendor_invoices(id) ON DELETE CASCADE,
    CONSTRAINT chk_vi_qty_pos CHECK (invoiced_quantity > 0),
    CONSTRAINT chk_vi_price_pos CHECK (unit_price > 0),
    CONSTRAINT chk_vi_subtotal_pos CHECK (subtotal > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payment_vouchers (
    id VARCHAR(36) PRIMARY KEY,
    voucher_number VARCHAR(64) NOT NULL UNIQUE,
    invoice_id VARCHAR(36) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(32) NOT NULL, -- ISSUED, DISBURSED, CANCELLED
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_voucher_invoice FOREIGN KEY (invoice_id) REFERENCES vendor_invoices(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cryptographically chained append-only general ledger
CREATE TABLE IF NOT EXISTS ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    sequence_number BIGINT AUTO_INCREMENT UNIQUE,
    transaction_reference VARCHAR(64) NOT NULL, -- e.g. Voucher ID or PO ID
    account_code VARCHAR(32) NOT NULL,          -- e.g. 1101 (Cash), 2101 (AP), 5101 (Inventory)
    entry_type VARCHAR(6) NOT NULL,             -- DEBIT, CREDIT
    amount DECIMAL(19, 4) NOT NULL,
    previous_hash VARCHAR(64) NOT NULL,
    current_hash VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_ledger_seq (sequence_number),
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 1.4. Database: `apex_finance_db` (Treasury Holding & In-House Banking)

```sql
-- Subsidiary Cash Pooling Accounts (Managed by Holding)
CREATE TABLE IF NOT EXISTS treasury_cash_pools (
    id VARCHAR(36) PRIMARY KEY,
    subsidiary_code VARCHAR(32) NOT NULL UNIQUE,
    subsidiary_name VARCHAR(128) NOT NULL,
    current_balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    yield_rate DECIMAL(5, 4) NOT NULL DEFAULT 0.0600, -- 6.00% p.a. deposit yield paid by holding
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Intercompany Working Capital Loan Contracts
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
```

---

## 2. Algorithm Designs

### 2.1. 3-Way Matching Engine Algorithm

```
Algorithm: ExecuteThreeWayMatching(PO, GR, VI, ToleranceRate = 0.005)
Input    : PurchaseOrder PO, GoodsReceipt GR, VendorInvoice VI
Output   : MatchResult (Status: MATCHED | MATCHED_WITH_TOLERANCE | DISCREPANCY, DiscrepancyDetails)

1. Initialization:
   - TotalItems = PO.LineItems.Count
   - Mismatches = List[]
   - MaxAllowedDiscrepancy = ToleranceRate * PO.TotalAmount

2. Header Consistency Check:
   - IF VI.PurchaseOrderID != PO.ID THEN RETURN DISCREPANCY("Invalid PO Reference")
   - IF VI.GoodsReceiptID != GR.ID THEN RETURN DISCREPANCY("Invalid GR Reference")

3. Iterate Over VI.LineItems:
   FOR EACH vi_item IN VI.LineItems:
       po_item = PO.FindItem(vi_item.POLineItemID)
       gr_item = GR.FindItem(vi_item.POLineItemID)

       IF po_item IS NULL OR gr_item IS NULL:
           Mismatches.Add("Unmatched line item id: " + vi_item.POLineItemID)
           CONTINUE

       // Quantity Validation: Invoiced Qty must be <= Received Qty from Warehouse
       IF vi_item.InvoicedQuantity > gr_item.ReceivedQuantity:
           Mismatches.Add(Format("Qty Mismatch on %s: Invoiced=%d, Received=%d",
                                 po_item.Name, vi_item.InvoicedQuantity, gr_item.ReceivedQuantity))

       // Price Validation: Invoiced unit price must match agreed PO price
       PriceDelta = ABS(vi_item.UnitPrice - po_item.UnitPrice)
       IF PriceDelta > 0.0001:
           Mismatches.Add(Format("Price Mismatch on %s: Invoiced=%s, Agreed=%s",
                                 po_item.Name, vi_item.UnitPrice, po_item.UnitPrice))

4. Total Amount Variance Evaluation:
   TotalDiff = ABS(VI.TotalAmount - (PO.TotalAmount + VI.TaxAmount))

   IF Mismatches.IsEmpty() AND TotalDiff == 0:
       RETURN MatchResult(MATCHED, "Full 3-Way Invariant Satisfied")
   ELSE IF Mismatches.IsEmpty() AND TotalDiff <= MaxAllowedDiscrepancy:
       RETURN MatchResult(MATCHED_WITH_TOLERANCE, "Variance within configured tolerance")
   ELSE:
       RETURN MatchResult(DISCREPANCY, Mismatches)
```

---

### 2.2. Cryptographic Hash-Chaining Algorithm (Append-Only Ledger)

Each sequential ledger entry is cryptographically linked to its predecessor:

$$\text{CurrentHash}_n = \text{SHA256}\left(\text{PreviousHash}_{n-1} + \text{Seq}_n + \text{TxnRef}_n + \text{Account}_n + \text{Type}_n + \text{Amount}_n + \text{ISO\_Timestamp}_n\right)$$

- For the genesis record ($n=1$), $\text{PreviousHash}_0 = 64 \times \text{'0'}$.
- Data integrity is verified continuously by re-computing the hash sequence from record 1 to record $n$.

---

## 3. Internal Interfaces & Java 21 Record Types

```java
package com.apex.common.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Immutable Money Value Object with explicit scale
public record Money(BigDecimal amount, String currency) {
    public Money {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency must not be null");
        }
        amount = amount.setScale(4, java.math.RoundingMode.HALF_EVEN);
    }

    public static Money of(String amountStr, String currency) {
        return new Money(new BigDecimal(amountStr), currency);
    }
}

// Universal CloudEvents Payload Envelope
public record EventEnvelope<T>(
    UUID id,
    String source,
    String type,
    Instant time,
    T data
) {}

// 3-Way Match Evaluation Record
public record ThreeWayMatchEvaluation(
    String purchaseOrderId,
    String goodsReceiptId,
    String invoiceId,
    boolean isQuantityMatched,
    boolean isPriceMatched,
    BigDecimal totalVariance,
    MatchStatus resultStatus
) {
    public enum MatchStatus {
        MATCHED,
        MATCHED_WITH_TOLERANCE,
        DISCREPANCY
    }
}
```
