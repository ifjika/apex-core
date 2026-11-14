import api from './client';
import { Employee, ApprovalRouteResponse, CostCenter } from '../types/hris';

export const hrisApi = {
  getEmployees: async (): Promise<Employee[]> => {
    const res = await api.get<Employee[]>('/hris/employees');
    return res.data;
  },

  getCostCenter: async (id: string): Promise<CostCenter> => {
    const res = await api.get<CostCenter>(`/hris/cost-centers/${id}`);
    return res.data;
  },

  getApprovalRoute: async (requesterId: string, amount: number): Promise<ApprovalRouteResponse> => {
    const res = await api.get<ApprovalRouteResponse>('/hris/approval-routing/route', {
      params: { requesterId, amount },
    });
    return res.data;
  },
};

