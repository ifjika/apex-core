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

---

## 2. Active Task Checklist (Current In-Progress Feature)

> [!IMPORTANT]
> **Checklist Usage Rules:** This section ONLY contains the micro-steps for the feature CURRENTLY in progress. Once all steps below are confirmed and verified, this checklist is cleared, and replaced with the micro-steps of the next feature according to the implementation table above.

### Active Phase: Phase 10 - Treasury Holding & In-House Banking Engine

- [x] **10.1 Database Schema Migration for Treasury Holding** in `services/apex-finance-service/src/main/resources/db/migration/V2__treasury_holding.sql`:
  - Define `treasury_cash_pools` table tracking subsidiary liquidity balances and 6.00% p.a. deposit yields.
  - Define `intercompany_loans` table recording principal amounts, 8.50% p.a. borrowing rates, 2.50% p.a. holding spread margins (NIM), and tenors.
  - Seed initial conglomerate entities (`SUB-AGRO` surplus lender with IDR 50B, `SUB-MFG` deficit borrower).

- [x] **10.2 Domain Entities & Repositories** in `services/apex-finance-service/src/main/java/com/apex/finance/domain/`:
  - Implement `TreasuryCashPool.java` with atomic `deposit()` and `withdraw()` liquidity methods.
  - Implement `IntercompanyLoan.java` calculating total interest and holding spread margin projections via `HALF_EVEN` rounding.
  - Create `TreasuryCashPoolRepository.java` and `IntercompanyLoanRepository.java`.

- [x] **10.3 Domain Service & Cryptographic Ledger Integration** in `services/apex-finance-service/src/main/java/com/apex/finance/service/TreasuryHoldingService.java`:
  - Implement `originateLoan()` validating available liquidity in lender pool, generating contract, and posting balanced double-entry to SHA-256 ledger (`1105-INTERCOMPANY-LOAN-RECEIVABLE` / `1001-HOLDING-CASH-POOL`).
  - Implement `accrueMonthlyInterest()` recording Holding's net interest margin revenue into ledger (`4105-INTERCOMPANY-INTEREST-INCOME`).
  - Implement `getTreasurySummary()` returning aggregate pool liquidity and projected annual holding income.

- [x] **10.4 REST API Controller** in `services/apex-finance-service/src/main/java/com/apex/finance/controller/TreasuryHoldingController.java`:
  - Expose `GET /api/v1/finance/treasury/summary` for dashboard liquidity and income KPIs.
  - Expose `POST /api/v1/finance/treasury/loans` for originating new intercompany working capital facilities.
  - Expose `GET /api/v1/finance/treasury/loans` and `POST /loans/{id}/accrue-interest`.

- [x] **10.5 Unit & Scenario Tests** in `services/apex-finance-service/src/test/java/com/apex/finance/service/TreasuryHoldingServiceTest.java`:
  - Validate successful loan origination, insufficient liquidity rejection, self-lending defense, and monthly interest accrual posting to GL.
  - Verified 4/4 passing unit tests with 100% assertion coverage.

- [x] **10.6 Interactive Treasury Holding Frontend Dashboard** in `apex-ui/src/App.tsx`:
  - Visual summary cards for total liquidity, 2.50% NIM spread, projected annual income, and DJP audit compliance.
  - Split view of subsidiary cash pool accounts (`SUB-AGRO` surplus vs `SUB-MFG` deficit).
  - Interactive loan simulator calculating borrower interest, lender yield, and net holding income in realtime.
