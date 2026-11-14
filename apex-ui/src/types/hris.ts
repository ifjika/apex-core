export interface Employee {
  id: string;
  employeeCode: string;
  email: string;
  fullName: string;
  departmentId: string;
  supervisorId?: string;
  approvalRole: string;
  active: boolean;
}

export interface ApprovalStep {
  stepNumber: number;
  approverId: string;
  approverName: string;
  role: string;
  isFinalApprover: boolean;
}

export interface ApprovalRouteResponse {
  requesterId: string;
  requesterName: string;
  requestedAmount: number;
  isFullyRoutable: boolean;
  approvalSteps: ApprovalStep[];
}

export interface CostCenter {
  id: string;
  code: string;
  name: string;
  allocatedBudget: number;
  reservedBudget: number;
  spentBudget: number;
  active: boolean;
}

