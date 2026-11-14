import React, { useState } from 'react';
import { Employee, ApprovalRouteResponse } from '../../types/hris';
import { formatIDR } from '../../utils/formatters';

interface OrgHierarchyTreeProps {
  employees: Employee[];
  approvalRoute: ApprovalRouteResponse | null;
  onCalculateRoute: (requesterId: string, amount: number) => void;
  calculating: boolean;
}

export const OrgHierarchyTree: React.FC<OrgHierarchyTreeProps> = ({
  employees,
  approvalRoute,
  onCalculateRoute,
  calculating,
}) => {
  const [testAmount, setTestAmount] = useState<number>(450_000_000);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div>
          <h3 className="font-semibold text-slate-100">Hierarchical Approval Limits & Matrix</h3>
          <p className="text-xs text-slate-400">
            Recursive chain climbing: Manager handles up to IDR 100M; escalates to Director up to IDR 1B.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Organization Roster */}
        <div className="space-y-3">
          <h4 className="text-xs font-semibold uppercase tracking-wider text-slate-400 font-mono">
            Reporting Hierarchy
          </h4>
          <div className="space-y-2">
            {employees.map((emp, idx) => (
              <div
                key={emp.id}
                className="p-3 rounded-lg bg-slate-950/60 border border-slate-800/80 flex items-center space-x-3"
              >
                <div className="w-7 h-7 rounded-full bg-purple-500/20 text-purple-300 font-bold text-xs flex items-center justify-center border border-purple-500/30">
                  {idx + 1}
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-bold text-white truncate">{emp.fullName}</span>
                    <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-slate-800 text-purple-300 border border-purple-500/20">
                      {emp.approvalRole}
                    </span>
                  </div>
                  <span className="text-[11px] text-slate-500 font-mono">{emp.email}</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Route Simulator */}
        <div className="lg:col-span-2 bg-slate-950/60 p-5 rounded-xl border border-slate-800/80 space-y-4">
          <h4 className="text-xs font-semibold uppercase tracking-wider text-purple-400 font-mono">
            Dynamic Approval Route Simulator
          </h4>

          <div className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <label className="block text-[11px] text-slate-400 mb-1">
                Requested Purchase Amount (IDR)
              </label>
              <input
                type="number"
                step="50000000"
                value={testAmount}
                onChange={(e) => setTestAmount(Number(e.target.value))}
                className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm font-mono text-white focus:outline-none focus:border-purple-500"
              />
              <span className="text-[11px] text-slate-500 mt-1 block font-mono">
                {formatIDR(testAmount)}
              </span>
            </div>
            <div className="flex items-end">
              <button
                onClick={() => onCalculateRoute('EMP-001', testAmount)}
                disabled={calculating}
                className="w-full sm:w-auto px-4 py-2.5 bg-purple-600 hover:bg-purple-500 disabled:opacity-50 text-white font-medium text-xs rounded-lg transition"
              >
                {calculating ? 'Evaluating...' : 'Simulate Route Escalation'}
              </button>
            </div>
          </div>

          {/* Visual Route Steps */}
          {approvalRoute && (
            <div className="pt-3 border-t border-slate-800/80 space-y-2">
              <span className="text-xs text-slate-400 block font-medium">Evaluated Approval Chain:</span>
              <div className="space-y-2">
                {approvalRoute.approvalSteps.map((step) => (
                  <div
                    key={step.stepNumber}
                    className="p-3 rounded-lg bg-slate-900 border border-purple-500/30 flex items-center justify-between text-xs"
                  >
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-purple-400 font-bold">Step {step.stepNumber}:</span>
                      <span className="text-white font-semibold">{step.approverName}</span>
                      <span className="text-slate-400 text-[11px]">({step.role})</span>
                    </div>
                    {step.isFinalApprover && (
                      <span className="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-[10px] font-bold px-2 py-0.5 rounded">
                        ✓ Final Authority
                      </span>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

