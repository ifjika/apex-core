This document defines the roadmap, phased feature implementation matrix, and the active task checklist for the feature currently being implemented.

## 1. Feature Implementation Matrix

| Phase | Module & Feature | Associated Tech Stack | Status | Definition of Done |
| :---: | :--- | :--- | :---: | :--- |
| **1** | **Environment Setup & Multi-Module Workspace** | Java 21, Maven Multi-Module, Docker Compose, MySQL 8.0, Redis 7, Redpanda | 🟢 Completed | All database, cache, and broker containers configured under 600MB RAM; Maven monorepo cleanly initialized. |
| **2** | **Database Schema Design & Multi-Database Migrations** | MySQL 8.0, Isolated Schemas (`apex_hris_db`, `apex_procurement_db`, `apex_finance_db`), Flyway 10 | 🟢 Completed | Flyway migration schemas created with relational constraints, `@Version` locking columns, and initial corporate seeds. |
| **3** | **Common Core Engine & Domain Contracts** | Java 21 Records, `Money` (`HALF_EVEN`), CloudEvents 1.0, RFC 7807, Redis `SETNX` | 🟢 Completed | Immutable monetary value object with zero float drift; generic Transactional Outbox; Redis atomic idempotency shield. |
| **4** | **HRIS Organizational Core: Dynamic Approval Limits** | Spring Boot 3.3, Spring Data JPA, Virtual Threads, Hierarchical Matrix | 🟢 Completed | Recursive supervisor chain climbing evaluates discretionary limits; Cost Center budget reservations with `@Version` concurrency defense. |
| **5** | **Procurement Lifecycle: PR-to-PO State Machine & Outbox** | Spring Boot 3.3, State Machine, Transactional Outbox, Redpanda / Kafka | 🟢 Completed | Requisition FSM (`DRAFT` $\to$ `PENDING_APPROVAL` $\to$ `APPROVED` $\to$ `ORDERED`); background poller dispatches events to Kafka with partition keys. |
| **6** | **Financial Settlement: Automated 3-Way Matching Engine** | Spring Boot 3.3, 3-Way Match Algorithm, Decimal Tolerance Math, Accounts Payable | 🟢 Completed | Mathematical invariant $(\text{Qty}_{\text{PO}} = \text{Qty}_{\text{GR}} = \text{Qty}_{\text{VI}}) \land (\text{Price}_{\text{PO}} = \text{Price}_{\text{VI}})$ enforced; auto Payment Voucher creation. |
| **7** | **Cryptographic General Ledger: Append-Only SHA-256 Hash Chain** | Java 21, SHA-256 Digest, HexFormat, Append-Only Double-Entry GL | 🟢 Completed | Sequential hash links $\text{CurrentHash}_n = \text{SHA256}(\text{PreviousHash}_{n-1} \parallel \dots)$ enforced; `/verify-chain` endpoint detects database tampering. |
| **8** | **Defensive API Gateway & Ingress Security** | Spring Cloud Gateway, Reactive Redis, JJWT (RS256), Token Bucket Rate Limiter | 🟢 Completed | Reverse proxy routes to microservices; Bearer JWT claims validated and enriched; 100 req/min rate limit enforced. |
| **9** | **CI/CD Pipeline, Testcontainers & Concurrency Benchmarking** | GitHub Actions, Testcontainers (Kafka + MySQL 8.0), k6 Concurrency (1,000 RPS) | 🟢 Completed | Automated CI pipeline executes build & unit tests; Testcontainers guide and k6 benchmark script validating 1,000 concurrent approvals. |
| **10** | **Treasury Holding & In-House Banking Engine** | Spring Boot 3.3, MySQL 8.0, SHA-256 Ledger, Liquidity Sweeping, React 18 | 🟢 Completed | Cash pooling across subsidiaries, intercompany loan origination with 2.50% Net Interest Margin (NIM), and double-entry GL ledger posting. |
| **11** | **Enterprise Multi-Module UI Architecture & Component Modularization** | React 18, TypeScript, Vite, Tailwind CSS, Headless Hooks, AGENT.md Strict Limits | 🟢 Completed | Monolithic App.tsx refactored into Pattern A tabbed shell; 4 isolated domain modules (Treasury, Procurement FSM, HRIS Hierarchy, Finance SHA-256 Ledger) with strict <=150 LOC per component. |

---

## 2. Active Task Checklist (Current In-Progress Feature)

> [!IMPORTANT]
> **Checklist Usage Rules:** This section ONLY contains the micro-steps for the feature CURRENTLY in progress. Once all steps below are confirmed and verified, this checklist is cleared, and replaced with the micro-steps of the next feature according to the implementation table above.

### Active Phase: Phase 11 - Enterprise Multi-Module UI Architecture & Component Modularization

- [x] **11.1 Shell & Navigation Layout** in `apex-ui/src/components/layout/`:
  - Implemented `TabNavigation.tsx` (49 LOC) and `AppLayout.tsx` (52 LOC) with active module indicator and JWT token status.
  - Refactored `App.tsx` (19 LOC) from a 450-line monolith into a declarative root shell.

- [x] **11.2 Headless Custom Hooks & API Alignment** in `apex-ui/src/hooks/` and `src/api/`:
  - `useTreasury.ts` (100 LOC) — Liquidity sweeping, loan origination, and live NIM margin calculations.
  - `useProcurement.ts` (122 LOC) — PR creation, state transitions, and outbox simulation.
  - `useHris.ts` (119 LOC) — Recursive supervisor hierarchy climbing and cost center budget monitoring.
  - `useLedger.ts` (98 LOC) — SHA-256 hash-chain verification and double-entry tracking.
  - Fixed contract mismatch in `procurementApi.ts` (`@RequestBody ApprovalActionRequest`).
  - Fixed verification response mismatch in `types/finance.ts` (`isTamperFree`, `totalEntriesVerified`).

- [x] **11.3 Treasury & In-House Banking Module** in `apex-ui/src/components/treasury/`:
  - `CashPoolList.tsx` (66 LOC) — Subsidiary cash pool balances and yield rates.
  - `LoanSimulatorCard.tsx` (128 LOC) — Interactive working capital disbursement simulator with 2.50% NIM spread.
  - `TreasuryDashboard.tsx` (84 LOC) — Treasury KPI metrics and alert banners.

- [x] **11.4 Procurement Portal & FSM Workflow** in `apex-ui/src/components/procurement/`:
  - `RequisitionTable.tsx` (90 LOC) — Finite State Machine status badges (`DRAFT` -> `PENDING` -> `APPROVED` -> `ORDERED`) and contextual actions.
  - `CreatePrModal.tsx` (120 LOC) — Zero-trust validated PR submission form with multi-line item totals.
  - `ProcurementPortal.tsx` (61 LOC) — Procurement container with commitment metrics.

- [x] **11.5 HRIS & Budget Encumbrance Directory** in `apex-ui/src/components/hris/`:
  - `CostCenterBudget.tsx` (63 LOC) — Budget encumbrance progress bars (Allocated vs Reserved vs Spent).
  - `OrgHierarchyTree.tsx` (115 LOC) — Interactive approval limit escalator simulator.
  - `HrisDirectory.tsx` (26 LOC) — Module container.

- [x] **11.6 Cryptographic Ledger & Audit Explorer** in `apex-ui/src/components/finance/`:
  - `HashChainVisualizer.tsx` (75 LOC) — Visual cryptographic block linkage displaying `PrevHash` -> `CurrentHash` SHA-256 digests.
  - `LedgerAuditView.tsx` (55 LOC) — Interactive chain verification button and tamper-evidence status banner.

- [x] **11.7 Production Build Verification & Git Hygiene**:
  - TypeScript build (`tsc -b && vite build`) passed with 100% zero errors in 15s.
  - Added `*.tsbuildinfo` to root and frontend `.gitignore`. All 15 components confirmed $\le$ 128 LOC (strictly adhering to AGENT.md Section 7 $\le$ 150-200 LOC limit).
