import React, { useState } from 'react';
import { useProcurement } from '../../hooks/useProcurement';
import { RequisitionTable } from './RequisitionTable';
import { CreatePrModal } from './CreatePrModal';
import { formatIDR } from '../../utils/formatters';

export const ProcurementPortal: React.FC = () => {
  const { requisitions, loading, createPR, approvePR, rejectPR } = useProcurement();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const totalCommitted = requisitions.reduce((acc, r) => acc + r.totalAmount, 0);
  const pendingCount = requisitions.filter((r) => r.status === 'PENDING_APPROVAL').length;

  return (
    <div className="space-y-6">
      {/* Header with Call-to-Action */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-xl font-bold text-white">Procurement & Sourcing Portal</h2>
          <p className="text-xs text-slate-400 mt-1">
            Automated PR-to-PO workflow with transactional outbox publishing to Kafka/Redpanda.
          </p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-blue-600 hover:bg-blue-500 text-white font-medium text-xs px-4 py-2.5 rounded-lg shadow-lg shadow-blue-950 flex items-center space-x-2 transition"
        >
          <span>+ Create Purchase Requisition</span>
        </button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <span className="text-xs text-slate-400">Total Sourcing Commitments</span>
          <div className="text-xl font-bold font-mono text-white mt-1">{formatIDR(totalCommitted)}</div>
          <span className="text-[11px] text-slate-500 mt-2 block">{requisitions.length} requisitions tracked</span>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <span className="text-xs text-slate-400">Pending Approval</span>
          <div className="text-xl font-bold font-mono text-amber-400 mt-1">{pendingCount} Requisitions</div>
          <span className="text-[11px] text-amber-400/80 mt-2 block">Awaiting Supervisor Authorization</span>
        </div>
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <span className="text-xs text-slate-400">Kafka Outbox Status</span>
          <div className="text-xl font-bold font-mono text-emerald-400 mt-1">Operational</div>
          <span className="text-[11px] text-emerald-400/80 mt-2 block">Transactional Outbox Poller Active</span>
        </div>
      </div>

      {/* Requisitions Table */}
      <RequisitionTable
        requisitions={requisitions}
        onApprove={(id) => approvePR(id, 'EMP-002')}
        onReject={(id) => rejectPR(id, 'EMP-002', 'Budget constraint rejection')}
        loading={loading}
      />

      <CreatePrModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={createPR}
        loading={loading}
      />
    </div>
  );
};

