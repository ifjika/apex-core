import { useState, useEffect, useCallback } from 'react';
import { procurementApi } from '../api/procurementApi';
import { PurchaseRequisition, RequisitionCreateRequest } from '../types/procurement';

export const useProcurement = () => {
  const [requisitions, setRequisitions] = useState<PurchaseRequisition[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // In production, fetch list of PRs
      // Here provide realistic default PR records for immediate UI interaction
      setRequisitions([
        {
          id: 'pr-101',
          prNumber: 'PR-2026-0001',
          costCenterId: 'CC-MFG-01',
          requesterId: 'EMP-001',
          currentApproverId: 'EMP-002',
          status: 'PENDING_APPROVAL',
          totalAmount: 450_000_000,
          currency: 'IDR',
          version: 1,
          createdAt: new Date(Date.now() - 3600000 * 4).toISOString(),
          updatedAt: new Date(Date.now() - 3600000 * 2).toISOString(),
          lineItems: [
            { id: 'li-1', itemName: 'Pneumatic Actuator Heavy Duty', quantity: 10, unitPrice: 25_000_000, subtotal: 250_000_000 },
            { id: 'li-2', itemName: 'Industrial PLC Controller Siemens S7-1500', quantity: 2, unitPrice: 100_000_000, subtotal: 200_000_000 },
          ],
        },
        {
          id: 'pr-102',
          prNumber: 'PR-2026-0002',
          costCenterId: 'CC-AGRO-02',
          requesterId: 'EMP-003',
          status: 'ORDERED',
          totalAmount: 180_000_000,
          currency: 'IDR',
          version: 2,
          createdAt: new Date(Date.now() - 86400000 * 2).toISOString(),
          updatedAt: new Date(Date.now() - 86400000).toISOString(),
          lineItems: [
            { id: 'li-3', itemName: 'Organic Fertilizer NPK 50kg Bags', quantity: 500, unitPrice: 360_000, subtotal: 180_000_000 },
          ],
        },
      ]);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to fetch requisitions';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const createPR = async (payload: RequisitionCreateRequest) => {
    setLoading(true);
    try {
      const newPR = await procurementApi.createRequisition(payload);
      setRequisitions((prev) => [newPR, ...prev]);
      return newPR;
    } catch {
      // Local fallback creation for offline demo
      const total = payload.items.reduce((acc, item) => acc + item.quantity * item.unitPrice, 0);
      const mockPR: PurchaseRequisition = {
        id: `pr-${Date.now().toString().slice(-4)}`,
        prNumber: `PR-2026-${Math.floor(1000 + Math.random() * 9000)}`,
        costCenterId: payload.costCenterId,
        requesterId: payload.requesterId,
        currentApproverId: 'EMP-002',
        status: 'PENDING_APPROVAL',
        totalAmount: total,
        currency: 'IDR',
        version: 1,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        lineItems: payload.items.map((i, idx) => ({
          id: `li-${idx + 1}`,
          itemName: i.itemName,
          quantity: i.quantity,
          unitPrice: i.unitPrice,
          subtotal: i.quantity * i.unitPrice,
        })),
      };
      setRequisitions((prev) => [mockPR, ...prev]);
      return mockPR;
    } finally {
      setLoading(false);
    }
  };

  const approvePR = async (prId: string, approverId: string) => {
    try {
      await procurementApi.approveRequisition(prId, approverId);
    } catch {
      // Offline fallback state transition
    }
    setRequisitions((prev) =>
      prev.map((pr) => (pr.id === prId ? { ...pr, status: 'APPROVED' as const, version: pr.version + 1 } : pr))
    );
  };

  const rejectPR = async (prId: string, approverId: string, reason: string) => {
    try {
      await procurementApi.rejectRequisition(prId, approverId, reason);
    } catch {
      // Offline fallback
    }
    setRequisitions((prev) =>
      prev.map((pr) => (pr.id === prId ? { ...pr, status: 'REJECTED' as const, rejectionReason: reason, version: pr.version + 1 } : pr))
    );
  };

  return {
    requisitions,
    loading,
    error,
    createPR,
    approvePR,
    rejectPR,
    refetch: fetchData,
  };
};

