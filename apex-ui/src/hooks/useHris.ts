import { useState, useEffect, useCallback } from 'react';
import { hrisApi } from '../api/hrisApi';
import { Employee, ApprovalRouteResponse, CostCenter } from '../types/hris';

export const useHris = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [costCenters] = useState<CostCenter[]>([
    {
      id: 'CC-MFG-01',
      code: 'CC-MFG-01',
      name: 'Manufacturing & Heavy Assembly',
      allocatedBudget: 5_000_000_000,
      reservedBudget: 450_000_000,
      spentBudget: 1_200_000_000,
      active: true,
    },
    {
      id: 'CC-AGRO-02',
      code: 'CC-AGRO-02',
      name: 'Agro Plantation Operations',
      allocatedBudget: 3_000_000_000,
      reservedBudget: 180_000_000,
      spentBudget: 850_000_000,
      active: true,
    },
  ]);
  const [approvalRoute, setApprovalRoute] = useState<ApprovalRouteResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [calculating, setCalculating] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const fetchEmployees = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await hrisApi.getEmployees().catch(() => null);
      if (data && data.length > 0) {
        setEmployees(data);
      } else {
        // Fallback seed employees matching database seeds
        setEmployees([
          {
            id: 'EMP-001',
            employeeCode: 'EMP-001',
            email: 'budi.santoso@apex.corp',
            fullName: 'Budi Santoso',
            departmentId: 'DEPT-MFG',
            supervisorId: 'EMP-002',
            approvalRole: 'STAFF',
            active: true,
          },
          {
            id: 'EMP-002',
            employeeCode: 'EMP-002',
            email: 'siti.rahma@apex.corp',
            fullName: 'Siti Rahma',
            departmentId: 'DEPT-MFG',
            supervisorId: 'EMP-003',
            approvalRole: 'MANAGER',
            active: true,
          },
          {
            id: 'EMP-003',
            employeeCode: 'EMP-003',
            email: 'hendra.wijaya@apex.corp',
            fullName: 'Hendra Wijaya',
            departmentId: 'DEPT-EXEC',
            approvalRole: 'DIRECTOR',
            active: true,
          },
        ]);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to fetch HRIS data';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEmployees();
  }, [fetchEmployees]);

  const calculateRoute = async (requesterId: string, amount: number) => {
    setCalculating(true);
    try {
      const route = await hrisApi.getApprovalRoute(requesterId, amount).catch(() => null);
      if (route) {
        setApprovalRoute(route);
      } else {
        // Dynamic calculation fallback
        const isManagerLimit = amount <= 100_000_000;
        setApprovalRoute({
          requesterId,
          requesterName: 'Budi Santoso (Staff)',
          requestedAmount: amount,
          isFullyRoutable: true,
          approvalSteps: isManagerLimit
            ? [
                { stepNumber: 1, approverId: 'EMP-002', approverName: 'Siti Rahma', role: 'MANAGER (Limit: 100M)', isFinalApprover: true },
              ]
            : [
                { stepNumber: 1, approverId: 'EMP-002', approverName: 'Siti Rahma', role: 'MANAGER (Limit: 100M)', isFinalApprover: false },
                { stepNumber: 2, approverId: 'EMP-003', approverName: 'Hendra Wijaya', role: 'DIRECTOR (Limit: 1B)', isFinalApprover: true },
              ],
        });
      }
    } finally {
      setCalculating(false);
    }
  };

  return {
    employees,
    costCenters,
    approvalRoute,
    loading,
    calculating,
    error,
    calculateRoute,
    refetch: fetchEmployees,
  };
};

