import React from 'react';
import { useHris } from '../../hooks/useHris';
import { CostCenterBudget } from './CostCenterBudget';
import { OrgHierarchyTree } from './OrgHierarchyTree';

export const HrisDirectory: React.FC = () => {
  const { employees, costCenters, approvalRoute, calculating, calculateRoute } = useHris();

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2">
        <div>
          <h2 className="text-xl font-bold text-white">HRIS Organizational Core & Budget Control</h2>
          <p className="text-xs text-slate-400 mt-1">
            Hierarchical approval limits, cost center encumbrance, and supervisor escalation matrix.
          </p>
        </div>
      </div>

      <CostCenterBudget costCenters={costCenters} />

      <OrgHierarchyTree
        employees={employees}
        approvalRoute={approvalRoute}
        onCalculateRoute={calculateRoute}
        calculating={calculating}
      />
    </div>
  );
};

