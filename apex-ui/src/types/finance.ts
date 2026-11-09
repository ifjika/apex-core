export type InvoiceStatus = 'PENDING_MATCH' | 'MATCHED' | 'DISCREPANCY' | 'PAID';
export type EntryType = 'DEBIT' | 'CREDIT';

export interface LedgerEntry {
  id: string;
  sequenceNumber: number;
  transactionReference: string;
  accountCode: string;
  entryType: EntryType;
  amount: number;
  previousHash: string;
  currentHash: string;
  timestamp: string;
}

export interface ChainVerificationResponse {
  valid: boolean;
  totalEntriesChecked: number;
  message: string;
}

export interface PaymentVoucher {
  id: string;
  voucherNumber: string;
  invoiceId: string;
  amount: number;
  status: 'ISSUED' | 'DISBURSED' | 'CANCELLED';
  createdAt: string;
}

