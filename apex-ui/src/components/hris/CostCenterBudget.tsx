import React from 'react';
import { CostCenter } from '../../types/hris';
import { formatIDR, formatPercent } from '../../utils/formatters';

interface CostCenterBudgetProps {
  costCenters: CostCenter[];
}

export const CostCenterBudget: React.FC<CostCenterBudgetProps> = ({ costCenters }) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm">
      <h3 className="font-semibold text-slate-100 mb-1">Cost Center Budget Encumbrance</h3>
      <p className="text-xs text-slate-400 mb-4">
        Optimistic locking (@Version) guarantees zero double-allocation of budget funds.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {costCenters.map((cc) => {
          const totalCommitted = cc.spentBudget + cc.reservedBudget;
          const utilizationRate = (totalCommitted / cc.allocatedBudget) * 100;
          const availableBudget = cc.allocatedBudget - totalCommitted;

          return (
            <div key={cc.id} className="p-4 rounded-lg bg-slate-950/60 border border-slate-800/80 space-y-3">
              <div className="flex justify-between items-start">
                <div>
                  <span className="font-mono text-xs font-bold text-slate-300">{cc.code}</span>
                  <h4 className="text-sm font-medium text-white">{cc.name}</h4>
                </div>
                <span className="text-xs font-mono font-bold text-purple-400">
                  {formatPercent(utilizationRate)} Used
                </span>
              </div>

              {/* Progress bar */}
              <div className="w-full bg-slate-800 rounded-full h-2 overflow-hidden flex">
                <div
                  className="bg-purple-500 h-full"
                  style={{ width: `${(cc.spentBudget / cc.allocatedBudget) * 100}%` }}
                  title="Spent"
                />
                <div
                  className="bg-amber-500 h-full"
                  style={{ width: `${(cc.reservedBudget / cc.allocatedBudget) * 100}%` }}
                  title="Reserved"
                />
              </div>

              <div className="grid grid-cols-3 gap-2 text-[11px] pt-1 border-t border-slate-800/60">
                <div>
                  <span className="text-slate-500 block uppercase">Allocated</span>
                  <span className="font-mono text-slate-200">{formatIDR(cc.allocatedBudget)}</span>
                </div>
                <div>
                  <span className="text-amber-500 block uppercase">Reserved</span>
                  <span className="font-mono text-amber-300">{formatIDR(cc.reservedBudget)}</span>
                </div>
                <div>
                  <span className="text-emerald-500 block uppercase">Remaining</span>
                  <span className="font-mono text-emerald-400 font-bold">{formatIDR(availableBudget)}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

