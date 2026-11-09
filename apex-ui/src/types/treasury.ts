export interface SubsidiaryPool {
  subsidiaryCode: string;
  subsidiaryName: string;
  balance: number;
  yieldRate: number;
}

export interface TreasurySummary {
  totalLiquidityInPool: number;
  totalActiveLoansDisbursed: number;
  projectedAnnualHoldingIncome: number;
  activeLoanCount: number;
  pools: SubsidiaryPool[];
}

export interface IntercompanyLoanRecord {
  id: string;
  loanReference: string;
  borrowerSubsidiary: string;
  lenderSubsidiary: string;
  purchaseRequisitionId?: string;
  principalAmount: number;
  interestRate: number;
  spreadRate: number;
  tenorMonths: number;
  status: 'PENDING' | 'ACTIVE' | 'REPAID' | 'DEFAULTED';
  totalInterestProjected: number;
  holdingMarginProjected: number;
  createdAt: string;
}

export interface LoanOriginatePayload {
  borrowerSubsidiary: string;
  lenderSubsidiary: string;
  purchaseRequisitionId?: string;
  principalAmount: number;
  tenorMonths: number;
}

