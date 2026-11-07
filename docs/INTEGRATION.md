# Integration Specification & API Contracts
## APEX-Core (Asynchronous Procurement & Enterprise Exchange Core)

---

## 1. API Standards & Protocols

* **Standard Format:** RESTful JSON over HTTPS (v1 endpoints: `/api/v1/...`).
* **Authentication:** Stateless Bearer JWT via Authorization header (`Authorization: Bearer <token>`).
* **Content-Type:** `application/json; charset=UTF-8`.
* **Idempotency Header:** Critical mutating endpoints mandate an `X-Idempotency-Key: <UUIDv4>` header.
* **Error Representation:** RFC 7807 (*Problem Details for HTTP APIs*).

---

## 2. Inbound & Outbound External API Contracts

### 2.1. Purchase Requisition Submission
* **Endpoint:** `POST /api/v1/procurement/requisitions`
* **Headers:**
  ```http
  Authorization: Bearer <JWT>
  X-Idempotency-Key: 9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d
  Content-Type: application/json
  ```
* **Request Payload:**
  ```json
  {
    "cost_center_id": "CC-FIN-001",
    "requested_by_employee_id": "EMP-88210",
    "required_date": "2026-11-20",
    "notes": "Procurement of enterprise rackmount servers for data center expansion",
    "currency": "IDR",
    "line_items": [
      {
        "item_name": "Server Rack 42U Enterprise",
        "description": "Standard 19-inch 42U Server Cabinet",
        "quantity": 2,
        "estimated_unit_price": "18500000.00"
      },
      {
        "item_name": "PDU Rackmount 16A",
        "description": "Vertical Power Distribution Unit",
        "quantity": 4,
        "estimated_unit_price": "2750000.00"
      }
    ]
  }
  ```
* **Response (HTTP 201 Created):**
  ```json
  {
    "pr_id": "PR-2026-11-0042",
    "status": "PENDING_APPROVAL",
    "total_estimated_amount": "48000000.00",
    "currency": "IDR",
    "created_at": "2026-11-07T09:15:30Z",
    "approval_route": [
      {
        "step": 1,
        "approver_role": "SUPERVISOR",
        "status": "APPROVED",
        "acted_at": "2026-11-07T09:16:00Z"
      },
      {
        "step": 2,
        "approver_role": "DEPARTMENT_MANAGER",
        "status": "PENDING"
      }
    ]
  }
  ```

### 2.2. Vendor Invoice Submission (Accounts Payable Ingestion)
* **Endpoint:** `POST /api/v1/finance/invoices`
* **Request Payload:**
  ```json
  {
    "vendor_id": "VEND-IND-501",
    "invoice_number": "INV/2026/XI/889",
    "invoice_date": "2026-11-07",
    "due_date": "2026-12-07",
    "purchase_order_id": "PO-2026-11-0019",
    "goods_receipt_id": "GR-2026-11-0014",
    "tax_amount": "5280000.00",
    "total_amount": "53280000.00",
    "line_items": [
      {
        "po_line_item_id": "POLI-9901",
        "invoiced_quantity": 2,
        "unit_price": "18500000.00",
        "subtotal": "37000000.00"
      },
      {
        "po_line_item_id": "POLI-9902",
        "invoiced_quantity": 4,
        "unit_price": "2750000.00",
        "subtotal": "11000000.00"
      }
    ]
  }
  ```
* **Response (HTTP 200 OK / 202 Accepted):**
  ```json
  {
    "invoice_id": "VI-2026-11-0081",
    "status": "3WAY_MATCHED",
    "variance_detected": false,
    "voucher_id": "PV-2026-11-0033",
    "ledger_entry_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  }
  ```

---

## 3. Kafka Event Message Contracts

Payload contracts comply with the CloudEvents 1.0 specification:

### 3.1. Topic: `apex.procurement.events`
```json
{
  "specversion": "1.0",
  "id": "e4f8b2d1-0f72-4e94-81b4-21394b8e2193",
  "source": "apex.procurement.service",
  "type": "com.apex.procurement.po.issued.v1",
  "datacontenttype": "application/json",
  "time": "2026-11-07T09:20:00Z",
  "data": {
    "po_id": "PO-2026-11-0019",
    "vendor_id": "VEND-IND-501",
    "cost_center_id": "CC-FIN-001",
    "total_amount": "48000000.00",
    "currency": "IDR",
    "items": [
      {
        "item_id": "POLI-9901",
        "name": "Server Rack 42U Enterprise",
        "quantity": 2,
        "unit_price": "18500000.00"
      }
    ]
  }
}
```

### 3.2. Topic: `apex.finance.matching`
```json
{
  "specversion": "1.0",
  "id": "78a9c1e2-bdf1-4a21-8201-9876543210ab",
  "source": "apex.finance.service",
  "type": "com.apex.finance.match.completed.v1",
  "datacontenttype": "application/json",
  "time": "2026-11-07T09:25:00Z",
  "data": {
    "match_id": "MATCH-7712",
    "po_id": "PO-2026-11-0019",
    "invoice_id": "VI-2026-11-0081",
    "result": "MATCHED",
    "delta_amount": "0.00",
    "voucher_generated": true
  }
}
```

---

## 4. Webhook Specifications

### 4.1. Outbound Webhook: Vendor PO Dispatch
Dispatched to the Vendor ERP system upon final approval of a PO.
* **Method:** `POST`
* **Signature Header:** `X-APEX-Signature: sha256=<HMAC_SHA256_HASH>`
* **Payload:**
  ```json
  {
    "event": "purchase_order.issued",
    "po_number": "PO-2026-11-0019",
    "vendor_code": "VEND-IND-501",
    "issued_date": "2026-11-07",
    "contract_download_url": "https://api.apex-core.internal/api/v1/procurement/orders/PO-2026-11-0019/pdf"
  }
  ```

### 4.2. Inbound Webhook: Banking Settlement Notification
* **Endpoint:** `POST /api/v1/finance/webhooks/bank-settlement`
* **Header:** `X-Signature-Auth: <HMAC>`
* **Payload:**
  ```json
  {
    "voucher_id": "PV-2026-11-0033",
    "disbursement_reference": "BANK-TXN-998877",
    "status": "SETTLED",
    "settled_at": "2026-11-07T10:00:00Z"
  }
  ```

---

## 5. Third-Party Integrations & Resilience

```
[APEX-Core Gateway]
       │
       ├──[Resilience4j Circuit Breaker]──> Banking API (Disbursal)
       ├──[Rate Limiter: 100 req/min]────> e-Invoice / Tax Authority API
       └──[Retry with Exp. Backoff]───────> OCR Optical Receipt Scanner
```

* **Circuit Breaker Policy:**
  * Failure Rate Threshold: $50\%$ failures across the last 20 requests.
  * Wait Duration in Open State: 10 seconds.
  * Automatic Transition to Half-Open.

---

## 6. Standard Problem Details Payload (RFC 7807)

All API errors return standardized RFC 7807 responses:

```json
{
  "type": "https://api.apex-core.internal/errors/three-way-match-discrepancy",
  "title": "3-Way Matching Discrepancy Detected",
  "status": 422,
  "detail": "Invoiced quantity (2) exceeds verified warehouse goods receipt quantity (1) for item POLI-9901.",
  "instance": "/api/v1/finance/invoices",
  "code": "FIN_MATCH_QTY_MISMATCH",
  "timestamp": "2026-11-07T09:30:15Z",
  "field_errors": [
    {
      "field": "line_items[0].invoiced_quantity",
      "rejected_value": 2,
      "expected_value": 1,
      "message": "Invoiced quantity cannot exceed goods receipt verified quantity."
    }
  ]
}
```
