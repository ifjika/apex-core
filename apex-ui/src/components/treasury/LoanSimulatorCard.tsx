import React from 'react';
import { LoanSimulationState } from '../../hooks/useTreasury';
import { formatIDR, formatPercent } from '../../utils/formatters';

interface LoanSimulatorCardProps {
  simulation: LoanSimulationState;
  onUpdate: (params: { principal?: number; tenorMonths?: number }) => void;
  onOriginate: () => void;
  loading: boolean;
}

export const LoanSimulatorCard: React.FC<LoanSimulatorCardProps> = ({
  simulation,
  onUpdate,
  onOriginate,
  loading,
}) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-6">
        <div>
          <h3 className="font-semibold text-slate-100 flex items-center space-x-2">
            <span>Intercompany Loan & NIM Engine</span>
            <span className="text-xs bg-emerald-500/10 text-emerald-400 px-2 py-0.5 rounded border border-emerald-500/20">
              Holding Spread: +{formatPercent(simulation.spreadRate)}
            </span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Simulate working capital disbursement from Holding treasury to deficit operating subsidiaries.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Controls */}
        <div className="space-y-4 bg-slate-950/60 p-4 rounded-lg border border-slate-800/80">
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">
              Principal Loan Amount (IDR)
            </label>
            <input
              type="number"
              step="1000000000"
              min="1000000000"
              value={simulation.principal}
              onChange={(e) => onUpdate({ principal: Number(e.target.value) || 0 })}
              className="w-full bg-slate-900 border border-slate-700 rounded-lg px-3 py-2 text-sm font-mono text-white focus:outline-none focus:border-emerald-500"
            />
            <span className="text-[11px] text-slate-500 mt-1 block font-mono">
              {formatIDR(simulation.principal)}
            </span>
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1">
              Tenor (Months): <span className="text-emerald-400 font-mono font-bold">{simulation.tenorMonths} Mos</span>
            </label>
            <input
              type="range"
              min="3"
              max="36"
              step="3"
              value={simulation.tenorMonths}
              onChange={(e) => onUpdate({ tenorMonths: Number(e.target.value) })}
              className="w-full accent-emerald-500"
            />
          </div>

          <div className="pt-2">
            <button
              onClick={onOriginate}
              disabled={loading}
              className="w-full bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white font-semibold py-2.5 px-4 rounded-lg text-xs tracking-wide transition shadow-lg shadow-emerald-950 flex items-center justify-center space-x-2"
            >
              <span>{loading ? 'Processing...' : 'Disburse Intercompany Loan'}</span>
              <span>⚡</span>
            </button>
          </div>
        </div>

        {/* Calculation Visualizer */}
        <div className="lg:col-span-2 grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="bg-slate-950/60 p-4 rounded-lg border border-slate-800/80 flex flex-col justify-between">
            <div>
              <span className="text-[10px] uppercase font-mono text-slate-400 block">Borrower Cost (SUB-MFG)</span>
              <span className="text-xs font-bold text-amber-400 font-mono">
                {formatPercent(simulation.borrowerRate)} p.a.
              </span>
              <p className="text-xs text-slate-400 mt-2">Interest charged to operating subsidiary.</p>
            </div>
            <div className="mt-4 pt-3 border-t border-slate-800">
              <span className="text-[10px] text-slate-500 block uppercase font-mono">Total Interest</span>
              <span className="text-sm font-bold font-mono text-amber-400">
                {formatIDR(simulation.borrowerInterest)}
              </span>
            </div>
          </div>

          <div className="bg-slate-950/60 p-4 rounded-lg border border-slate-800/80 flex flex-col justify-between">
            <div>
              <span className="text-[10px] uppercase font-mono text-slate-400 block">Lender Yield (SUB-AGRO)</span>
              <span className="text-xs font-bold text-sky-400 font-mono">
                {formatPercent(simulation.lenderRate)} p.a.
              </span>
              <p className="text-xs text-slate-400 mt-2">Yield paid to liquidity depositor.</p>
            </div>
            <div className="mt-4 pt-3 border-t border-slate-800">
              <span className="text-[10px] text-slate-500 block uppercase font-mono">Total Yield Paid</span>
              <span className="text-sm font-bold font-mono text-sky-400">
                {formatIDR(simulation.lenderYield)}
              </span>
            </div>
          </div>

          <div className="bg-gradient-to-br from-emerald-950/40 to-slate-950/80 p-4 rounded-lg border border-emerald-500/30 flex flex-col justify-between">
            <div>
              <span className="text-[10px] uppercase font-mono text-emerald-400 font-bold block">
                Holding Net Spread (NIM)
              </span>
              <span className="text-xs font-extrabold text-emerald-400 font-mono">
                +{formatPercent(simulation.spreadRate)} Margin
              </span>
              <p className="text-xs text-emerald-300/80 mt-2">Pure net revenue earned by Holding Treasury.</p>
            </div>
            <div className="mt-4 pt-3 border-t border-emerald-500/20">
              <span className="text-[10px] text-emerald-400/80 block uppercase font-mono">Net Holding Revenue</span>
              <span className="text-base font-extrabold font-mono text-emerald-400">
                {formatIDR(simulation.netHoldingMargin)}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

