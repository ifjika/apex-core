import React, { useState } from 'react';
import { AppLayout } from './components/layout/AppLayout';
import { ActiveTab } from './components/layout/TabNavigation';
import { TreasuryDashboard } from './components/treasury/TreasuryDashboard';
import { ProcurementPortal } from './components/procurement/ProcurementPortal';
import { HrisDirectory } from './components/hris/HrisDirectory';
import { LedgerAuditView } from './components/finance/LedgerAuditView';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<ActiveTab>('treasury');

  return (
    <AppLayout activeTab={activeTab} onTabChange={setActiveTab}>
      {activeTab === 'treasury' && <TreasuryDashboard />}
      {activeTab === 'procurement' && <ProcurementPortal />}
      {activeTab === 'hris' && <HrisDirectory />}
      {activeTab === 'finance' && <LedgerAuditView />}
    </AppLayout>
  );
};

export default App;
