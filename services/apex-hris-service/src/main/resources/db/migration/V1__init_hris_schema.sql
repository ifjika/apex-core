-- =============================================================================
-- APEX-Core HRIS Schema Initialization (MySQL 8.0)
-- =============================================================================

CREATE TABLE IF NOT EXISTS departments (
    id VARCHAR(36) PRIMARY KEY,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cost_centers (
    id VARCHAR(36) PRIMARY KEY,
    department_id VARCHAR(36) NOT NULL,
    code VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    allocated_budget DECIMAL(19, 4) NOT NULL,
    reserved_budget DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    spent_budget DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_cost_centers_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT chk_allocated_positive CHECK (allocated_budget >= 0),
    CONSTRAINT chk_reserved_positive CHECK (reserved_budget >= 0),
    CONSTRAINT chk_spent_positive CHECK (spent_budget >= 0),
    CONSTRAINT chk_budget_consistency CHECK (reserved_budget + spent_budget <= allocated_budget)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS employees (
    id VARCHAR(36) PRIMARY KEY,
    employee_code VARCHAR(32) NOT NULL UNIQUE,
    email VARCHAR(128) NOT NULL UNIQUE,
    full_name VARCHAR(128) NOT NULL,
    department_id VARCHAR(36) NOT NULL,
    supervisor_id VARCHAR(36),
    approval_role VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_employees_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_employees_supervisor FOREIGN KEY (supervisor_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS approval_matrices (
    id VARCHAR(36) PRIMARY KEY,
    role VARCHAR(32) NOT NULL,
    min_amount DECIMAL(19, 4) NOT NULL,
    max_amount DECIMAL(19, 4),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed initial corporate organizational hierarchy & approval matrix
INSERT IGNORE INTO departments (id, code, name) VALUES
('dept-eng-001', 'ENG', 'Engineering & Infrastructure'),
('dept-fin-001', 'FIN', 'Finance & Accounting'),
('dept-ops-001', 'OPS', 'Operations & Logistics');

INSERT IGNORE INTO cost_centers (id, department_id, code, name, allocated_budget, reserved_budget, spent_budget, version) VALUES
('cc-eng-001', 'dept-eng-001', 'CC-ENG-IT', 'IT Infrastructure & Cloud', 500000000.0000, 0.0000, 0.0000, 0),
('cc-fin-001', 'dept-fin-001', 'CC-FIN-001', 'Corporate Treasury', 250000000.0000, 0.0000, 0.0000, 0);

INSERT IGNORE INTO approval_matrices (id, role, min_amount, max_amount) VALUES
('mat-sup-001', 'SUPERVISOR', 0.0000, 10000000.0000),
('mat-mgr-001', 'DEPARTMENT_MANAGER', 10000000.0001, 50000000.0000),
('mat-dir-001', 'DIVISION_DIRECTOR', 50000000.0001, NULL);

-- Seed sample employees
INSERT IGNORE INTO employees (id, employee_code, email, full_name, department_id, supervisor_id, approval_role, is_active) VALUES
('emp-dir-001', 'DIR001', 'director.eng@apex-core.internal', 'Budi Hartono', 'dept-eng-001', NULL, 'DIVISION_DIRECTOR', TRUE),
('emp-mgr-001', 'MGR001', 'manager.eng@apex-core.internal', 'Siti Rahma', 'dept-eng-001', 'emp-dir-001', 'DEPARTMENT_MANAGER', TRUE),
('emp-sup-001', 'SUP001', 'supervisor.eng@apex-core.internal', 'Ahmad Dani', 'dept-eng-001', 'emp-mgr-001', 'SUPERVISOR', TRUE),
('emp-stf-001', 'STF001', 'staff.eng@apex-core.internal', 'Rian Pratama', 'dept-eng-001', 'emp-sup-001', 'STAFF', TRUE);
