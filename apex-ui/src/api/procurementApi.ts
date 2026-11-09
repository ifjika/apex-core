import api from './client.ts';
import { PurchaseRequisition, RequisitionCreateRequest } from '../types/procurement.ts';

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
    const res = await api.post<PurchaseRequisition>(`/procurement/requisitions/${id}/approve`, null, {
      params: { approverId },
    });
    return res.data;
  },
};

