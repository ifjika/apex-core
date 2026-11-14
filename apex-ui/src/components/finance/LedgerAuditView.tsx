import React from 'react';
import { useLedger } from '../../hooks/useLedger';
import { HashChainVisualizer } from './HashChainVisualizer';

export const LedgerAuditView: React.FC = () => {
  const { entries, verification, verifying, verifyChain } = useLedger();

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h2 className="text-xl font-bold text-white">Cryptographic General Ledger & Audit</h2>
          <p className="text-xs text-slate-400 mt-1">
            Tamper-evident double-entry bookkeeping chained sequentially via SHA-256 digests.
          </p>
        </div>
        <button
          onClick={verifyChain}
          disabled={verifying}
          className="bg-amber-600 hover:bg-amber-500 disabled:opacity-50 text-white font-medium text-xs px-4 py-2.5 rounded-lg shadow-lg shadow-amber-950 flex items-center space-x-2 transition"
        >
          <span>{verifying ? 'Recalculating Digests...' : 'Verify Cryptographic Integrity'}</span>
          <span>🔒</span>
        </button>
      </div>

      {/* Verification Status Banner */}
      {verification && (
        <div
          className={`p-4 rounded-xl border flex items-center justify-between text-xs animate-fade-in ${
            verification.isTamperFree
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300'
              : 'bg-rose-500/10 border-rose-500/30 text-rose-300'
          }`}
        >
          <div className="flex items-center space-x-2">
            <span className="text-base">{verification.isTamperFree ? '🛡️' : '⚠️'}</span>
            <div>
              <span className="font-bold">
                {verification.isTamperFree
                  ? 'Cryptographic Verification Passed: Zero Tampering Detected'
                  : 'INTEGRITY BREACH: Broken Hash Chain Link Detected'}
              </span>
              <p className="text-[11px] opacity-80 mt-0.5">
                {verification.totalEntriesVerified} consecutive entries checked against genesis block.
              </p>
            </div>
          </div>
          <span className="font-mono text-[10px] uppercase font-bold bg-slate-900/60 px-2 py-1 rounded border border-current">
            100% Immutable
          </span>
        </div>
      )}

      {/* Visual Hash Chain */}
      <HashChainVisualizer entries={entries} />
    </div>
  );
};

