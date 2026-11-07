# Design System & UI/UX Standards
## APEX-Core Enterprise Portal

---

## 1. Overview & Principles

The APEX-Core Enterprise Portal is engineered for enterprise financial applications where the key priorities are **high information density**, **arithmetic precision**, **rapid interaction response**, and **secure multi-tier transaction approval workflows**.

### Core Principles:
1. **Clarity over Decoration:** Minimalist interface focused on data legibility, transaction states, and transparent audit trails.
2. **Deterministic Feedback:** Every mutation or status shift must convey an unambiguous state (Loading, Success, Partial/Discrepancy, Failed).
3. **Ergonomic Auditing:** Allows operators and auditors to review hundreds of line items across PO, GR, and Invoice data without visual fatigue.

---

## 2. Design Tokens

### 2.1. Color Palette

```
Slate / Neutral (Base Surfaces)
  ├─ neutral-950: #0B0F19  (Dark Mode Background)
  ├─ neutral-900: #111827  (Dark Mode Surface / Cards)
  ├─ neutral-800: #1F2937  (Borders & Dividers Dark)
  ├─ neutral-200: #E5E7EB  (Borders Light)
  ├─ neutral-100: #F3F4F6  (Light Mode Secondary Surface)
  └─ neutral-50:  #F9FAFB  (Light Mode Background)

Brand & Primary (Enterprise Blue)
  ├─ primary-900: #1E3A8A  (Deep Navy)
  ├─ primary-700: #1D4ED8  (Active Primary Actions)
  ├─ primary-600: #2563EB  (Default Interactive Buttons & Links)
  ├─ primary-100: #DBEAFE  (Primary Light Badges)
  └─ primary-50:  #EFF6FF  (Highlight Rows & Focus Rings)

Functional & Semantic Tokens
  ├─ Success (Emerald): #059669 (Text) | #D1FAE5 (Surface) | #10B981 (Dot)
  │    → Used for: MATCHED, APPROVED, FULLY_PAID, Valid Hash
  ├─ Warning (Amber):   #D97706 (Text) | #FEF3C7 (Surface) | #F59E0B (Dot)
  │    → Used for: MATCHED_WITH_TOLERANCE, PENDING_APPROVAL, PARTIAL_DELIVERY
  ├─ Error / Danger (Rose): #DC2626 (Text) | #FEE2E2 (Surface) | #EF4444 (Dot)
  │    → Used for: DISCREPANCY, REJECTED, CANCELLED, HASH_TAMPERED
  └─ Info (Sky):        #0284C7 (Text) | #E0F2FE (Surface) | #0EA5E9 (Dot)
       → Used for: DRAFT, ORDERED, IN_PROCESSING
```

### 2.2. Typography

* **Primary Font Family (UI & Body):** `Inter`, `-apple-system`, `BlinkMacSystemFont`, `Segoe UI`, `sans-serif`
* **Monospace Font Family (Code, Hashes, Numbers & Currencies):** `JetBrains Mono`, `Fira Code`, `ui-monospace`, `monospace`

#### Type Hierarchy & Scale

| Token | Size | Line Height | Weight | Usage |
| :--- | :--- | :--- | :--- | :--- |
| `text-display` | 32px (2.00rem) | 40px | 700 (Bold) | Dashboard KPI Totals |
| `text-h1` | 24px (1.50rem) | 32px | 600 (SemiBold) | Page Titles (e.g. Purchase Order Details) |
| `text-h2` | 20px (1.25rem) | 28px | 600 (SemiBold) | Section Headers & Modal Headings |
| `text-h3` | 16px (1.00rem) | 24px | 600 (SemiBold) | Card Headers & Form Section Labels |
| `text-body` | 14px (0.875rem) | 20px | 400 (Regular) / 500 (Medium) | Body Text, Table Cell Values |
| `text-mono` | 13px (0.8125rem) | 18px | 500 (Medium) | Ledger Hashes, UUID, Currency Values (Tabular figures) |
| `text-caption`| 12px (0.75rem) | 16px | 400 (Regular) | Help Text, Timestamps, Table Column Headers |

### 2.3. Spacing & Grid System

Modular spacing system based on a **4px / 8px** increment:
* `space-1`: 4px (badge internal padding, compact gaps)
* `space-2`: 8px (icon gaps, form field margins)
* `space-3`: 12px (table cell vertical padding)
* `space-4`: 16px (standard card padding, control heights)
* `space-6`: 24px (grid column gaps, modal content separation)
* `space-8`: 32px (major layout section separation)

---

## 3. UI Component Guidelines

### 3.1. Financial Data Tables & Virtualization
* **Tabular Figures:** All numeric columns (amounts, quantities) must use monospace fonts with `font-variant-numeric: tabular-nums` and be right-aligned.
* **Sticky Headers:** Transaction tables require fixed headers when content exceeds the viewport height.
* **Inline Variance Highlighting:** Discrepancies in 3-Way Matching comparisons are highlighted with soft amber or rose tints on mismatched cells.

### 3.2. Status Badge Matrix
Standard format: `[Indicator Dot] [Short Label]`.

```html
<!-- Example Approved -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">
  <span class="w-1.5 h-1.5 mr-1.5 bg-emerald-500 rounded-full"></span>
  APPROVED
</span>

<!-- Example Discrepancy -->
<span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-rose-100 text-rose-800">
  <span class="w-1.5 h-1.5 mr-1.5 bg-rose-500 rounded-full"></span>
  3-WAY DISCREPANCY
</span>
```

### 3.3. 3-Way Matching Reconciliation Split-View
Horizontal 3-column comparative view:
* **Column 1:** Purchase Order (Ordered quantities & agreed unit prices)
* **Column 2:** Goods Receipt (Verified warehouse intake & delivery order reference)
* **Column 3:** Vendor Invoice (Billed quantities, unit prices, taxes, and subtotal)
* **Bottom Status Bar:** Highlights absolute delta values $\Delta Qty$ and $\Delta Price$.

### 3.4. Cryptographic Hash Chain Inspector
* Displays sequential journal links: `previous_hash` $\rightarrow$ `current_hash`.
* Features a *"Verify Cryptographic Chain"* action triggering background chain validation with sequential green confirmation ticks.

---

## 4. UX Rules & Interaction Patterns

### 4.1. Double Confirmation for Critical Financial Actions
* Critical actions such as **PO Approval exceeding IDR 50,000,000**, **Document Rejection**, and **Payment Voucher Release** mandate a confirmation modal outlining:
  * Total monetary impact.
  * Cost Center budget allocation effects.
  * Mandatory comment field (minimum 10 characters if rejecting).

### 4.2. Form Input Formatting
* **Currency Inputs:** Automatically formats thousand separators (e.g. `12,500,000.00`), while payloads dispatched to REST APIs retain raw string numbers (e.g., `"12500000.00"`).
* **Keyboard Shortcuts:** Reviewers can approve (`Alt + A`), reject (`Alt + R`), or step to the next document (`J` / `K`) for high-volume audit efficiency.

### 4.3. Optimistic UI & Concurrency Handling
* The UI **must not** apply optimistic state updates to financial balances or document approval transitions; it must await server confirmation (200 OK / 202 Accepted).
* If the server returns HTTP 409 Conflict (Optimistic Locking failure due to concurrent action), a toast alert displays:
  > *"Document was modified by another operator. Reloading latest state."* followed by an automated data refetch.
