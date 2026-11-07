# Security Guide & Threat Mitigation Strategy

## APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

---

## 1. Authentication & RBAC Authorization Matrix

APEX-Core applies a **Zero-Trust** security architecture where inbound calls undergo stateless JWT token verification at the **Spring Cloud Gateway**, followed by method-level authorization claims checking inside each downstream microservice.

### 1.1. Role-Based Access Control (RBAC) Matrix

| Endpoint Action / Domain           | `ROLE_REQUESTER` | `ROLE_APPROVER` | `ROLE_BUYER` | `ROLE_RECEIVER` | `ROLE_AP_FINANCE` | `ROLE_AUDITOR` |
| :--------------------------------- | :--------------: | :-------------: | :----------: | :-------------: | :---------------: | :------------: |
| Draft & Submit PR                  |    **ALLOW**     |        -        |      -       |        -        |         -         |       -        |
| Approve / Reject PR                |        -         |    **ALLOW**    |      -       |        -        |         -         |       -        |
| Issue Purchase Order (PO)          |        -         |        -        |  **ALLOW**   |        -        |         -         |       -        |
| Record Goods Receipt (GR)          |        -         |        -        |      -       |    **ALLOW**    |         -         |       -        |
| Ingest Vendor Invoice (VI)         |        -         |        -        |      -       |        -        |     **ALLOW**     |       -        |
| Release Payment Voucher            |        -         |        -        |      -       |        -        |     **ALLOW**     |       -        |
| Verify Cryptographic Ledger Chain  |        -         |        -        |      -       |        -        |     **ALLOW**     |   **ALLOW**    |
| Cross-Department Financial Reports |        -         |        -        |      -       |        -        |         -         |   **ALLOW**    |

---

## 2. OWASP API Security Top 10 Mitigation Matrix

| OWASP API Threat Vector                                   | APEX-Core Specific Risk                                                                                                 | Architectural Mitigation Strategy                                                                                                                                                                                                       |
| :-------------------------------------------------------- | :---------------------------------------------------------------------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **API1: Broken Object Level Auth (BOLA / IDOR)**          | User modifies PR or PO ID in URL parameters to inspect or approve unauthorized departmental budgets.                    | Enforces JWT claim checks on `tenant_id` and `cost_center_id` at repository method boundaries: `@PreAuthorize("@securityEvaluator.canAccessCostCenter(principal, #costCenterId)")`. Public IDs use UUIDv4 or randomized opaque strings. |
| **API2: Broken Authentication**                           | Token signature forging or session hijacking.                                                                           | Asymmetric RS256 signature verification. Short token TTL (15 minutes). Token revocation blacklist maintained in Redis with automated TTL expiration.                                                                                    |
| **API3: Broken Object Property Level Auth**               | Attackers attempt mass assignment by injecting arbitrary `APPROVED` status or mutated `total_amount` in request bodies. | Enforces strict Java 21 Record DTOs (`@RequestBody RequisitionCreateRequest`). JPA entities are never exposed or bound directly to REST controller endpoints.                                                                           |
| **API4: Unrestricted Resource Consumption**               | Flooding PR submissions or triggering full ledger re-indexing to exhaust CPU/memory.                                    | Spring Cloud Gateway enforces a distributed Redis Token Bucket Rate Limiter (100 req/min per IP/user). All search endpoints enforce mandatory pagination (`page`, `size` maximum 100).                                                  |
| **API5: Broken Function Level Auth**                      | Unauthorized users executing supervisor approval endpoints.                                                             | Method-level security annotations `@PreAuthorize("hasRole('APPROVER')")` across service layers. Gateway restricts administrative paths to authenticated corporate roles.                                                                |
| **API6: Unrestricted Access to Sensitive Business Flows** | Double-approvals or duplicate invoice payments triggered by network timeouts or retry storms.                           | Mandatory `X-Idempotency-Key` header verified against Redis using atomic `SETNX` commands, serializing execution and returning cached responses for duplicates.                                                                         |
| **API7: Server Side Request Forgery (SSRF)**              | Vendor webhook URLs forged to target internal cloud metadata (`169.254.169.254` or `localhost`).                        | Webhook URL validator rejects loopback and RFC 1918 private subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`, `127.0.0.1`). Only public HTTPS destinations are permitted.                                                       |
| **API8: Security Misconfiguration**                       | Verbose database error messages and stack traces leaking internal architecture in responses.                            | Global Exception Handler conforms to RFC 7807 Problem Details. Internal stack traces and SQL schema names are sanitized in production (`server.error.include-stacktrace: never`).                                                       |
| **API9: Improper Inventory Management**                   | Internal test or actuator debugging endpoints exposed publicly.                                                         | Spring Boot Actuator endpoints are restricted to `/actuator/health` and `/actuator/prometheus` on an isolated internal management port.                                                                                                 |
| **API10: Unsafe Consumption of APIs**                     | Malicious payloads injected through incoming Kafka broker topics.                                                       | Kafka event consumers enforce strict JSON schema deserialization and data validation before database writes.                                                                                                                            |

---

## 3. Input Sanitization & Data Integrity Defense

### 3.1. Elimination of Floating-Point Drift

- Primitive `float` and `double` are strictly prohibited for monetary calculations.
- All monetary amounts use `java.math.BigDecimal` with fixed scale (4 decimals) and banker's rounding:
  ```java
  BigDecimal unitPrice = request.unitPrice().setScale(4, RoundingMode.HALF_EVEN);
  BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(4, RoundingMode.HALF_EVEN);
  ```

### 3.2. SQL Injection Prevention

- Persistence operations use Spring Data JPA / Hibernate with parameterized prepared statements.
- String concatenation within JPQL or native SQL queries is strictly prohibited.

---

## 4. Secrets Management & Environment Isolation

1. **Repository Protection:** `.env` files, credentials, and private keys (`*.pem`, `*.jks`) are excluded in `.gitignore`.
2. **Environment Variable Injection:** All sensitive values (database passwords, JWT public keys, broker credentials) are injected through environment variables:
   ```yaml
   SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
   APEX_SECURITY_JWT_PUBLIC_KEY: ${JWT_PUBLIC_KEY}
   ```
3. **Log Sanitization:** Logback masking pattern filters prevent masking sensitive attributes such as bank account numbers, tax IDs, and authorization tokens from appearing in log streams.
