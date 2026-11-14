import React, { useState } from 'react';
import { RequisitionCreateRequest } from '../../types/procurement';
import { formatIDR } from '../../utils/formatters';

interface CreatePrModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (payload: RequisitionCreateRequest) => Promise<unknown>;
  loading: boolean;
}

export const CreatePrModal: React.FC<CreatePrModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  loading,
}) => {
  const [costCenterId, setCostCenterId] = useState('CC-MFG-01');
  const [requesterId, setRequesterId] = useState('EMP-001');
  const [itemName, setItemName] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [unitPrice, setUnitPrice] = useState(10_000_000);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!itemName) return;
    await onSubmit({
      costCenterId,
      requesterId,
      items: [{ itemName, quantity: Number(quantity), unitPrice: Number(unitPrice) }],
    });
    setItemName('');
    onClose();
  };

  const calculatedTotal = quantity * unitPrice;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-800 rounded-xl max-w-lg w-full p-6 shadow-2xl space-y-4">
        <div className="flex justify-between items-center border-b border-slate-800 pb-3">
          <h3 className="text-base font-bold text-white">Create Purchase Requisition</h3>
          <button onClick={onClose} className="text-slate-400 hover:text-white">✕</button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4 text-xs">
          <div>
            <label className="block text-slate-400 mb-1">Cost Center</label>
            <select
              value={costCenterId}
              onChange={(e) => setCostCenterId(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-white"
            >
              <option value="CC-MFG-01">CC-MFG-01 (Manufacturing Assembly)</option>
              <option value="CC-AGRO-02">CC-AGRO-02 (Agro Plantation)</option>
            </select>
          </div>

          <div>
            <label className="block text-slate-400 mb-1">Requester ID</label>
            <input
              type="text"
              value={requesterId}
              onChange={(e) => setRequesterId(e.target.value)}
              className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-white font-mono"
            />
          </div>

          <div>
            <label className="block text-slate-400 mb-1">Item Description / SKU</label>
            <input
              type="text"
              placeholder="e.g. Industrial Sensor Array"
              value={itemName}
              onChange={(e) => setItemName(e.target.value)}
              required
              className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-slate-400 mb-1">Quantity</label>
              <input
                type="number"
                min="1"
                value={quantity}
                onChange={(e) => setQuantity(Math.max(1, Number(e.target.value)))}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-white font-mono"
              />
            </div>
            <div>
              <label className="block text-slate-400 mb-1">Unit Price (IDR)</label>
              <input
                type="number"
                min="1000"
                step="500000"
                value={unitPrice}
                onChange={(e) => setUnitPrice(Math.max(0, Number(e.target.value)))}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-white font-mono"
              />
            </div>
          </div>

          <div className="p-3 rounded-lg bg-slate-950 border border-slate-800 flex justify-between items-center">
            <span className="text-slate-400">Total Commitment:</span>
            <span className="text-sm font-bold font-mono text-emerald-400">{formatIDR(calculatedTotal)}</span>
          </div>

          <div className="flex justify-end space-x-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 bg-slate-800 text-slate-300 hover:text-white rounded-lg"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || !itemName}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white font-semibold rounded-lg"
            >
              {loading ? 'Submitting...' : 'Submit PR'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

