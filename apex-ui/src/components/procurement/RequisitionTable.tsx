import React from 'react';
import { PurchaseRequisition, RequisitionStatus } from '../../types/procurement';
import { formatIDR } from '../../utils/formatters';

interface RequisitionTableProps {
  requisitions: PurchaseRequisition[];
  onApprove: (id: string) => void;
  onReject: (id: string) => void;
  loading: boolean;
}

const STATUS_BADGES: Record<RequisitionStatus, { label: string; style: string }> = {
  DRAFT: { label: 'Draft', style: 'bg-slate-700/40 text-slate-300 border-slate-600' },
  PENDING_APPROVAL: { label: 'Pending Approval', style: 'bg-amber-500/10 text-amber-400 border-amber-500/30' },
  APPROVED: { label: 'Approved', style: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30' },
  REJECTED: { label: 'Rejected', style: 'bg-rose-500/10 text-rose-400 border-rose-500/30' },
  ORDERED: { label: 'Ordered (PO Issued)', style: 'bg-blue-500/10 text-blue-400 border-blue-500/30' },
};

export const RequisitionTable: React.FC<RequisitionTableProps> = ({
  requisitions,
  onApprove,
  onReject,
  loading,
}) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden shadow-sm">
      <div className="px-6 py-4 border-b border-slate-800 flex justify-between items-center">
        <div>
          <h3 className="font-semibold text-slate-100">Purchase Requisitions (PR)</h3>
          <p className="text-xs text-slate-400">Finite State Machine: DRAFT → PENDING → APPROVED → ORDERED</p>
        </div>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left text-sm text-slate-300">
          <thead className="bg-slate-950/60 text-xs uppercase font-mono text-slate-400 border-b border-slate-800">
            <tr>
              <th className="px-6 py-3">PR Number</th>
              <th className="px-6 py-3">Cost Center</th>
              <th className="px-6 py-3">Requester</th>
              <th className="px-6 py-3">Status</th>
              <th className="px-6 py-3 text-right">Total Amount</th>
              <th className="px-6 py-3 text-center">FSM Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {requisitions.map((pr) => {
              const badge = STATUS_BADGES[pr.status];
              const isPending = pr.status === 'PENDING_APPROVAL';

              return (
                <tr key={pr.id} className="hover:bg-slate-800/40 transition">
                  <td className="px-6 py-4 font-mono text-xs font-semibold text-white">{pr.prNumber}</td>
                  <td className="px-6 py-4 font-mono text-xs text-slate-400">{pr.costCenterId}</td>
                  <td className="px-6 py-4 text-xs font-mono text-slate-300">{pr.requesterId}</td>
                  <td className="px-6 py-4">
                    <span className={`text-[11px] font-semibold px-2.5 py-0.5 rounded border ${badge.style}`}>
                      {badge.label}
                    </span>
                  </td>
                  <td className="px-6 py-4 font-mono text-xs font-bold text-white text-right">
                    {formatIDR(pr.totalAmount)}
                  </td>
                  <td className="px-6 py-4 text-center">
                    {isPending ? (
                      <div className="flex items-center justify-center space-x-2">
                        <button
                          onClick={() => onApprove(pr.id)}
                          disabled={loading}
                          className="bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-medium px-2.5 py-1 rounded transition"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => onReject(pr.id)}
                          disabled={loading}
                          className="bg-rose-600/80 hover:bg-rose-600 text-white text-xs font-medium px-2.5 py-1 rounded transition"
                        >
                          Reject
                        </button>
                      </div>
                    ) : (
                      <span className="text-xs text-slate-500 font-mono">Completed</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};

