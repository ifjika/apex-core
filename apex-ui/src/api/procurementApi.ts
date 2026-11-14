import { PurchaseRequisition, RequisitionCreateRequest } from '../types/procurement.ts';
import api from './client.ts';

export const procurementApi = {
  createRequisition: async (payload: RequisitionCreateRequest, idempotencyKey?: string): Promise<PurchaseRequisition> => {
    const headers = idempotencyKey ? { 'Idempotency-Key': idempotencyKey } : {};
    const res = await api.post<PurchaseRequisition>('/procurement/requisitions', payload, { headers });
    return res.data;
  },

  getRequisition: async (id: string): Promise<PurchaseRequisition> => {
    const res = await api.get<PurchaseRequisition>(`/procurement/requisitions/${id}`);
    return res.data;
  },

  submitForApproval: async (id: string): Promise<PurchaseRequisition> => {
    const res = await api.post<PurchaseRequisition>(`/procurement/requisitions/${id}/submit`);
    return res.data;
  },

  approveRequisition: async (id: string, approverId: string): Promise<PurchaseRequisition> => {
    const res = await api.post<PurchaseRequisition>(`/procurement/requisitions/${id}/approve`, {
      approverId,
    });
    return res.data;
  },

  rejectRequisition: async (id: string, approverId: string, reason: string): Promise<PurchaseRequisition> => {
    const res = await api.post<PurchaseRequisition>(`/procurement/requisitions/${id}/reject`, {
      approverId,
      reason,
    });
    return res.data;
  },
};

