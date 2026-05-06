/**
 * useAgentNotifications — Real-time order notifications for service agents.
 *
 * Subscribes to /topic/agents/{agentId}/new-order
 * When a new order arrives:
 *   1. Shows a toast notification with service + location details
 *   2. Calls onNewOrder() callback so the parent can refresh the orders list
 *
 * Usage in AgentHome:
 *   useAgentNotifications(agentId, () => fetchOrders());
 */

import { useEffect } from 'react';
import { toast } from 'react-toastify';
import { useWebSocket } from '../../lib/useWebSocket';

export const useAgentNotifications = (agentId, onNewOrder) => {
  const { subscribe, isConnected } = useWebSocket();

  useEffect(() => {
    if (!agentId) return;

    const destination = `/topic/agents/${agentId}/new-order`;

    const unsubscribe = subscribe(destination, (notification) => {
      // Show a rich toast notification
      toast.info(
        <div>
          <p className="font-bold text-sm">🔔 New Order Request!</p>
          <p className="text-sm mt-1">
            <span className="font-medium">Service:</span> {notification.serviceName}
          </p>
          <p className="text-sm">
            <span className="font-medium">Location:</span> {notification.location}
          </p>
          <p className="text-sm text-green-700 font-semibold mt-1">
            ₹{notification.totalPrice}
          </p>
        </div>,
        {
          position: 'top-right',
          autoClose: 8000,
          hideProgressBar: false,
          pauseOnHover: true,
          draggable: true,
          toastId: `order-${notification.orderId}`, // prevent duplicate toasts
        }
      );

      // Trigger parent to refresh orders list
      if (onNewOrder) onNewOrder(notification);
    });

    return unsubscribe;
  }, [agentId, subscribe, onNewOrder]);

  return { isConnected };
};
