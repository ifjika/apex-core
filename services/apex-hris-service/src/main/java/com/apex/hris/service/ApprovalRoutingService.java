package com.apex.hris.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apex.common.exception.BusinessException;
import com.apex.common.exception.EntityNotFoundException;
import com.apex.hris.domain.ApprovalMatrix;
import com.apex.hris.domain.Employee;
import com.apex.hris.dto.ApprovalRouteResponse;
import com.apex.hris.repository.ApprovalMatrixRepository;
import com.apex.hris.repository.EmployeeRepository;

/**
 * Service orchestrating dynamic organizational approval matrix calculations.
 */
@Service
@Transactional(readOnly = true)
public class ApprovalRoutingService {

    private final EmployeeRepository employeeRepository;
    private final ApprovalMatrixRepository approvalMatrixRepository;

    public ApprovalRoutingService(EmployeeRepository employeeRepository,
            ApprovalMatrixRepository approvalMatrixRepository) {
        this.employeeRepository = employeeRepository;
        this.approvalMatrixRepository = approvalMatrixRepository;
    }

    /**
     * Resolves the sequential approval chain for a procurement request by
     * traversing
     * the reporting hierarchy upwards until an executive with adequate authority is
     * reached.
     */
    public ApprovalRouteResponse calculateRoute(String requesterId, BigDecimal amount) {
        Employee requester = employeeRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("Employee", requesterId));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Requested amount must be greater than zero.", "INVALID_REQUEST_AMOUNT");
        }

        List<ApprovalRouteResponse.ApprovalStepDTO> steps = new ArrayList<>();
        String currentSupervisorId = requester.getSupervisorId();
        int stepNumber = 1;
        boolean reachedFinalAuthority = false;

        while (currentSupervisorId != null && !reachedFinalAuthority) {
            final String supervisorIdToFind = currentSupervisorId;
            Employee supervisor = employeeRepository.findById(supervisorIdToFind)
                    .orElseThrow(() -> new EntityNotFoundException("Supervisor", supervisorIdToFind));

            if (!supervisor.isActive()) {
                throw new BusinessException(
                        "Supervisor in hierarchy line is inactive: " + supervisor.getFullName(),
                        "INACTIVE_SUPERVISOR_IN_CHAIN");
            }

            ApprovalMatrix matrix = approvalMatrixRepository.findByRole(supervisor.getApprovalRole())
                    .orElse(null);

            boolean canApprove = matrix != null && matrix.coversAmount(amount);

            steps.add(new ApprovalRouteResponse.ApprovalStepDTO(
                    stepNumber++,
                    supervisor.getId(),
                    supervisor.getFullName(),
                    supervisor.getApprovalRole(),
                    canApprove));

            if (canApprove) {
                reachedFinalAuthority = true;
            } else {
                currentSupervisorId = supervisor.getSupervisorId();
            }
        }

        if (!reachedFinalAuthority) {
            throw new BusinessException(
                    "No authority in reporting chain holds sufficient discretionary limit for amount: " + amount,
                    "EXCEEDS_ORGANIZATIONAL_LIMIT");
        }

        return new ApprovalRouteResponse(
                requester.getId(),
                requester.getFullName(),
                amount,
                true,
                steps);
    }
}
