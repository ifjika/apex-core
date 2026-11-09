-- =============================================================================
-- APEX-Core MySQL 8.0 Multi-Database Initialization Script
-- Implements Database-per-Service Bounded Contexts
-- =============================================================================

CREATE DATABASE IF NOT EXISTS apex_hris_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS apex_procurement_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS apex_finance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant permissions to microservice application user
GRANT ALL PRIVILEGES ON apex_hris_db.* TO 'apex_admin'@'%';
GRANT ALL PRIVILEGES ON apex_procurement_db.* TO 'apex_admin'@'%';
GRANT ALL PRIVILEGES ON apex_finance_db.* TO 'apex_admin'@'%';

FLUSH PRIVILEGES;
