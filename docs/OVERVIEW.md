# Technical Specification & Architecture Overview: APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

## 1. Executive Summary

This document defines the architectural blueprint for the enterprise backend system **APEX-Core** (_Asynchronous Procurement & Enterprise Exchange Core_), engineered in **Java 21** and **Spring Boot 3.3+**. The system demonstrates an **Event-Driven Microservices** architecture within the Enterprise Resource Planning (ERP) domain, centered on an end-to-end **Procurement-to-Pay (P2P)** pipeline seamlessly integrated with **Organizational HRIS** and **Financial Accounts Payable (AP)**.

The architectural focus is placed on solving distributed transaction integrity, uncompromising financial ledger determinism, cryptographic non-repudiation, and local resource efficiency (_zero-cost infrastructure_).

## 2. System Architecture & Topology

APEX-Core applies the **Database-per-Service** pattern to enforce strict data isolation between bounded contexts. Asynchronous inter-service communication is orchestrated through **Event Streaming** on the Apache Kafka protocol powered by Redpanda.

```
                               ┌────────────────────────────────────────┐
                               │   API Gateway (Spring Cloud Gateway)   │
                               │        [Port 8080 | Auth & RBAC]       │
                               └──────────────────┬─────────────────────┘
                                                  │
                 ┌────────────────────────────────┼────────────────────────────────┐
                 ▼                                ▼                                ▼
   ┌───────────────────────────┐    ┌───────────────────────────┐    ┌───────────────────────────┐
   │     apex-hris-service     │    │ apex-procurement-service  │    │   apex-finance-service    │
   │       [Port 8081]         │    │        [Port 8082]        │    │        [Port 8083]        │
   │ - Employee Registry       │    │ - Purchase Requisition    │    │ - 3-Way Matching Engine   │
   │ - Org Hierarchy / Limit   │    │ - PO Approval Flow        │    │ - General Ledger (GL)     │
   │ - Cost Center Allocation  │    │ - Vendor Management       │    │ - Invoice Payment Voucher │
   └─────────────┬─────────────┘    └─────────────┬─────────────┘    └─────────────┬─────────────┘
                 │                                │                                │
                 ▼                                ▼                                ▼
        [(PostgreSQL DB 1)]              [(PostgreSQL DB 2)]              [(PostgreSQL DB 3)]
                 │                                │                                │
                 │                                │ (Transactional Outbox)         │
                 │                                ▼                                │
                 │                  ┌───────────────────────────┐                  │
                 └─────────────────>│  Redpanda / Apache Kafka  │<─────────────────┘
                                    │     [Event Messaging]     │
                                    └───────────────────────────┘
```

## 3. Bounded Contexts & Core Modules

### 3.1. apex-hris-service (Organizational Core)

- **Responsibilities:**
  - Single Source of Truth for employee identity, departments, and Cost Centers.
  - Approval limits hierarchy matrix (e.g., Supervisor $\le$ IDR 10,000,000; Division Director $>$ IDR 100,000,000).
- **Core Data Models:** `Employee`, `Department`, `CostCenter`, `ApprovalMatrix`.

### 3.2. apex-procurement-service (P2P Ingestion)

- **Responsibilities:**
  - Lifecycle management converting Purchase Requisitions (PR) to Purchase Orders (PO).
  - Asynchronous budget limit verification against requester Cost Centers via HRIS.
  - Finite State Machine engine governing the procurement lifecycle (`DRAFT` $\rightarrow$ `PENDING_APPROVAL` $\rightarrow$ `APPROVED` $\rightarrow$ `ORDERED` $\rightarrow$ `FULFILLED`).
- **Core Data Models:** `PurchaseRequisition`, `PurchaseOrder`, `POLineItem`, `Vendor`.

### 3.3. apex-finance-service (Settlement & Accounts Payable)

- **Responsibilities:**
  - **3-Way Matching Invariant:** Automated multi-point validation across **Purchase Order (PO)**, warehouse **Goods Receipt (GR)**, and **Vendor Invoice (VI)**.
    $$\text{Status Valid} \iff (\text{Qty}_{\text{PO}} = \text{Qty}_{\text{GR}} = \text{Qty}_{\text{VI}}) \land (\text{Price}_{\text{PO}} = \text{Price}_{\text{VI}})$$
  - Append-only double-entry general ledger journal entries.
- **Core Data Models:** `VendorInvoice`, `GoodsReceipt`, `PaymentVoucher`, `LedgerEntry`.

## 4. Distributed Transaction & Data Integrity Patterns

Because each service maintains an isolated database schema, system integrity is safeguarded using the following patterns:

### 4.1. Transactional Outbox Pattern

To prevent dual-write anomalies (where database mutations persist but message broker publish fails):

1. Business state changes and event records are written in **a single atomic local transaction** (`@Transactional`).
2. The `outbox_events` table is polled by a background dispatcher worker.
3. Messages are streamed to Redpanda with **At-Least-Once Delivery** guarantees.

### 4.2. Saga Pattern (Choreography-based Execution)

Handling approval rejections and distributed cancellations:

1. `apex-procurement-service` publishes a `PO_APPROVAL_REQUESTED` event.
2. `apex-hris-service` validates authority limits and emits `BUDGET_RESERVED` or `APPROVAL_REJECTED`.
3. `apex-finance-service` records budget accrual commitment.
4. If any step fails, **Compensating Transactions** trigger to release budget reservations and restore the PO state to `REJECTED`.

### 4.3. Idempotency & Concurrency Defense

- **Idempotency Key:** Every event and mutating request carries a unique UUID (`event_id` or `X-Idempotency-Key`). Consumers store processed keys in Redis with a TTL to prevent duplicate processing.
- **Optimistic Locking:** Critical financial entities are protected with `@Version` annotations to eliminate race conditions when multiple approvers act simultaneously.

## 5. Security & Threat Vector Analysis

| Threat Vector                  | Potential Impact                                                                   | Architectural Mitigation Strategy                                                                                                     |
| :----------------------------- | :--------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------------------------------------------------------ |
| **BOLA / IDOR**                | Unauthorized access to POs or payroll data across departmental boundaries.         | JWT claim verification at each microservice; Cost Center and Tenant ownership checks at repository level (`@PreAuthorize`).           |
| **Financial Ledger Tampering** | Direct balance or invoice manipulation at the database layer by an insider threat. | Every `LedgerEntry` stores a cryptographic `previous_hash` computed via SHA-256, forming an unbreakable hash chain.                   |
| **Floating-Point Drift**       | Erroneous balances caused by standard binary IEEE 754 floating-point division.     | All monetary values strictly use `java.math.BigDecimal` with fixed scale (4 places) and explicit rounding (`RoundingMode.HALF_EVEN`). |

## 6. Zero-Cost Infrastructure & Local Resource Optimization (8 GB RAM)

APEX-Core is engineered to run on a local workstation with 8 GB RAM using resource-constrained Docker containers:

```yaml
version: "3.8"

services:
  # Multi-tenant Shared Database via Logical Schemas
  postgres-apex:
    image: postgres:16-alpine
    container_name: apex-postgres
    environment:
      POSTGRES_USER: apex_admin
      POSTGRES_PASSWORD: secure_dev_password
      POSTGRES_DB: apex_core_db
    ports:
      - "5432:5432"
    deploy:
      resources:
        limits:
          memory: 200M

  # In-Memory Cache & Distributed Lock Store
  redis-cache:
    image: redis:7-alpine
    container_name: apex-redis
    command:
      [
        "redis-server",
        "--maxmemory",
        "64mb",
        "--maxmemory-policy",
        "allkeys-lru",
      ]
    ports:
      - "6379:6379"
    deploy:
      resources:
        limits:
          memory: 80M

  # Ultra-lightweight Kafka Protocol Broker in C++
  redpanda-broker:
    image: docker.redpanda.com/redpandadata/redpanda:v24.2.10
    container_name: apex-redpanda
    command:
      - redpanda start
      - --smp 1
      - --memory 250M
      - --reserve-memory 0M
      - --overprovisioned
      - --node-id 0
      - --kafka-addr PLAINTEXT://0.0.0.0:9092
    ports:
      - "9092:9092"
    deploy:
      resources:
        limits:
          memory: 300M
```

### JVM Execution Parameters:

Run each Spring Boot service with explicit heap constraints:

```bash
java -Xms128m -Xmx256m -XX:+UseG1GC -jar apex-hris-service.jar
java -Xms128m -Xmx256m -XX:+UseG1GC -jar apex-procurement-service.jar
java -Xms128m -Xmx256m -XX:+UseG1GC -jar apex-finance-service.jar
```

_Total memory consumption for all 3 microservices + Docker infrastructure ranges between **1.6 GB – 2.0 GB**, preserving workstation responsiveness._

## 7. Quality Assurance & Benchmarking Strategy

1. **Automated Integration Testing:** Uses **Testcontainers** to validate asynchronous Saga choreography against real Docker-managed PostgreSQL and Redpanda instances.
2. **Concurrency Stress Testing (k6):** Validates optimistic locking resilience under 1,000 concurrent approval attempts without ledger anomalies.
3. **OpenAPI Federation:** Unified API catalog aggregated through Spring Cloud Gateway.
