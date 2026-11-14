import React from 'react';
import { LedgerEntry } from '../../types/finance';
import { formatIDR } from '../../utils/formatters';

interface HashChainVisualizerProps {
  entries: LedgerEntry[];
}

export const HashChainVisualizer: React.FC<HashChainVisualizerProps> = ({ entries }) => {
  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 shadow-sm space-y-4">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="font-semibold text-slate-100 flex items-center space-x-2">
            <span>Cryptographic Hash-Chain Topology</span>
            <span className="text-[10px] bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2 py-0.5 rounded font-mono font-bold">
              SHA-256 Digest
            </span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Sequential cryptographic linkage: CurrentHash = SHA256(PrevHash || Timestamp || TxPayload).
          </p>
        </div>
      </div>

      {/* Visual Chain Blocks */}
      <div className="flex flex-col space-y-3">
        {entries.map((entry) => {
          const isDebit = entry.entryType === 'DEBIT';

          return (
            <div
              key={entry.id}
              className="p-4 rounded-lg bg-slate-950/60 border border-slate-800 hover:border-slate-700 transition"
            >
              <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2 mb-2 pb-2 border-b border-slate-800/80">
                <div className="flex items-center space-x-2">
                  <span className="text-xs font-mono font-bold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded border border-amber-500/20">
                    Seq #{entry.sequenceNumber}
                  </span>
                  <span className="text-xs font-mono font-semibold text-white">{entry.transactionReference}</span>
                  <span
                    className={`text-[10px] font-mono font-bold px-2 py-0.5 rounded border ${
                      isDebit
                        ? 'bg-blue-500/10 text-blue-400 border-blue-500/20'
                        : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                    }`}
                  >
                    {entry.entryType}
                  </span>
                </div>
                <div className="text-right">
                  <span className="text-xs font-mono font-bold text-white">
                    {formatIDR(entry.amount)}
                  </span>
                </div>
              </div>

              {/* Cryptographic Linkage Display */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-[11px] font-mono">
                <div className="bg-slate-900/80 p-2.5 rounded border border-slate-800 truncate">
                  <span className="text-slate-500 block text-[9px] uppercase tracking-wider mb-0.5">
                    Previous Hash (SHA-256)
                  </span>
                  <span className="text-slate-400 select-all font-mono">{entry.previousHash}</span>
                </div>
                <div className="bg-slate-900/80 p-2.5 rounded border border-amber-500/20 truncate">
                  <span className="text-amber-500/80 block text-[9px] uppercase tracking-wider mb-0.5">
                    Current Block Hash (Calculated)
                  </span>
                  <span className="text-amber-300 select-all font-mono font-semibold">{entry.currentHash}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

