export type RequisitionStatus = 'DRAFT' | 'PENDING_APPROVAL' | 'APPROVED' | 'REJECTED' | 'ORDERED';

export interface PRLineItem {
  id?: string;
  itemName: string;
  description?: string;
  quantity: number;
  unitPrice: number;
  subtotal?: number;
}

export interface PurchaseRequisition {
  id: string;
  prNumber: string;
  costCenterId: string;
  requesterId: string;
  currentApproverId?: string;
  status: RequisitionStatus;
  totalAmount: number;
  currency: string;
  rejectionReason?: string;
  version: number;
  createdAt: string;
  updatedAt: string;
  lineItems: PRLineItem[];
}

export interface RequisitionCreateRequest {
  costCenterId: string;
  requesterId: string;
  items: {
    itemName: string;
    description?: string;
    quantity: number;
    unitPrice: number;
  }[];
}

