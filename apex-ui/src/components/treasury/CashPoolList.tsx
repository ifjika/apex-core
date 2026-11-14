import React from 'react';
import { SubsidiaryPool } from '../../types/treasury';
import { formatIDR, formatPercent } from '../../utils/formatters';

interface CashPoolListProps {
  pools: SubsidiaryPool[];
}

export const CashPoolList: React.FC<CashPoolListProps> = ({ pools }) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="font-semibold text-slate-100 flex items-center space-x-2">
            <span>Subsidiary Cash Pools</span>
            <span className="text-xs bg-slate-800 text-slate-400 px-2 py-0.5 rounded border border-slate-700">
              In-House Banking
            </span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Internal treasury accounts sweeping surplus idle funds across enterprise business units.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {pools.map((pool) => {
          const isSurplus = pool.balance > 0;
          return (
            <div
              key={pool.subsidiaryCode}
              className="p-4 rounded-lg bg-slate-950/60 border border-slate-800/80 hover:border-slate-700 transition"
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center space-x-2">
                    <span className="font-mono text-xs font-bold text-slate-300">{pool.subsidiaryCode}</span>
                    <span
                      className={`text-[10px] font-semibold px-2 py-0.5 rounded border ${
                        isSurplus
                          ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                          : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                      }`}
                    >
                      {isSurplus ? 'Surplus Depositor' : 'Deficit / Borrower'}
                    </span>
                  </div>
                  <h4 className="text-sm font-medium text-slate-200 mt-1">{pool.subsidiaryName}</h4>
                </div>
                <div className="text-right">
                  <span className="text-[10px] text-slate-500 block uppercase font-mono">Yield Rate</span>
                  <span className="text-xs font-semibold text-emerald-400 font-mono">
                    {formatPercent(pool.yieldRate * 100)} p.a.
                  </span>
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-slate-800/60 flex items-baseline justify-between">
                <span className="text-xs text-slate-400">Current Balance:</span>
                <span className="text-base font-bold font-mono text-white tracking-tight">
                  {formatIDR(pool.balance)}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

