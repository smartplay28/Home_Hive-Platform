/**
 * useOrderTracking — Real-time order status updates for customers.
 *
 * Subscribes to /topic/customers/{customerId}/order-update
 * When a status update arrives:
 *   1. Shows a contextual toast (success for AGENT_ASSIGNED, info for searching, warning for no agents)
 *   2. Calls onStatusUpdate() so parent can update local state (e.g. order history)
 *
 * Status flow:
 *   PENDING_NOT_ASSIGNED → "Finding agents..."
 *   AGENT_ASSIGNED       → "Raj Kumar accepted your request!" ✅
 *   NO_AGENTS_AVAILABLE  → "No agents in your area" ⚠️
 *
 * Usage:
 *   useOrderTracking(customerId, (update) => setLatestUpdate(update));
 */

import { useEffect } from 'react';
import { toast } from 'react-toastify';
import { useWebSocket } from '../../lib/useWebSocket';

const STATUS_ICONS = {
  PENDING_NOT_ASSIGNED: '🔍',
  AGENT_ASSIGNED: '✅',
  IN_PROGRESS: '🔧',
  COMPLETED: '🎉',
  NO_AGENTS_AVAILABLE: '⚠️',
  CANCELLED: '❌',
};

const getToastType = (status) => {
  switch (status) {
    case 'AGENT_ASSIGNED':
    case 'COMPLETED':
      return 'success';
    case 'NO_AGENTS_AVAILABLE':
    case 'CANCELLED':
      return 'warning';
    default:
      return 'info';
  }
};

export const useOrderTracking = (customerId, onStatusUpdate) => {
  const { subscribe, isConnected } = useWebSocket();

  useEffect(() => {
    if (!customerId) return;

    const destination = `/topic/customers/${customerId}/order-update`;

    const unsubscribe = subscribe(destination, (update) => {
      const icon = STATUS_ICONS[update.newStatus] || '📦';
      const toastType = getToastType(update.newStatus);

      const ToastContent = () => (
        <div>
          <p className="font-bold text-sm">{icon} Order #{update.orderId}</p>
          <p className="text-sm mt-1">{update.message}</p>
          {update.assignedAgentName && (
            <p className="text-xs text-gray-500 mt-1">
              Agent: {update.assignedAgentName}
            </p>
          )}
        </div>
      );

      toast[toastType](<ToastContent />, {
        position: 'bottom-right',
        autoClose: update.newStatus === 'AGENT_ASSIGNED' ? 10000 : 5000,
        toastId: `status-${update.orderId}-${update.newStatus}`,
      });

      // Pass update to parent for UI state update
      if (onStatusUpdate) onStatusUpdate(update);
    });

    return unsubscribe;
  }, [customerId, subscribe, onStatusUpdate]);

  return { isConnected };
};
