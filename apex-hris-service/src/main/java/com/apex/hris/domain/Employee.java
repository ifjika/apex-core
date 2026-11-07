package com.apex.hris.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Corporate employee record defining organizational reporting lines and approval authority.
 */
@Entity
@Table(name = "employees", schema = "hris")
public class Employee {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "employee_code", length = 32, nullable = false, unique = true)
    private String employeeCode;

    @Column(name = "email", length = 128, nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", length = 128, nullable = false)
    private String fullName;

    @Column(name = "department_id", length = 36, nullable = false)
    private String departmentId;

    @Column(name = "supervisor_id", length = 36)
    private String supervisorId;

    @Column(name = "approval_role", length = 32, nullable = false)
    private String approvalRole;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Employee() {}

    public Employee(String id, String employeeCode, String email, String fullName, String departmentId, String supervisorId, String approvalRole) {
        this.id = id;
        this.employeeCode = employeeCode;
        this.email = email;
        this.fullName = fullName;
        this.departmentId = departmentId;
        this.supervisorId = supervisorId;
        this.approvalRole = approvalRole;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }
    public String getSupervisorId() { return supervisorId; }
    public void setSupervisorId(String supervisorId) { this.supervisorId = supervisorId; }
    public String getApprovalRole() { return approvalRole; }
    public void setApprovalRole(String approvalRole) { this.approvalRole = approvalRole; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

