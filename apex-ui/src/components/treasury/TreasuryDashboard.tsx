import React, { useState } from 'react';
import { useTreasury } from '../../hooks/useTreasury';
import { CashPoolList } from './CashPoolList';
import { LoanSimulatorCard } from './LoanSimulatorCard';
import { formatIDR, formatPercent } from '../../utils/formatters';

export const TreasuryDashboard: React.FC = () => {
  const { summary, loading, simulation, updateSimulation, originateLoan } = useTreasury();
  const [successBanner, setSuccessBanner] = useState<string | null>(null);

  const handleOriginate = async () => {
    try {
      await originateLoan({
        borrowerSubsidiary: 'SUB-MFG',
        lenderSubsidiary: 'SUB-AGRO',
        principalAmount: simulation.principal,
        tenorMonths: simulation.tenorMonths,
      });
      setSuccessBanner(
        `Loan originated successfully! Disbursed ${formatIDR(simulation.principal)} to SUB-MFG at 8.5% interest.`
      );
      setTimeout(() => setSuccessBanner(null), 6000);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to disburse loan';
      alert(msg);
    }
  };

  return (
    <div className="space-y-6">
      {successBanner && (
        <div className="p-4 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center justify-between animate-fade-in">
          <span>✓ {successBanner}</span>
          <button onClick={() => setSuccessBanner(null)} className="text-emerald-400 hover:text-white font-bold ml-4">✕</button>
        </div>
      )}

      {/* KPI Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <span className="text-xs text-slate-400 font-medium">Total Pool Liquidity</span>
          <div className="text-xl font-bold font-mono text-white mt-1">
            {formatIDR(summary?.totalLiquidityInPool || 0)}
          </div>
          <span className="text-[11px] text-emerald-400 flex items-center gap-1 mt-2">
            <span>●</span> Centralized Treasury
          </span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <span className="text-xs text-slate-400 font-medium">Active Intercompany Loans</span>
          <div className="text-xl font-bold font-mono text-amber-400 mt-1">
            {formatIDR(summary?.totalActiveLoansDisbursed || 0)}
          </div>
          <span className="text-[11px] text-slate-400 mt-2 block">
            {summary?.activeLoanCount || 0} active facility
          </span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <span className="text-xs text-slate-400 font-medium">Projected Holding Income</span>
          <div className="text-xl font-bold font-mono text-emerald-400 mt-1">
            {formatIDR(summary?.projectedAnnualHoldingIncome || 0)}
          </div>
          <span className="text-[11px] text-emerald-400/80 mt-2 block">
            Annual Net Spread Revenue
          </span>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-5 shadow-sm">
          <span className="text-xs text-slate-400 font-medium">Holding Net Margin (NIM)</span>
          <div className="text-xl font-bold font-mono text-sky-400 mt-1">
            +{formatPercent(2.5)}
          </div>
          <span className="text-[11px] text-sky-400/80 mt-2 block">
            8.50% Loan − 6.00% Deposit
          </span>
        </div>
      </div>

      {/* Subsidiary Cash Pool Accounts */}
      <CashPoolList pools={summary?.pools || []} />

      {/* Loan Simulator & Working Capital Disbursement */}
      <LoanSimulatorCard
        simulation={simulation}
        onUpdate={updateSimulation}
        onOriginate={handleOriginate}
        loading={loading}
      />
    </div>
  );
};

