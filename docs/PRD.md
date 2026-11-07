# Product Requirements Document (PRD)
## APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

---

## 1. Document Overview & Metadata
* **Product Name:** APEX-Core (*Asynchronous Procurement & Enterprise Exchange Core*)
* **Version:** 1.0.0
* **Status:** Active Engineering
* **Target Audience:** Engineering Team, Enterprise Architects, Product Managers, Financial & Compliance Auditors
* **Core Stack:** Java 21, Spring Boot 3.3+, PostgreSQL 16, Redpanda (Kafka Protocol), Redis 7, Spring Cloud Gateway

---

## 2. Executive Summary & Problem Statement

### 2.1. Background
In medium-to-large enterprises, the Procurement-to-Pay (P2P) pipeline frequently suffers from procedural friction, rogue expenditure (*maverick spending*), and vulnerability to accounting audit failure. Typical pain points include:
1. **Siloed & Sluggish Approvals:** Lack of automated organizational approval matrix resolution leads to misrouted or stalled procurement requisitions.
2. **Payment Fraud & Duplicate Billing:** Discrepancies between what was purchased (PO), what was physically received at the warehouse (GR), and what the vendor billed (VI) go undetected due to manual reconciliations.
3. **Data Tampering & Non-Repudiation Failures:** General Ledger records remain vulnerable to direct database manipulation without cryptographic proofs.
4. **Distributed Transaction Anomalies:** Traditional microservices architectures suffer from *dual-write anomalies*, where database changes succeed but message broker notifications fail.

### 2.2. Product Vision
APEX-Core provides an event-driven enterprise P2P microservices engine that guarantees financial data consistency, automated 3-Way Matching, append-only hash-chained general ledgers, and high-throughput concurrency while running efficiently on local workstations (*zero-cost infrastructure*).

---

## 3. User Personas & Roles

| Role Code | Role Name | Description & Primary Responsibilities |
| :--- | :--- | :--- |
| `ROLE_REQUESTER` | Department Requester | Employees creating Purchase Requisitions (PR) tied to specific Cost Centers. |
| `ROLE_APPROVER` | Line Approver / Executive | Authorized managers (Supervisor, Manager, Director) reviewing and approving PRs based on authority limits. |
| `ROLE_BUYER` | Procurement Officer | Buyers managing vendor master data, creating Purchase Orders (PO), and monitoring fulfillment. |
| `ROLE_RECEIVER` | Warehouse Inspector | Logistics staff verifying incoming physical items and recording Goods Receipts (GR). |
| `ROLE_AP_FINANCE` | Accounts Payable Specialist | Finance staff ingesting Vendor Invoices (VI), monitoring 3-Way Matching, and releasing Payment Vouchers. |
| `ROLE_AUDITOR` | Compliance & Internal Auditor | Auditors verifying cryptographic hash-chain continuity across all ledger transactions. |
| `ROLE_SYSADMIN` | System Administrator | Platform administrator managing service health, API Gateway routes, and credentials. |

---

## 4. User Stories & Acceptance Criteria

### US-01: Purchase Requisition (PR) Submission
* **As a:** `ROLE_REQUESTER`
* **I want to:** Submit a procurement requisition specifying Cost Center and line items.
* **So that:** My departmental procurement requests are handled through official corporate channels.
* **Acceptance Criteria:**
  1. The form requires `cost_center_id`, required date, currency, and at least one `pr_line_item` (item name, quantity $>0$, unit price $>0$).
  2. The system computes `total_estimated_amount` using `BigDecimal` without binary floating-point rounding.
  3. The initial status is `DRAFT`. Upon submission, status shifts to `PENDING_APPROVAL` and emits an event to HRIS for route calculation.

### US-02: Dynamic Approval Routing & Authority Validation
* **As a:** `ROLE_APPROVER`
* **I want to:** Approve or reject procurement requests according to my monetary authority limits.
* **So that:** Corporate expenditure remains controlled and compliant with internal governance.
* **Acceptance Criteria:**
  1. HRIS evaluates the approval hierarchy:
     * Amount $\le$ IDR 10,000,000: Requires Supervisor level.
     * IDR 10,000,000 $<$ Amount $\le$ IDR 50,000,000: Requires Department Manager.
     * Amount $>$ IDR 50,000,000: Requires Division Director.
  2. If an approver executes a rejection (`REJECT`), the document transitions to `REJECTED` with a mandatory reason, triggering a Saga Compensating Transaction to release budget reservations.
  3. Employs JPA `@Version` Optimistic Locking to prevent concurrent dual approvals.

### US-03: Purchase Order (PO) Issuance
* **As a:** `ROLE_BUYER`
* **I want to:** Convert approved PRs into official Purchase Orders dispatched to registered vendors.
* **So that:** Vendors receive contractual orders and begin fulfillment.
* **Acceptance Criteria:**
  1. POs can only be generated from `APPROVED` PRs.
  2. The target vendor must have an `ACTIVE` status and a valid Tax Identification Number (NPWP).
  3. Generated POs receive a unique identifier matching `PO-{YEAR}-{MONTH}-{SEQUENCE}`.
  4. The `PO_ISSUED` event is published to Redpanda via the Transactional Outbox.

### US-04: Warehouse Goods Receipt (GR) Verification
* **As a:** `ROLE_RECEIVER`
* **I want to:** Record physical receipt of goods against active POs.
* **So that:** The finance department has verified proof of goods intake before processing payments.
* **Acceptance Criteria:**
  1. The receiver looks up POs in `ORDERED` or `PARTIALLY_FULFILLED` status.
  2. The received quantity is recorded per line item.
  3. Total received quantity cannot exceed the ordered quantity on the PO (0% default tolerance).
  4. System records the GR with a unique identifier and emits `GOODS_RECEIVED`.

### US-05: Vendor Invoice Processing & Automated 3-Way Matching
* **As a:** `ROLE_AP_FINANCE`
* **I want to:** Upload vendor invoices and let the engine automatically reconcile them against PO and GR data.
* **So that:** Payments are only disbursed for verified goods received at agreed prices.
* **Acceptance Criteria:**
  1. The 3-Way Matching Engine evaluates the three invariants:
     $$\text{Status Valid} \iff (\text{Qty}_{\text{PO}} = \text{Qty}_{\text{GR}} = \text{Qty}_{\text{VI}}) \land (\text{Price}_{\text{PO}} = \text{Price}_{\text{VI}})$$
  2. If the conditions are met, invoice status transitions to `MATCHED` and automatically generates a draft `PaymentVoucher`.
  3. If quantity or price variances exist:
     * Variance $\le$ configured tolerance (e.g. 0.5% VAT rounding): Flagged as `MATCHED_WITH_TOLERANCE`.
     * Variance $>$ tolerance: Marked as `DISCREPANCY` and routed for manual review.

### US-06: Cryptographic Append-Only Double-Entry General Ledger
* **As a:** `ROLE_AUDITOR` / `ROLE_AP_FINANCE`
* **I want to:** Have every financial event recorded into a balanced double-entry ledger linked via SHA-256 hash chains.
* **So that:** Historical entries cannot be modified or deleted without immediate cryptographic detection.
* **Acceptance Criteria:**
  1. Total Debits must equal Total Credits for every voucher transaction batch.
  2. Each `LedgerEntry` record stores `previous_hash`, `current_hash`, and sequential nonce.
  3. Formula: $\text{Hash}_n = \text{SHA256}(\text{Hash}_{n-1} \parallel \text{EntryID} \parallel \text{Account} \parallel \text{Amount} \parallel \text{Timestamp})$.
  4. Provides a public verification endpoint: `/api/v1/finance/ledger/verify-chain`.

---

## 5. Target Metrics & Key Performance Indicators (KPIs)

| Metric Category | Performance Indicator | Target KPI | Measurement Mechanism |
| :--- | :--- | :--- | :--- |
| **Latency** | P95 API Response Time | $\le 200 \text{ ms}$ | Spring Boot Actuator + Prometheus |
| **Latency** | P99 API Response Time | $\le 500 \text{ ms}$ | Spring Boot Actuator + Prometheus |
| **Throughput** | Concurrent Approval Requests | $\ge 1,000 \text{ RPS}$ without deadlocks | k6 Load Test Suite |
| **Financial Accuracy** | Floating-Point Drift | $0.00\%$ (Zero Error) | Automated Regression Suite |
| **Automation** | 3-Way Match Auto-Pass Rate | $\ge 90\%$ of valid invoices | Finance Analytics Module |
| **Reliability** | Event Loss Rate | $0.00\%$ (At-Least-Once Outbox) | Outbox Audit vs Kafka Offsets |
| **Memory Footprint** | Local Dev Environment RAM | $\le 2.0 \text{ GB RAM}$ | Docker stats & JVM Heap Metrics |

---

## 6. Functional Feature Breakdown

```
APEX-Core Platform
 ├── HRIS & Organizational Governance
 │    ├── Multi-tier Employee Registry & Hierarchy
 │    ├── Dynamic Cost Center Budget Allocation
 │    └── Approval Matrix Configuration Engine
 ├── Procurement Management (P2P Engine)
 │    ├── Purchase Requisition (PR) Lifecycle Management
 │    ├── Multi-level Approval Workflow State Machine
 │    ├── Vendor Master & Tax Identification Registry
 │    └── Purchase Order (PO) Contract Generation
 ├── Warehouse & Logistics Integration
 │    ├── Goods Receipt (GR) Tracking
 │    └── Over-delivery & Duplicate Receipt Protection
 ├── Financial Settlement & Accounts Payable
 │    ├── Automated 3-Way Matching Engine (PO-GR-VI)
 │    ├── Discrepancy Flagging & Variance Resolution Workflow
 │    ├── Payment Voucher Auto-Generation
 │    └── Tamper-evident Double-Entry Cryptographic General Ledger
 └── Cross-Cutting Infrastructure
      ├── Transactional Outbox Event Dispatcher
      ├── Redis Distributed Lock & Idempotent Consumer Filter
      └── Centralized API Gateway with RBAC & Rate Limiting
```

---

## 7. System Constraints & Dependencies
* **Operating Environment:** Runs on standard workstations (8 GB RAM via Docker Compose) and cloud container clusters.
* **Core Dependencies:**
  * Database: PostgreSQL 16+.
  * Message Broker: Apache Kafka protocol compatible (Redpanda v24+).
  * Distributed Cache: Redis 7+.
  * Java Runtime: OpenJDK 21 LTS (Virtual Threads enabled).
