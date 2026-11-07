# Technical Specification Document (TSD)

## APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

---

## 1. Database Architecture & Schemas

APEX-Core utilizes PostgreSQL 16 with **Logical Schemas** to maintain bounded-context isolation within a single, memory-efficient database container (memory limit: 200MB):

- Schema `hris`: Owned by `apex-hris-service`.
- Schema `procurement`: Owned by `apex-procurement-service`.
- Schema `finance`: Owned by `apex-finance-service`.

---

### 1.1. Schema: `hris` (Organizational Domain)

```sql
CREATE SCHEMA IF NOT EXISTS hris;

CREATE TABLE hris.departments (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hris.cost_centers (
    id VARCHAR(36) PRIMARY KEY,
    department_id VARCHAR(36) NOT NULL REFERENCES hris.departments(id),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    allocated_budget NUMERIC(19, 4) NOT NULL CHECK (allocated_budget >= 0),
    reserved_budget NUMERIC(19, 4) NOT NULL DEFAULT 0.0000 CHECK (reserved_budget >= 0),
    spent_budget NUMERIC(19, 4) NOT NULL DEFAULT 0.0000 CHECK (spent_budget >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_budget_consistency CHECK (reserved_budget + spent_budget <= allocated_budget)
);

CREATE TABLE hris.employees (
    id VARCHAR(36) PRIMARY KEY,
    employee_code VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    full_name VARCHAR(128) NOT NULL,
    department_id VARCHAR(36) NOT NULL REFERENCES hris.departments(id),
    supervisor_id VARCHAR(36) REFERENCES hris.employees(id),
    approval_role VARCHAR(32) NOT NULL, -- e.g. SUPERVISOR, DEPT_MANAGER, DIVISION_DIRECTOR
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE hris.approval_matrices (
    id VARCHAR(36) PRIMARY KEY,
    role VARCHAR(32) NOT NULL,
    min_amount NUMERIC(19, 4) NOT NULL,
    max_amount NUMERIC(19, 4), -- NULL represents unlimited
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

### 1.2. Schema: `procurement` (Purchasing & Ingestion Domain)

```sql
CREATE SCHEMA IF NOT EXISTS procurement;

CREATE TABLE procurement.vendors (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    tax_id VARCHAR(64) NOT NULL,
    bank_account_number VARCHAR(64) NOT NULL,
    bank_name VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE procurement.purchase_requisitions (
    id VARCHAR(36) PRIMARY KEY,
    pr_number VARCHAR(64) NOT NULL UNIQUE,
    cost_center_id VARCHAR(36) NOT NULL,
    requester_id VARCHAR(36) NOT NULL,
    current_approver_id VARCHAR(36),
    status VARCHAR(32) NOT NULL, -- DRAFT, PENDING_APPROVAL, APPROVED, REJECTED, ORDERED
    total_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    rejection_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE procurement.pr_line_items (
    id VARCHAR(36) PRIMARY KEY,
    requisition_id VARCHAR(36) NOT NULL REFERENCES procurement.purchase_requisitions(id) ON DELETE CASCADE,
    item_name VARCHAR(128) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL CHECK (unit_price > 0),
    subtotal NUMERIC(19, 4) NOT NULL CHECK (subtotal > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE procurement.purchase_orders (
    id VARCHAR(36) PRIMARY KEY,
    po_number VARCHAR(64) NOT NULL UNIQUE,
    requisition_id VARCHAR(36) NOT NULL,
    vendor_id VARCHAR(36) NOT NULL REFERENCES procurement.vendors(id),
    status VARCHAR(32) NOT NULL, -- ISSUED, ACKNOWLEDGED, PARTIALLY_FULFILLED, FULFILLED, CANCELLED
    total_amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'IDR',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE procurement.po_line_items (
    id VARCHAR(36) PRIMARY KEY,
    purchase_order_id VARCHAR(36) NOT NULL REFERENCES procurement.purchase_orders(id) ON DELETE CASCADE,
    pr_line_item_id VARCHAR(36) NOT NULL,
    item_name VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL CHECK (unit_price > 0),
    subtotal NUMERIC(19, 4) NOT NULL CHECK (subtotal > 0)
);

CREATE TABLE procurement.outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(128) NOT NULL,
    payload JSONB NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    retry_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMPTZ
);
CREATE INDEX idx_proc_outbox_unpub ON procurement.outbox_events (published, created_at) WHERE published = FALSE;
```

---

### 1.3. Schema: `finance` (Settlement & Cryptographic Ledger Domain)

```sql
CREATE SCHEMA IF NOT EXISTS finance;

CREATE TABLE finance.goods_receipts (
    id VARCHAR(36) PRIMARY KEY,
    gr_number VARCHAR(64) NOT NULL UNIQUE,
    purchase_order_id VARCHAR(36) NOT NULL,
    received_by VARCHAR(36) NOT NULL,
    received_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE finance.gr_line_items (
    id VARCHAR(36) PRIMARY KEY,
    goods_receipt_id VARCHAR(36) NOT NULL REFERENCES finance.goods_receipts(id) ON DELETE CASCADE,
    po_line_item_id VARCHAR(36) NOT NULL,
    received_quantity INTEGER NOT NULL CHECK (received_quantity >= 0)
);

CREATE TABLE finance.vendor_invoices (
    id VARCHAR(36) PRIMARY KEY,
    invoice_number VARCHAR(64) NOT NULL UNIQUE,
    vendor_id VARCHAR(36) NOT NULL,
    purchase_order_id VARCHAR(36) NOT NULL,
    goods_receipt_id VARCHAR(36) NOT NULL,
    status VARCHAR(32) NOT NULL, -- PENDING_MATCH, MATCHED, DISCREPANCY, PAID
    tax_amount NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    total_amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE finance.vi_line_items (
    id VARCHAR(36) PRIMARY KEY,
    vendor_invoice_id VARCHAR(36) NOT NULL REFERENCES finance.vendor_invoices(id) ON DELETE CASCADE,
    po_line_item_id VARCHAR(36) NOT NULL,
    invoiced_quantity INTEGER NOT NULL CHECK (invoiced_quantity > 0),
    unit_price NUMERIC(19, 4) NOT NULL CHECK (unit_price > 0),
    subtotal NUMERIC(19, 4) NOT NULL CHECK (subtotal > 0)
);

CREATE TABLE finance.payment_vouchers (
    id VARCHAR(36) PRIMARY KEY,
    voucher_number VARCHAR(64) NOT NULL UNIQUE,
    invoice_id VARCHAR(36) NOT NULL REFERENCES finance.vendor_invoices(id),
    amount NUMERIC(19, 4) NOT NULL,
    status VARCHAR(32) NOT NULL, -- ISSUED, DISBURSED, CANCELLED
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cryptographically chained append-only general ledger
CREATE TABLE finance.ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    sequence_number BIGSERIAL NOT NULL UNIQUE,
    transaction_reference VARCHAR(64) NOT NULL, -- e.g. Voucher ID or PO ID
    account_code VARCHAR(32) NOT NULL,          -- e.g. 1101 (Cash), 2101 (AP)
    entry_type VARCHAR(6) NOT NULL CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    previous_hash VARCHAR(64) NOT NULL,
    current_hash VARCHAR(64) NOT NULL,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_ledger_seq ON finance.ledger_entries(sequence_number);
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
