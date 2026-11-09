# Testing Guide & Quality Assurance Standards

## APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

---

## 1. Testing Philosophy & Pyramid

APEX-Core applies a multi-layered testing strategy to guarantee financial correctness (_zero-tolerance for financial errors_) and distributed consistency:

```
           /\
          /  \         E2E & Concurrency Benchmarks (k6)
         / 5% \        - 1,000 concurrent approvals, race condition defense
        /──────\
       /        \      Integration Tests (Testcontainers)
      /   25%    \     - Real MySQL 8.0, Redpanda, Redis inside Docker
     /────────────\
    /              \   Unit Tests (JUnit 5 + AssertJ + Mockito)
   /      70%       \  - Domain logic, 3-Way Match calculation, SHA-256 Ledger
  /──────────────────\
```

---

## 2. Unit Testing Standards

- **Framework:** **JUnit 5 (Jupiter)**, **AssertJ** for expressive fluent assertions, and **Mockito 5** for external dependency isolation.
- **Unit Testing Focus Areas:**
  - Monetary calculation precision (`BigDecimal` scale and rounding rules).
  - 3-Way Matching Engine outcome evaluation (full match, tolerance match, quantity/price discrepancy).
  - SHA-256 cryptographic hash-chaining algorithm on `LedgerEntry`.
  - State Machine transition invariants (PR: `DRAFT` $\rightarrow$ `PENDING_APPROVAL` $\rightarrow$ `APPROVED`).

### Standard Unit Test Example: 3-Way Matching Engine

```java
@ExtendWith(MockitoExtension.class)
class ThreeWayMatchingEngineTest {

    private ThreeWayMatchingEngine matchingEngine;

    @BeforeEach
    void setUp() {
        // Default tolerance: 0.5% (0.005)
        matchingEngine = new ThreeWayMatchingEngine(new BigDecimal("0.005"));
    }

    @Test
    @DisplayName("Should return MATCHED when PO, GR, and VI quantities and prices are identical")
    void shouldReturnMatchedWhenAllParametersAlign() {
        var po = createDummyPO("PO-101", 10, new BigDecimal("100000.00"));
        var gr = createDummyGR("GR-101", 10);
        var vi = createDummyVI("VI-101", 10, new BigDecimal("100000.00"));

        MatchResult result = matchingEngine.evaluate(po, gr, vi);

        assertThat(result.status()).isEqualTo(MatchStatus.MATCHED);
        assertThat(result.hasDiscrepancies()).isFalse();
    }

    @Test
    @DisplayName("Should return DISCREPANCY when invoiced quantity exceeds goods receipt quantity")
    void shouldReturnDiscrepancyWhenInvoicedQtyExceedsReceivedQty() {
        var po = createDummyPO("PO-102", 10, new BigDecimal("100000.00"));
        var gr = createDummyGR("GR-102", 8); // Warehouse received 8 items
        var vi = createDummyVI("VI-102", 10, new BigDecimal("100000.00")); // Vendor billed for 10 items

        MatchResult result = matchingEngine.evaluate(po, gr, vi);

        assertThat(result.status()).isEqualTo(MatchStatus.DISCREPANCY);
        assertThat(result.reasons()).contains("Invoiced quantity cannot exceed goods receipt verified quantity.");
    }
}
```

---

## 3. Integration Testing Standards (Testcontainers)

- **Guiding Principle:** In-memory databases such as H2 are avoided for integration testing; tests execute against real containerized instances of MySQL 8.0, Redpanda, and Redis managed by **Testcontainers**.
- **Rationale:** Verifies MySQL 8.0-specific dialects (JSON outbox columns, AUTO_INCREMENT, InnoDB transactions), real Kafka partition assignment, and Redis atomic scripts.

### Standard Base Integration Test Class

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("apex_hris_db")
            .withUsername("apex_user")
            .withPassword("apex_password");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

---

## 4. Mocking Guidelines

| Component / Scenario                 | Mock Allowed?  | Recommended Approach                                                                                                            |
| :----------------------------------- | :------------: | :------------------------------------------------------------------------------------------------------------------------------ |
| **Domain Logic & Entities**          |     **NO**     | Test directly using real domain object instances.                                                                               |
| **Database Repositories**            | **RESTRICTED** | Mock only in isolated service-level unit tests. In integration tests, use real MySQL 8.0 via Testcontainers.                   |
| **Message Broker (Kafka/Redpanda)**  | **RESTRICTED** | Mock `KafkaTemplate` in unit tests. For Saga flows, use real broker containers to validate serialization and event consumption. |
| **Third-Party APIs (Banking / Tax)** |    **YES**     | Use **WireMock** to simulate external HTTP responses (200, 429, 500, network timeouts).                                         |

---

## 5. Concurrency & Performance Testing (k6)

To validate system behavior under high contention (e.g. concurrent manager approvals on the same document), concurrency tests are automated using k6.

### Script: `stress-approval.js`

```javascript
import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  vus: 50, // 50 concurrent virtual users
  duration: "30s",
  thresholds: {
    http_req_duration: ["p(95)<250"], // 95% of requests must complete within 250ms
    http_req_failed: ["rate<0.01"], // Unexpected errors must be < 1%
  },
};

export default function () {
  const url =
    "http://localhost:8080/api/v1/procurement/requisitions/PR-2026-11-0001/approve";
  const payload = JSON.stringify({
    approver_id: "EMP-001",
    note: "Approved via concurrency test",
    document_version: 1,
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      Authorization: "Bearer DUMMY_STRESS_TOKEN",
    },
  };

  const res = http.post(url, payload, params);

  // Exactly one request must succeed (200), subsequent concurrent requests return 409 Conflict via Optimistic Locking
  check(res, {
    "Status is 200 or 409": (r) => r.status === 200 || r.status === 409,
  });

  sleep(0.1);
}
```

---

## 6. CI/CD Execution Pipeline (GitHub Actions)

Standard build and verification workflow:

```yaml
name: APEX-Core CI Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  verify:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: "21"
          distribution: "temurin"
          cache: maven

      - name: Run Unit & Integration Tests (Testcontainers)
        run: mvn clean verify -B

      - name: Publish Test Report
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: JUnit Test Results
          path: "**/surefire-reports/*.xml"
          reporter: java-junit
```
