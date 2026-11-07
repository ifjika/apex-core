# Application Flows, State Machines & Screen Navigation

## APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

---

## 1. End-to-End Business User Flows

### 1.1. Procurement-to-Pay (P2P) Lifecycle

```mermaid
sequenceDiagram
    autonumber
    actor Requester as Department Requester
    actor Approver as Line Approver (HRIS)
    actor Buyer as Procurement Buyer
    actor Receiver as Warehouse Receiver
    actor AP as Accounts Payable Specialist
    participant Gateway as API Gateway (8080)
    participant ProcSvc as Procurement Service (8082)
    participant HRISSvc as HRIS Service (8081)
    participant FinSvc as Finance Service (8083)
    participant Kafka as Redpanda Broker

    %% Phase 1: Requisition & Approval
    Requester->>Gateway: POST /requisitions (DRAFT)
    Gateway->>ProcSvc: Create PR
    ProcSvc->>Kafka: PRSubmittedEvent
    Kafka->>HRISSvc: Calculate Approval Route Matrix
    HRISSvc-->>ProcSvc: ApprovalRouteAssigned
    Approver->>Gateway: POST /requisitions/{id}/approve
    Gateway->>ProcSvc: Approve Requisition
    ProcSvc->>Kafka: PRApprovedEvent

    %% Phase 2: PO Issuance & Delivery
    Buyer->>Gateway: POST /purchase-orders (From PR)
    Gateway->>ProcSvc: Issue PO
    ProcSvc->>Kafka: POIssuedEvent
    Kafka->>FinSvc: Cache PO Commitment & Budget Accrual

    Receiver->>Gateway: POST /goods-receipts
    Gateway->>FinSvc: Record Goods Receipt (GR)
    FinSvc->>Kafka: GoodsReceivedEvent

    %% Phase 3: Invoicing, 3-Way Match & Settlement
    AP->>Gateway: POST /invoices (Upload Vendor Invoice)
    Gateway->>FinSvc: Ingest Invoice
    FinSvc->>FinSvc: Execute Automated 3-Way Match (PO == GR == VI)
    alt 3-Way Match Success
        FinSvc->>FinSvc: Generate Payment Voucher
        FinSvc->>FinSvc: Append-Only SHA-256 Ledger Entry
        FinSvc->>Kafka: PaymentVoucherIssuedEvent
    else Discrepancy Found
        FinSvc->>FinSvc: Flag Status as DISCREPANCY
        FinSvc-->>AP: Require Manual Variance Resolution
    end
```

---

## 2. State Machine Specifications

### 2.1. Purchase Requisition (PR) State Machine

```mermaid
stateDiagram-v2
    [*] --> DRAFT : Create Requisition
    DRAFT --> PENDING_APPROVAL : Submit for Review
    DRAFT --> CANCELLED : Cancelled by User

    state PENDING_APPROVAL {
        [*] --> LEVEL_1_REVIEW : Route to Supervisor
        LEVEL_1_REVIEW --> LEVEL_2_REVIEW : Approved & Amount > Limit 1
        LEVEL_2_REVIEW --> LEVEL_3_REVIEW : Approved & Amount > Limit 2
        LEVEL_1_REVIEW --> FULLY_APPROVED : Approved & Amount <= Limit 1
        LEVEL_2_REVIEW --> FULLY_APPROVED : Approved & Amount <= Limit 2
        LEVEL_3_REVIEW --> FULLY_APPROVED : Approved
    }

    PENDING_APPROVAL --> REJECTED : Any Approver Rejects
    PENDING_APPROVAL --> CANCELLED : Revoked by Requester

    FULLY_APPROVED --> ORDERED : Converted to Purchase Order

    REJECTED --> DRAFT : Edit and Resubmit
    CANCELLED --> [*]
    ORDERED --> [*]
```

---

### 2.2. Purchase Order (PO) State Machine

```mermaid
stateDiagram-v2
    [*] --> ISSUED : Created from Approved PR
    ISSUED --> ACKNOWLEDGED : Vendor Confirms Order
    ISSUED --> CANCELLED : Cancelled before vendor confirmation

    ACKNOWLEDGED --> PARTIALLY_RECEIVED : Partial Goods Receipt Recorded
    ACKNOWLEDGED --> FULLY_RECEIVED : Full Goods Receipt Recorded
    PARTIALLY_RECEIVED --> FULLY_RECEIVED : Remaining Goods Received

    FULLY_RECEIVED --> INVOICED : Vendor Invoice Submitted
    INVOICED --> CLOSED : Payment Voucher Released & Settled

    CANCELLED --> [*]
    CLOSED --> [*]
```

---

### 2.3. 3-Way Matching Engine State Machine

```mermaid
stateDiagram-v2
    [*] --> PENDING_DATA : Invoice Ingested
    PENDING_DATA --> AWAITING_GR : PO Valid, Waiting GR
    PENDING_DATA --> EVALUATING : PO & GR Available
    AWAITING_GR --> EVALUATING : GR Received

    state EVALUATING {
        [*] --> CHECK_QUANTITIES
        CHECK_QUANTITIES --> CHECK_PRICES : Qty Matched (PO == GR == VI)
        CHECK_QUANTITIES --> DISCREPANCY : Invoiced Qty > Received Qty
        CHECK_PRICES --> MATCHED : Price Exactly Equal
        CHECK_PRICES --> TOLERANCE_MATCHED : Delta <= 0.5% (Configured)
        CHECK_PRICES --> DISCREPANCY : Delta > Tolerance
    }

    MATCHED --> VOUCHER_GENERATED : Auto-create Payment Voucher
    TOLERANCE_MATCHED --> VOUCHER_GENERATED : Auto-create with variance note
    DISCREPANCY --> DISCREPANCY_REVIEW : Sent to AP Specialist Desk

    DISCREPANCY_REVIEW --> VOUCHER_GENERATED : Manager Manual Override
    DISCREPANCY_REVIEW --> REJECTED_INVOICE : Returned to Vendor

    VOUCHER_GENERATED --> POSTED_TO_LEDGER : Chained to Double-Entry GL
    POSTED_TO_LEDGER --> [*]
```

---

## 3. Screen & Navigation Sitemap

```
APEX-Core Web Application
├── /login                              # Enterprise SSO / JWT Login
├── /dashboard                          # Executive Overview, Approvals KPI, Budget Status
│
├── /procurement                        # Procurement Module
│   ├── /requisitions                   # Purchase Requisition List (Filter by Status, Dept)
│   │   ├── /new                        # Create Requisition Form (Line items grid, Cost Center selector)
│   │   └── /{id}                       # PR Details, Approval Timeline, Activity Log
│   ├── /orders                         # Purchase Orders List (PO)
│   │   └── /{id}                       # PO Details, Contract PDF, Delivery Progress
│   └── /vendors                        # Vendor Directory, Tax Compliance, Rating
│
├── /warehouse                          # Warehouse & Logistics Module
│   ├── /goods-receipts                 # Historical Goods Receipts (GR)
│   └── /goods-receipts/new             # Physical Verification & Delivery Order Intake Form
│
├── /finance                            # Finance & Accounts Payable Module
│   ├── /invoices                       # Inbound Vendor Invoices
│   │   ├── /upload                     # Upload Invoice (PDF / Manual Intake / OCR)
│   │   └── /{id}/match-view            # 3-Way Match Visual Comparison (PO vs GR vs Invoice)
│   ├── /payment-vouchers               # Approved Payment Vouchers
│   └── /general-ledger                 # Double-Entry General Ledger
│       └── /verify                     # Cryptographic Hash Chain Verification Screen
│
└── /organization                       # HR & Governance Module (Admin/Approver)
    ├── /employees                      # Employee Directory & Supervisor Relationships
    ├── /cost-centers                   # Cost Center Budgets & Real-time Utilization
    └── /approval-matrices              # Approval Limit Threshold Configuration
```
