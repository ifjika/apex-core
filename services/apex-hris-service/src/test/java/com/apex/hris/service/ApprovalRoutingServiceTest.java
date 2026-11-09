package com.apex.hris.service;

import com.apex.common.exception.BusinessException;
import com.apex.hris.domain.ApprovalMatrix;
import com.apex.hris.domain.Employee;
import com.apex.hris.dto.ApprovalRouteResponse;
import com.apex.hris.repository.ApprovalMatrixRepository;
import com.apex.hris.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalRoutingServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApprovalMatrixRepository approvalMatrixRepository;

    @InjectMocks
    private ApprovalRoutingService approvalRoutingService;

    @Test
    @DisplayName("Should resolve single supervisor approval for requisition below supervisor limit")
    void shouldResolveSingleSupervisorApproval() {
        Employee requester = new Employee("emp-stf", "STF001", "staff@apex.internal", "Staff User", "dept-1", "emp-sup", "STAFF");
        Employee supervisor = new Employee("emp-sup", "SUP001", "sup@apex.internal", "Supervisor User", "dept-1", "emp-mgr", "SUPERVISOR");

        ApprovalMatrix supervisorMatrix = new ApprovalMatrix("mat-1", "SUPERVISOR", BigDecimal.ZERO, new BigDecimal("10000000.00"));

        when(employeeRepository.findById("emp-stf")).thenReturn(Optional.of(requester));
        when(employeeRepository.findById("emp-sup")).thenReturn(Optional.of(supervisor));
        when(approvalMatrixRepository.findByRole("SUPERVISOR")).thenReturn(Optional.of(supervisorMatrix));

        ApprovalRouteResponse response = approvalRoutingService.calculateRoute("emp-stf", new BigDecimal("5000000.00"));

        assertThat(response.isFullyRoutable()).isTrue();
        assertThat(response.approvalSteps()).hasSize(1);
        assertThat(response.approvalSteps().get(0).approverId()).isEqualTo("emp-sup");
        assertThat(response.approvalSteps().get(0).isFinalApprover()).isTrue();
    }

    @Test
    @DisplayName("Should escalate to department manager when requested amount exceeds supervisor limit")
    void shouldEscalateToManagerWhenExceedingSupervisorLimit() {
        Employee requester = new Employee("emp-stf", "STF001", "staff@apex.internal", "Staff User", "dept-1", "emp-sup", "STAFF");
        Employee supervisor = new Employee("emp-sup", "SUP001", "sup@apex.internal", "Supervisor User", "dept-1", "emp-mgr", "SUPERVISOR");
        Employee manager = new Employee("emp-mgr", "MGR001", "mgr@apex.internal", "Manager User", "dept-1", null, "DEPARTMENT_MANAGER");

        ApprovalMatrix supervisorMatrix = new ApprovalMatrix("mat-1", "SUPERVISOR", BigDecimal.ZERO, new BigDecimal("10000000.00"));
        ApprovalMatrix managerMatrix = new ApprovalMatrix("mat-2", "DEPARTMENT_MANAGER", new BigDecimal("10000000.01"), new BigDecimal("50000000.00"));

        when(employeeRepository.findById("emp-stf")).thenReturn(Optional.of(requester));
        when(employeeRepository.findById("emp-sup")).thenReturn(Optional.of(supervisor));
        when(employeeRepository.findById("emp-mgr")).thenReturn(Optional.of(manager));

        when(approvalMatrixRepository.findByRole("SUPERVISOR")).thenReturn(Optional.of(supervisorMatrix));
        when(approvalMatrixRepository.findByRole("DEPARTMENT_MANAGER")).thenReturn(Optional.of(managerMatrix));

        ApprovalRouteResponse response = approvalRoutingService.calculateRoute("emp-stf", new BigDecimal("25000000.00"));

        assertThat(response.isFullyRoutable()).isTrue();
        assertThat(response.approvalSteps()).hasSize(2);
        assertThat(response.approvalSteps().get(0).approverId()).isEqualTo("emp-sup");
        assertThat(response.approvalSteps().get(0).isFinalApprover()).isFalse();
        assertThat(response.approvalSteps().get(1).approverId()).isEqualTo("emp-mgr");
        assertThat(response.approvalSteps().get(1).isFinalApprover()).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid non-positive requisition amount")
    void shouldRejectZeroAmount() {
        Employee requester = new Employee("emp-stf", "STF001", "staff@apex.internal", "Staff User", "dept-1", "emp-sup", "STAFF");
        when(employeeRepository.findById("emp-stf")).thenReturn(Optional.of(requester));

        assertThatThrownBy(() -> approvalRoutingService.calculateRoute("emp-stf", BigDecimal.ZERO))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Requested amount must be greater than zero");
    }
}

