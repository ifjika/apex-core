import React, { useState } from 'react';
import { Layers, ShieldCheck, ShoppingCart, CheckCircle, Landmark, TrendingUp, ArrowRight, DollarSign } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'requisition' | 'matching' | 'ledger' | 'treasury'>('treasury');

  // Interactive Treasury Simulator State
  const [loanAmount, setLoanAmount] = useState<number>(300000000); // 300 Juta
  const [tenorMonths, setTenorMonths] = useState<number>(6);

  // Calculations:
  // Borrower Interest = Principal * 8.5% * (tenor/12)
  // Lender Yield = Principal * 6.0% * (tenor/12)
  // Holding Net Margin = Principal * 2.5% * (tenor/12)
  const tenorFraction = tenorMonths / 12;
  const borrowerInterest = loanAmount * 0.085 * tenorFraction;
  const lenderYield = loanAmount * 0.060 * tenorFraction;
  const holdingNetMargin = loanAmount * 0.025 * tenorFraction;

  const formatIDR = (val: number) => {
    return new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 0 }).format(val);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', backgroundColor: '#0b0f19', color: '#f3f4f6' }}>
      {/* Top Header */}
      <header style={{ borderBottom: '1px solid #1f2937', padding: '16px 32px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', backgroundColor: '#111827' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ backgroundColor: '#2563eb', padding: '8px', borderRadius: '8px', display: 'flex', alignItems: 'center' }}>
            <Layers size={22} color="#ffffff" />
          </div>
          <div>
            <h1 style={{ fontSize: '18px', fontWeight: '700', letterSpacing: '-0.025em' }}>APEX-Core</h1>
            <p style={{ fontSize: '12px', color: '#9ca3af' }}>Enterprise Procurement &amp; Treasury Holding Exchange</p>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav style={{ display: 'flex', gap: '6px' }}>
          <button
            onClick={() => setActiveTab('treasury')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '6px',
              border: 'none',
              backgroundColor: activeTab === 'treasury' ? '#1f2937' : 'transparent',
              color: activeTab === 'treasury' ? '#38bdf8' : '#9ca3af',
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            <Landmark size={16} /> Treasury Holding
          </button>
          <button
            onClick={() => setActiveTab('requisition')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '6px',
              border: 'none',
              backgroundColor: activeTab === 'requisition' ? '#1f2937' : 'transparent',
              color: activeTab === 'requisition' ? '#60a5fa' : '#9ca3af',
              fontWeight: 500,
              cursor: 'pointer',
            }}
          >
            <ShoppingCart size={16} /> Requisitions (PR)
          </button>
          <button
            onClick={() => setActiveTab('matching')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '6px',
              border: 'none',
              backgroundColor: activeTab === 'matching' ? '#1f2937' : 'transparent',
              color: activeTab === 'matching' ? '#60a5fa' : '#9ca3af',
              fontWeight: 500,
              cursor: 'pointer',
            }}
          >
            <CheckCircle size={16} /> 3-Way Match
          </button>
          <button
            onClick={() => setActiveTab('ledger')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '6px',
              border: 'none',
              backgroundColor: activeTab === 'ledger' ? '#1f2937' : 'transparent',
              color: activeTab === 'ledger' ? '#60a5fa' : '#9ca3af',
              fontWeight: 500,
              cursor: 'pointer',
            }}
          >
            <ShieldCheck size={16} /> Cryptographic GL
          </button>
        </nav>

        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span style={{ fontSize: '12px', padding: '4px 10px', borderRadius: '12px', backgroundColor: '#064e3b', color: '#34d399', fontWeight: 600 }}>
            Gateway :8080 Active
          </span>
        </div>
      </header>

      {/* Main Content */}
      <main style={{ flex: 1, padding: '32px', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        
        {/* TAB 1: TREASURY HOLDING */}
        {activeTab === 'treasury' && (
          <div>
            {/* Header section */}
            <div style={{ marginBottom: '24px' }}>
              <h2 style={{ fontSize: '24px', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Landmark size={26} color="#38bdf8" /> In-House Banking &amp; Treasury Holding
              </h2>
              <p style={{ color: '#9ca3af', fontSize: '14px', marginTop: '4px' }}>
                Centralized liquidity pooling across corporate subsidiaries. Generates enterprise income through intercompany working capital loans and 2.50% Net Interest Margin (NIM).
              </p>
            </div>

            {/* Metrics Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', marginBottom: '28px' }}>
              <div style={{ backgroundColor: '#111827', padding: '20px', borderRadius: '10px', border: '1px solid #1f2937' }}>
                <p style={{ fontSize: '12px', color: '#9ca3af', fontWeight: 600 }}>TOTAL CASH POOL LIQUIDITY</p>
                <p style={{ fontSize: '22px', fontWeight: '700', color: '#38bdf8', marginTop: '6px' }}>Rp 52.5 Miliar</p>
                <p style={{ fontSize: '12px', color: '#34d399', marginTop: '4px' }}>+Rp 50M (Agro) | +Rp 2.5M (Mfg)</p>
              </div>

              <div style={{ backgroundColor: '#111827', padding: '20px', borderRadius: '10px', border: '1px solid #1f2937' }}>
                <p style={{ fontSize: '12px', color: '#9ca3af', fontWeight: 600 }}>HOLDING SPREAD MARGIN (NIM)</p>
                <p style={{ fontSize: '22px', fontWeight: '700', color: '#10b981', marginTop: '6px' }}>+2.50% p.a.</p>
                <p style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px' }}>8.50% Lending - 6.00% Deposit</p>
              </div>

              <div style={{ backgroundColor: '#111827', padding: '20px', borderRadius: '10px', border: '1px solid #1f2937' }}>
                <p style={{ fontSize: '12px', color: '#9ca3af', fontWeight: 600 }}>PROJECTED ANNUAL INCOME</p>
                <p style={{ fontSize: '22px', fontWeight: '700', color: '#f59e0b', marginTop: '6px' }}>Rp 1.25 Miliar</p>
                <p style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px' }}>From Rp 50B Annual Turnover</p>
              </div>

              <div style={{ backgroundColor: '#111827', padding: '20px', borderRadius: '10px', border: '1px solid #1f2937' }}>
                <p style={{ fontSize: '12px', color: '#9ca3af', fontWeight: 600 }}>TRANSFER PRICING AUDIT</p>
                <p style={{ fontSize: '22px', fontWeight: '700', color: '#a855f7', marginTop: '6px' }}>100% DJP/OECD</p>
                <p style={{ fontSize: '12px', color: '#9ca3af', marginTop: '4px' }}>SHA-256 Chained Contracts</p>
              </div>
            </div>

            {/* Split View: Cash Pool Balances & Loan Origination Simulator */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
              
              {/* Left Panel: Subsidiary Cash Pools */}
              <div style={{ backgroundColor: '#111827', padding: '24px', borderRadius: '12px', border: '1px solid #1f2937' }}>
                <h3 style={{ fontSize: '16px', fontWeight: '600', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <DollarSign size={18} color="#10b981" /> Subsidiary Cash Pool Accounts
                </h3>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {/* Entity 1: Agro */}
                  <div style={{ padding: '16px', backgroundColor: '#1f2937', borderRadius: '8px', borderLeft: '4px solid #10b981' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <span style={{ fontSize: '11px', fontWeight: '700', backgroundColor: '#064e3b', color: '#34d399', padding: '2px 8px', borderRadius: '4px' }}>
                          SURPLUS LENDER
                        </span>
                        <h4 style={{ fontSize: '15px', fontWeight: '600', marginTop: '6px' }}>PT Apex Agro Lestari</h4>
                        <p style={{ fontSize: '12px', color: '#9ca3af' }}>Code: SUB-AGRO | Yield Earned: 6.00% p.a.</p>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <p style={{ fontSize: '18px', fontWeight: '700', color: '#f3f4f6' }}>Rp 50.000.000.000</p>
                        <p style={{ fontSize: '12px', color: '#34d399' }}>Idle Cash Placed in Pool</p>
                      </div>
                    </div>
                  </div>

                  {/* Entity 2: Mfg */}
                  <div style={{ padding: '16px', backgroundColor: '#1f2937', borderRadius: '8px', borderLeft: '4px solid #f59e0b' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <span style={{ fontSize: '11px', fontWeight: '700', backgroundColor: '#78350f', color: '#fbbf24', padding: '2px 8px', borderRadius: '4px' }}>
                          DEFICIT BORROWER
                        </span>
                        <h4 style={{ fontSize: '15px', fontWeight: '600', marginTop: '6px' }}>PT Apex Manufaktur Teknik</h4>
                        <p style={{ fontSize: '12px', color: '#9ca3af' }}>Code: SUB-MFG | Borrowing Rate: 8.50% p.a.</p>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <p style={{ fontSize: '18px', fontWeight: '700', color: '#f3f4f6' }}>Rp 2.500.000.000</p>
                        <p style={{ fontSize: '12px', color: '#f59e0b' }}>Needs Capex Funding</p>
                      </div>
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: '20px', padding: '12px', backgroundColor: '#1e293b', borderRadius: '8px', fontSize: '12px', color: '#94a3b8' }}>
                  ℹ️ Holding automatically rebalances liquidity between entities without third-party bank fees or credit delays.
                </div>
              </div>

              {/* Right Panel: Intercompany Loan & Income Calculator */}
              <div style={{ backgroundColor: '#111827', padding: '24px', borderRadius: '12px', border: '1px solid #1f2937' }}>
                <h3 style={{ fontSize: '16px', fontWeight: '600', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <TrendingUp size={18} color="#38bdf8" /> Intercompany Loan Simulator (PR Funding)
                </h3>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  <div>
                    <label style={{ fontSize: '12px', color: '#9ca3af', display: 'block', marginBottom: '6px' }}>
                      Loan Principal Amount (IDR):
                    </label>
                    <input
                      type="number"
                      step="50000000"
                      value={loanAmount}
                      onChange={(e) => setLoanAmount(Number(e.target.value))}
                      style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid #374151', backgroundColor: '#1f2937', color: '#ffffff', fontSize: '15px' }}
                    />
                  </div>

                  <div>
                    <label style={{ fontSize: '12px', color: '#9ca3af', display: 'block', marginBottom: '6px' }}>
                      Loan Tenor (Months):
                    </label>
                    <select
                      value={tenorMonths}
                      onChange={(e) => setTenorMonths(Number(e.target.value))}
                      style={{ width: '100%', padding: '10px 14px', borderRadius: '6px', border: '1px solid #374151', backgroundColor: '#1f2937', color: '#ffffff', fontSize: '15px' }}
                    >
                      <option value={3}>3 Months</option>
                      <option value={6}>6 Months (Standard)</option>
                      <option value={12}>12 Months (1 Year)</option>
                    </select>
                  </div>

                  {/* Financial Breakdown Table */}
                  <div style={{ padding: '16px', backgroundColor: '#1f2937', borderRadius: '8px', display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '6px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                      <span style={{ color: '#9ca3af' }}>Borrower Cost (8.50% p.a.):</span>
                      <span style={{ fontWeight: '600', color: '#ef4444' }}>+{formatIDR(borrowerInterest)}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px' }}>
                      <span style={{ color: '#9ca3af' }}>Lender Yield Paid (6.00% p.a.):</span>
                      <span style={{ fontWeight: '600', color: '#38bdf8' }}>-{formatIDR(lenderYield)}</span>
                    </div>
                    <div style={{ borderTop: '1px solid #374151', paddingTop: '8px', display: 'flex', justifyContent: 'space-between', fontSize: '14px' }}>
                      <span style={{ fontWeight: '700', color: '#34d399' }}>Holding Net Income (2.50% NIM):</span>
                      <span style={{ fontWeight: '700', color: '#34d399' }}>{formatIDR(holdingNetMargin)}</span>
                    </div>
                  </div>

                  <button
                    onClick={() => alert(`Simulated Loan Disbursed! Holding earned ${formatIDR(holdingNetMargin)} net interest margin, recorded to SHA-256 Ledger.`)}
                    style={{
                      marginTop: '6px',
                      padding: '12px',
                      borderRadius: '8px',
                      border: 'none',
                      backgroundColor: '#2563eb',
                      color: '#ffffff',
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '8px',
                    }}
                  >
                    Originate Loan &amp; Disburse to Cost Center <ArrowRight size={16} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: REQUISITION */}
        {activeTab === 'requisition' && (
          <div style={{ backgroundColor: '#111827', borderRadius: '12px', border: '1px solid #1f2937', padding: '24px' }}>
            <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShoppingCart size={20} color="#3b82f6" /> Purchase Requisition Ingestion
            </h2>
            <p style={{ color: '#9ca3af', fontSize: '14px', marginBottom: '24px' }}>
              Create, submit, and route requisitions through the dynamic organizational approval limits matrix.
            </p>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
              <div style={{ backgroundColor: '#1f2937', padding: '16px', borderRadius: '8px' }}>
                <p style={{ fontSize: '12px', color: '#9ca3af' }}>DRAFT REQUISITIONS</p>
                <p style={{ fontSize: '24px', fontWeight: '700', marginTop: '4px' }}>0</p>
              </div>
              <div style={{ backgroundColor: '#1f2937', padding: '16px', borderRadius: '8px' }}>
                <p style={{ fontSize: '12px', color: '#9ca3af' }}>PENDING APPROVAL</p>
                <p style={{ fontSize: '24px', fontWeight: '700', marginTop: '4px', color: '#f59e0b' }}>0</p>
              </div>
              <div style={{ backgroundColor: '#1f2937', padding: '16px', borderRadius: '8px' }}>
                <p style={{ fontSize: '12px', color: '#9ca3af' }}>ORDERED (PO ISSUED)</p>
                <p style={{ fontSize: '24px', fontWeight: '700', marginTop: '4px', color: '#10b981' }}>0</p>
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: MATCHING */}
        {activeTab === 'matching' && (
          <div style={{ backgroundColor: '#111827', borderRadius: '12px', border: '1px solid #1f2937', padding: '24px' }}>
            <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <CheckCircle size={20} color="#10b981" /> Automated 3-Way Matching Engine
            </h2>
            <p style={{ color: '#9ca3af', fontSize: '14px', marginBottom: '24px' }}>
              Mathematical reconciliation across Purchase Order (PO), Goods Receipt (GR), and Vendor Invoice (VI).
            </p>
            <div style={{ padding: '20px', backgroundColor: '#1f2937', borderRadius: '8px' }}>
              <p style={{ fontSize: '14px', color: '#9ca3af' }}>Tolerance Rate: &le; 0.5% (0.005)</p>
            </div>
          </div>
        )}

        {/* TAB 4: LEDGER */}
        {activeTab === 'ledger' && (
          <div style={{ backgroundColor: '#111827', borderRadius: '12px', border: '1px solid #1f2937', padding: '24px' }}>
            <h2 style={{ fontSize: '20px', fontWeight: '600', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldCheck size={20} color="#8b5cf6" /> Cryptographic Append-Only General Ledger
            </h2>
            <p style={{ color: '#9ca3af', fontSize: '14px', marginBottom: '24px' }}>
              SHA-256 hash-chained double-entry accounting records with zero-knowledge tamper detection.
            </p>
          </div>
        )}
      </main>
    </div>
  );
};

export default App;
