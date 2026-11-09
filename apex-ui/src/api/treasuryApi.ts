import api from './client.ts';
import { TreasurySummary, IntercompanyLoanRecord, LoanOriginatePayload } from '../types/treasury.ts';

export const treasuryApi = {
  getSummary: async (): Promise<TreasurySummary> => {
    const res = await api.get<TreasurySummary>('/finance/treasury/summary');
    return res.data;
  },

  getAllLoans: async (): Promise<IntercompanyLoanRecord[]> => {
    const res = await api.get<IntercompanyLoanRecord[]>('/finance/treasury/loans');
    return res.data;
  },

  originateLoan: async (payload: LoanOriginatePayload): Promise<IntercompanyLoanRecord> => {
    const res = await api.post<IntercompanyLoanRecord>('/finance/treasury/loans', payload);
    return res.data;
  },

  accrueMonthlyInterest: async (loanId: string): Promise<void> => {
    await api.post(`/finance/treasury/loans/${loanId}/accrue-interest`);
  },
};

