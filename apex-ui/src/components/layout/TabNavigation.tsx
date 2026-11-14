import React from 'react';

export type ActiveTab = 'treasury' | 'procurement' | 'hris' | 'finance';

interface TabNavigationProps {
  activeTab: ActiveTab;
  onTabChange: (tab: ActiveTab) => void;
}

interface TabItem {
  id: ActiveTab;
  label: string;
  badge?: string;
  badgeColor?: string;
  icon: string;
}

const TABS: TabItem[] = [
  { id: 'treasury', label: 'Treasury & In-House Banking', badge: 'Revenue Core', badgeColor: 'bg-emerald-500/20 text-emerald-400 border-emerald-500/30', icon: '🏛️' },
  { id: 'procurement', label: 'Procurement Portal', badge: 'FSM', badgeColor: 'bg-blue-500/20 text-blue-400 border-blue-500/30', icon: '📦' },
  { id: 'hris', label: 'HRIS & Org Hierarchy', badge: 'Limits', badgeColor: 'bg-purple-500/20 text-purple-400 border-purple-500/30', icon: '👥' },
  { id: 'finance', label: 'Cryptographic Ledger', badge: 'SHA-256', badgeColor: 'bg-amber-500/20 text-amber-400 border-amber-500/30', icon: '🔒' },
];

export const TabNavigation: React.FC<TabNavigationProps> = ({ activeTab, onTabChange }) => {
  return (
    <div className="border-b border-slate-800 bg-slate-900/60 backdrop-blur px-6">
      <nav className="flex space-x-2 overflow-x-auto py-2.5 scrollbar-none" aria-label="Portal Modules">
        {TABS.map((tab) => {
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onTabChange(tab.id)}
              className={`flex items-center space-x-2 px-4 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 whitespace-nowrap ${
                isActive
                  ? 'bg-slate-800 text-white shadow-sm border border-slate-700'
                  : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/50'
              }`}
            >
              <span>{tab.icon}</span>
              <span>{tab.label}</span>
              {tab.badge && (
                <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded border ${tab.badgeColor}`}>
                  {tab.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>
    </div>
  );
};

