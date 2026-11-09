# APEX-Core Engine (apex-core)

[![Java Version](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=flat&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3%2B-6DB33F?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Architecture](https://img.shields.io/badge/Architecture-Event--Driven%20Microservices-orange?style=flat)]()
[![Messaging](https://img.shields.io/badge/Messaging-Redpanda%20%7C%20Kafka-red?style=flat&logo=apachekafka)](https://redpanda.com/)
[![Database](https://img.shields.io/badge/Database-MySQL%208.0%20%7C%20Redis%207-blue?style=flat&logo=mysql)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**APEX-Core** (*Asynchronous Procurement & Enterprise Exchange Core*) is a high-performance, distributed **Procurement-to-Pay (P2P)** enterprise backend engine engineered in **Java 21** and **Spring Boot 3.3+**. It orchestrates mission-critical enterprise workflows connecting **Organizational HRIS** (approval limits & cost center allocations), **Procurement Lifecycle Management** (PR-to-PO state machines), and **Financial Accounts Payable (AP)**.

The platform guarantees distributed data consistency via the **Transactional Outbox Pattern**, choreography-based **Saga Transactions**, automated **3-Way Matching Engine** (Purchase Order $\iff$ Goods Receipt $\iff$ Vendor Invoice), and tamper-evident **SHA-256 cryptographic general ledgers**—engineered to run seamlessly within a local **8 GB RAM workstation** (~1.85 GB footprint) using containerized MySQL 8.0, Redis, and Redpanda.

---

## 👨‍💻 About the Author

Hi, I'm **Jika**!

- 🐙 **GitHub**: [@ifjika](https://github.com/ifjika)
- 🌐 **Portfolio Website**: [ifjika-dev.vercel.app](https://ifjika-dev.vercel.app)

I engineered this project as an end-to-end deep dive into **Enterprise Resource Planning (ERP) and Distributed Financial Core Architecture**. In enterprise procurement, software anomalies or untracked state mutations translate directly to duplicate payments, rogue spending (*maverick buying*), or catastrophic audit failures. My objective with **APEX-Core** is to demonstrate how modern **Event-Driven Microservices** in **Java 21** solve distributed transactions without heavyweight distributed locks, ensure mathematically verifiable accounting invariants, and achieve ultra-low resource consumption without compromising enterprise compliance.

---

## 🏛️ Key Architectural Features

### 1. Automated 3-Way Matching Engine (PO-GR-VI Invariant)

- **Mathematical Invariant**: Validates multi-point consistency across purchasing, physical warehouse intake, and vendor billing before any payout voucher can be authorized:
  $$\text{Status Valid} \iff (\text{Qty}_{\text{PO}} = \text{Qty}_{\text{GR}} = \text{Qty}_{\text{VI}}) \land (\text{Price}_{\text{PO}} = \text{Price}_{\text{VI}})$$
- **Configurable Decimal Tolerance**: Flags micro-variances (e.g., fractional VAT rounding up to 0.5%) as `MATCHED_WITH_TOLERANCE` while routing larger discrepancies to specialized variance resolution queues.

### 2. Transactional Outbox Pattern & Zero Dual-Write Anomaly

- **Atomic State Transitions**: Business mutations and outbound event messages are committed within a **single local MySQL transaction** (`@Transactional`).
- **Reliable Event Streaming**: A dedicated background polling worker consumes uncommitted rows from `outbox_events` and dispatches them to **Redpanda (Apache Kafka protocol)** with strict *At-Least-Once Delivery* guarantees.

### 3. Choreography-Based Saga Distributed Transactions

- **Decentralized Coordination**: Microservices react autonomously to domain events (`PR_SUBMITTED`, `BUDGET_RESERVED`, `PO_ISSUED`, `GOODS_RECEIVED`) without a single point of orchestration bottleneck.
- **Compensating Transactions**: Automatic rollback routines release pre-allocated budget reservations in the HRIS domain whenever an approval is rejected or a contract is cancelled.

### 4. Tamper-Evident Cryptographic General Ledger (Append-Only GL)

- **Cryptographic Hash Chaining**: Every financial entry in `LedgerEntry` is bound to the preceding journal line using a sequential **SHA-256 Hash Chain**:
  $$\text{CurrentHash}_n = \text{SHA256}(\text{PreviousHash}_{n-1} \parallel \text{Seq}_n \parallel \text{TxnRef}_n \parallel \text{Account}_n \parallel \text{Type}_n \parallel \text{Amount}_n \parallel \text{Timestamp}_n)$$
- **Non-Repudiation**: Direct database tampering breaks the cryptographic continuity and is instantly detected by automated background audit verifiers (`/api/v1/finance/ledger/verify-chain`).

### 5. Concurrency Determinism & Financial Precision Defense

- **Optimistic Locking**: Critical JPA aggregates employ `@Version` fields to eliminate race conditions during concurrent managerial approvals.
- **Floating-Point Drift Elimination**: Uses immutable value objects backed by `java.math.BigDecimal` with a fixed scale of 4 decimal places and `RoundingMode.HALF_EVEN` (Banker's Rounding).

### 6. Defensive API Gateway & Ingress Layer

- **Atomic Idempotency Shield**: Header-based `X-Idempotency-Key` deduplication using Redis distributed caching to eliminate accidental double-submissions.
- **Token Bucket Rate Limiting**: Distributed rate-limiting filter guarding services against traffic surges.
- **RFC 7807 Problem Details**: Consistent, machine-readable error responses across all microservices.

---

## 📐 System Topology

```text
       ┌─────────────────────────────────────────────────────────────┐
       │             Client Web Portal / Mobile Approver             │
       │                   [HTTPS / REST JSON]                       │
       └──────────────────────────────┬──────────────────────────────┘
                                      │
                                      ▼
       ┌─────────────────────────────────────────────────────────────┐
       │             SPRING CLOUD GATEWAY (Port 8080)                │
       │  - JWT Bearer Authentication & RBAC Evaluator               │
       │  - Redis Token Bucket Rate Limiter (100 req/min)            │
       │  - X-Idempotency-Key Deduplication & RFC 7807 Filter        │
       └──────────────┬───────────────┬──────────────┬───────────────┘
                      │               │              │
           /api/v1/hris/*    /api/v1/procurement/*   /api/v1/finance/*
                      │               │              │
                      ▼               ▼              ▼
       ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────┐
       │ apex-hris-service  │  │apex-procurement-svc│  │apex-finance-service│
       │    [Port 8081]     │  │    [Port 8082]     │  │    [Port 8083]     │
       │ - Employee Tree    │  │ - PR & PO Lifecyle │  │ - 3-Way Match Eng  │
       │ - Approval Matrix  │  │ - State Machine    │  │ - Append-Only GL   │
       │ - Cost Center Cap  │  │ - Vendor Registry  │  │ - Payment Vouchers │
       └─────────┬──────────┘  └─────────┬──────────┘  └─────────┬──────────┘
                 │                       │                       │
                 ▼                       ▼                       ▼
       ┌───────────────────┐   ┌───────────────────┐   ┌────────────────────┐
       │MySQL(apex_hris_db)│   │MySQL(apex_proc_db)│   │MySQL(apex_fin_db)  │
       └─────────┬─────────┘   └─────────┬─────────┘   └─────────┬──────────┘
                 │                       │                       │
                 │               Transactional Outbox                    │
                 │                       ▼                       │
                 └──────────────────────>│<──────────────────────┘
                                         ▼
                     ┌───────────────────────────────────────┐
                     │     REDPANDA / APACHE KAFKA BROKER    │
                     │       [Port 9092 | Event Streaming]   │
                     │  - apex.procurement.events            │
                     │  - apex.finance.matching              │
                     └───────────────────┬───────────────────┘
                                         │
                                Distributed Cache / Lock
                                         ▼
                     ┌───────────────────────────────────────┐
                     │            REDIS 7 CACHE              │
                     │   - Distributed Lock (SETNX)          │
                     │   - Token Bucket Rate Limiting        │
                     │   - Idempotency Deduplication Key     │
                     └───────────────────────────────────────┘
```

---

## 🗂️ Monorepo Structure

```text
apex-core/
├── apex-ui/                             # Enterprise Frontend (React 18, Vite, TypeScript, Lucide)
│   ├── src/                             # P2P & In-House Banking Treasury Dashboard
│   └── package.json
├── services/                            # Distributed Backend Microservices (Spring Boot 3.3, Java 21)
│   ├── apex-common-core/                # Shared Domain Models, Money VO, Outbox Base & RFC 7807 Handlers
│   │   ├── src/main/java/com/apex/common/domain/        # Immutable Money Value Object, EventEnvelope<T>
│   │   ├── src/main/java/com/apex/common/exception/     # Global ProblemDetails RFC 7807 Error Handlers
│   │   └── src/main/java/com/apex/common/idempotency/   # Redis-backed Idempotency Interceptor
│   ├── apex-gateway/                    # Spring Cloud Gateway (Port 8080)
│   │   ├── src/main/java/com/apex/gateway/filter/       # JWT Auth Filter, Rate Limiter & Reverse Proxy
│   │   └── src/main/resources/application.yml
│   ├── apex-hris-service/               # Organizational Governance & Matrix Service (Port 8081)
│   │   ├── src/main/java/com/apex/hris/                 # Employee Registry, Cost Center, Approval Matrix
│   │   └── src/main/resources/db/migration/             # Flyway Schema Migrations (schema: hris)
│   ├── apex-procurement-service/        # Purchasing & PO Ingestion Service (Port 8082)
│   │   ├── src/main/java/com/apex/procurement/          # PR-to-PO State Machine, Outbox Poller Worker
│   │   └── src/main/resources/db/migration/             # Flyway Schema Migrations (schema: procurement)
│   └── apex-finance-service/            # 3-Way Match, Cryptographic Ledger & Treasury Holding (Port 8083)
│       ├── src/main/java/com/apex/finance/              # 3-Way Match Algorithm, SHA-256 GL Chain, Treasury In-House Bank
│       └── src/main/resources/db/migration/             # Flyway Schema Migrations (schema: finance)
├── deploy/                              # Local Cluster Infrastructure
│   ├── docker-compose.yml               # MySQL 8.0, Redis 7, Redpanda Broker (<600MB container RAM)
│   └── init-db/01-create-schemas.sql    # Multi-database initialization script
├── docs/                                # Enterprise Architectural Documentation Suite
├── mvnw & mvnw.cmd                      # Portable Apache Maven Wrapper (Java 21 LTS)
├── pom.xml                              # Root Maven Parent POM (Java 21, Spring Boot 3.3.x BOM)
└── README.md
```

---

## 🛠️ Tech Stack & Engineering Standards

| Domain | Technology / Standard | Usage |
| :--- | :--- | :--- |
| **Language & Platform** | **Java 21 LTS** | Virtual Threads (Project Loom), Pattern Matching, Record Types |
| **Framework Ecosystem** | **Spring Boot 3.3+ / Cloud 2023** | Enterprise DI, Spring Data JPA, Spring Cloud Gateway |
| **Architecture** | **Event-Driven Microservices (EDA)** | Domain-Driven Design (DDD), Outbox Pattern & Choreography Saga |
| **Database of Record** | **MySQL 8.0 (Multi-Database)** | Isolated databases (`apex_hris_db`, `apex_procurement_db`, `apex_finance_db`) |
| **Distributed Messaging** | **Redpanda (Kafka Protocol v24+)** | Low-latency C++ event broker with minimal memory overhead (<300MB) |
| **Cache & Distributed Lock** | **Redis 7 Alpine** | Idempotency key TTL, Token Bucket rate limiting, and distributed locks |
| **Schema Migration** | **Flyway 10** | Deterministic version-controlled database schema migrations |
| **Security & Auth** | **Spring Security 6 + JJWT (RS256)** | Stateless Bearer JWT tokens, Tenant ID & Cost Center scoping |
| **Testing Suite** | **JUnit 5, AssertJ, Testcontainers** | Real Docker-backed integration testing without in-memory compromises |
| **Load & Stress Testing** | **k6 (Grafana)** | Concurrency verification (1,000 RPS) validating Optimistic Locking |

---

## 🚀 Getting Started

### Prerequisites

- **Java 21 LTS** installed on your workstation.
- **Apache Maven 3.9+** installed.
- **Docker** & **Docker Compose** installed and running.

### 1. Clone & Setup Environment

```bash
git clone https://github.com/ifjika/apex-core.git
cd apex-core

# Verify project documentation suite
ls -l docs/
```

### 2. Start Local Infrastructure via Docker Compose

Run the memory-optimized infrastructure stack (MySQL 8.0, Redis, and Redpanda):

```bash
docker compose -f deploy/docker-compose.yml up -d
```

Verify service availability:
- **MySQL 8.0**: `localhost:3306` (Databases: `apex_hris_db`, `apex_procurement_db`, `apex_finance_db`)
- **Redis 7**: `localhost:6379`
- **Redpanda Broker**: `localhost:9092`

### 3. Build & Run Test Suite Across All Modules

Execute the build across all multi-module microservices:

```bash
# Build multi-module monorepo
mvn clean install -DskipTests

# Run Unit & Integration Test Suites
mvn test
```

### 4. Run Services with Explicit Memory Bounds

Run each service within bounded heap limits to preserve local workstation memory:

```bash
# Terminal 1: Ingress API Gateway
java -Xms128m -Xmx192m -XX:+UseG1GC -jar apex-gateway/target/apex-gateway-1.0.0-SNAPSHOT.jar

# Terminal 2: HRIS Core Service
java -Xms128m -Xmx256m -XX:+UseG1GC -jar apex-hris-service/target/apex-hris-service-1.0.0-SNAPSHOT.jar

# Terminal 3: Procurement Service
java -Xms128m -Xmx256m -XX:+UseG1GC -jar apex-procurement-service/target/apex-procurement-service-1.0.0-SNAPSHOT.jar

# Terminal 4: Finance Settlement Service
java -Xms128m -Xmx256m -XX:+UseG1GC -jar apex-finance-service/target/apex-finance-service-1.0.0-SNAPSHOT.jar
```

---

## 📚 Architectural Documentation

The project includes an enterprise-grade technical documentation suite in the [`docs/`](docs/) directory:

- 📄 [**OVERVIEW.md**](docs/OVERVIEW.md) — System Overview, Domain Boundaries & Local Resource Optimization Strategy.
- 📄 [**PRD.md**](docs/PRD.md) — Product Requirements Document, User Personas, Acceptance Criteria & Target KPIs.
- 📄 [**ARCHITECTURE.md**](docs/ARCHITECTURE.md) — High-Level Architectural Patterns, System Diagrams & Memory Budgets.
- 📄 [**TSD.md**](docs/TSD.md) — Technical Specification: Deep PostgreSQL Schemas, 3-Way Match & SHA-256 Algorithms.
- 📄 [**SECURITY_GUIDE.md**](docs/SECURITY_GUIDE.md) — RBAC Matrix, OWASP API Security Mitigation & Financial Integrity Rules.
- 📄 [**APP_FLOW.md**](docs/APP_FLOW.md) — End-to-End Sequence Diagrams, PR/PO/Match State Machines & Navigation Map.
- 📄 [**INTEGRATION.md**](docs/INTEGRATION.md) — External REST Contracts, CloudEvents Payloads, Webhooks & RFC 7807 Errors.
- 📄 [**TESTING_GUIDE.md**](docs/TESTING_GUIDE.md) — Testing Standards, Testcontainers Base Classes & k6 Concurrency Benchmark.
- 📄 [**DESIGN_SYSTEM.md**](docs/DESIGN_SYSTEM.md) — Enterprise Design Tokens, Reconcilation Tables & UI/UX Guidelines.
- 📄 [**IMPLEMENTATION_PLAN.md**](docs/IMPLEMENTATION_PLAN.md) — Phased Roadmap, Milestone Dependencies & Active Task Checklist.

---

## 📄 License

This project is open-source software licensed under the [MIT License](LICENSE).

