package com.apex.hris.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record ApprovalRouteResponse(
    String requesterId,
    String requesterName,
    BigDecimal requestedAmount,
    boolean isFullyRoutable,
    List<ApprovalStepDTO> approvalSteps
) implements Serializable {

    public record ApprovalStepDTO(
        int stepNumber,
        String approverId,
        String approverName,
        String role,
        boolean isFinalApprover
    ) implements Serializable {}
}

