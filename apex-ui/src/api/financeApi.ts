import api from './client.ts';
import { LedgerEntry, ChainVerificationResponse } from '../types/finance.ts';

export const financeApi = {
  getLedgerEntries: async (): Promise<LedgerEntry[]> => {
    const res = await api.get<LedgerEntry[]>('/finance/ledger/entries');
    return res.data;
  },

  verifyLedgerChain: async (): Promise<ChainVerificationResponse> => {
    const res = await api.get<ChainVerificationResponse>('/finance/ledger/verify-chain');
    return res.data;
  },

  getPaymentVoucher: async (id: string) => {
    const res = await api.get(`/finance/vouchers/${id}`);
    return res.data;
  },
};

