-- Initialize Logical Schemas for APEX-Core Microservices Cluster
CREATE SCHEMA IF NOT EXISTS hris;
CREATE SCHEMA IF NOT EXISTS procurement;
CREATE SCHEMA IF NOT EXISTS finance;

-- Grant schema permissions
GRANT ALL ON SCHEMA hris TO apex_admin;
GRANT ALL ON SCHEMA procurement TO apex_admin;
GRANT ALL ON SCHEMA finance TO apex_admin;

