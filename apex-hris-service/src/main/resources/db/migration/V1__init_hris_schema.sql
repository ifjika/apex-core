-- Schema: hris initialization
CREATE TABLE IF NOT EXISTS departments (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS cost_centers (
    id VARCHAR(36) PRIMARY KEY,
    department_id VARCHAR(36) NOT NULL REFERENCES departments(id),
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    allocated_budget NUMERIC(19, 4) NOT NULL CHECK (allocated_budget >= 0),
    reserved_budget NUMERIC(19, 4) NOT NULL DEFAULT 0.0000 CHECK (reserved_budget >= 0),
    spent_budget NUMERIC(19, 4) NOT NULL DEFAULT 0.0000 CHECK (spent_budget >= 0),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_budget_consistency CHECK (reserved_budget + spent_budget <= allocated_budget)
);

CREATE TABLE IF NOT EXISTS employees (
    id VARCHAR(36) PRIMARY KEY,
    employee_code VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    full_name VARCHAR(128) NOT NULL,
    department_id VARCHAR(36) NOT NULL REFERENCES departments(id),
    supervisor_id VARCHAR(36) REFERENCES employees(id),
    approval_role VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS approval_matrices (
    id VARCHAR(36) PRIMARY KEY,
    role VARCHAR(32) NOT NULL,
    min_amount NUMERIC(19, 4) NOT NULL,
    max_amount NUMERIC(19, 4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed initial corporate organizational hierarchy & approval matrix
INSERT INTO departments (id, code, name) VALUES
('dept-eng-001', 'ENG', 'Engineering & Infrastructure'),
('dept-fin-001', 'FIN', 'Finance & Accounting'),
('dept-ops-001', 'OPS', 'Operations & Logistics')
ON CONFLICT (id) DO NOTHING;

INSERT INTO cost_centers (id, department_id, code, name, allocated_budget, reserved_budget, spent_budget, version) VALUES
('cc-eng-001', 'dept-eng-001', 'CC-ENG-IT', 'IT Infrastructure & Cloud', 500000000.0000, 0.0000, 0.0000, 0),
('cc-fin-001', 'dept-fin-001', 'CC-FIN-001', 'Corporate Treasury', 250000000.0000, 0.0000, 0.0000, 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO approval_matrices (id, role, min_amount, max_amount) VALUES
('mat-sup-001', 'SUPERVISOR', 0.0000, 10000000.0000),
('mat-mgr-001', 'DEPARTMENT_MANAGER', 10000000.0001, 50000000.0000),
('mat-dir-001', 'DIVISION_DIRECTOR', 50000000.0001, NULL)
ON CONFLICT (id) DO NOTHING;

-- Seed sample employees
-- Director (top of chain)
INSERT INTO employees (id, employee_code, email, full_name, department_id, supervisor_id, approval_role, is_active) VALUES
('emp-dir-001', 'DIR001', 'director.eng@apex-core.internal', 'Budi Hartono', 'dept-eng-001', NULL, 'DIVISION_DIRECTOR', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Manager (reports to Director)
INSERT INTO employees (id, employee_code, email, full_name, department_id, supervisor_id, approval_role, is_active) VALUES
('emp-mgr-001', 'MGR001', 'manager.eng@apex-core.internal', 'Siti Rahma', 'dept-eng-001', 'emp-dir-001', 'DEPARTMENT_MANAGER', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Supervisor (reports to Manager)
INSERT INTO employees (id, employee_code, email, full_name, department_id, supervisor_id, approval_role, is_active) VALUES
('emp-sup-001', 'SUP001', 'supervisor.eng@apex-core.internal', 'Ahmad Dani', 'dept-eng-001', 'emp-mgr-001', 'SUPERVISOR', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Requester Staff (reports to Supervisor)
INSERT INTO employees (id, employee_code, email, full_name, department_id, supervisor_id, approval_role, is_active) VALUES
('emp-stf-001', 'STF001', 'staff.eng@apex-core.internal', 'Rian Pratama', 'dept-eng-001', 'emp-sup-001', 'STAFF', TRUE)
ON CONFLICT (id) DO NOTHING;

