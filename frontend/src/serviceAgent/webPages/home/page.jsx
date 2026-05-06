import { useState, useEffect, useCallback } from 'react';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Layout from '../../layout.jsx';
import services from '../../../customer/components/servicesData.jsx';
import api, { getUser } from '../../../lib/api.js';
import { useAgentNotifications } from '../../hooks/useAgentNotifications.jsx';

/* ─── Status Badge ─────────────────────────────────────────────────────────── */
const ConnectionBadge = ({ isConnected }) => (
  <div className={`flex items-center gap-2 text-sm font-medium px-3 py-1 rounded-full
    ${isConnected
      ? 'bg-green-100 text-green-700 border border-green-300'
      : 'bg-yellow-100 text-yellow-700 border border-yellow-300'}`}>
    <span className={`w-2 h-2 rounded-full ${isConnected ? 'bg-green-500 animate-pulse' : 'bg-yellow-500'}`} />
    {isConnected ? 'Live — Notifications Active' : 'Connecting...'}
  </div>
);

/* ─── Empty State ───────────────────────────────────────────────────────────── */
const EmptyState = ({ icon, title, subtitle }) => (
  <div className="text-center py-12 text-gray-400">
    <div className="text-5xl mb-3">{icon}</div>
    <p className="text-lg font-medium text-gray-500">{title}</p>
    <p className="text-sm mt-1">{subtitle}</p>
  </div>
);

/* ─── Order Card ────────────────────────────────────────────────────────────── */
const OrderCard = ({ serviceId, order, onAccept, onReject, onStart, onComplete, completed = false }) => {
  const serviceDetails = (() => {
    for (const category in services) {
      const s = services[category]?.find(s => s.id === serviceId);
      if (s) return s;
    }
    return null;
  })();

  const [accepting, setAccepting] = useState(false);
  const [rejecting, setRejecting] = useState(false);
  const [starting, setStarting] = useState(false);
  const [completing, setCompleting] = useState(false);

  const handleAccept = async () => {
    setAccepting(true);
    await onAccept(order.orderId, serviceId);
    setAccepting(false);
  };

  const handleReject = async () => {
    setRejecting(true);
    await onReject(order.orderId, serviceId);
    setRejecting(false);
  };

  const handleStart = async () => {
    setStarting(true);
    await onStart(order.orderId);
    setStarting(false);
  };

  const handleComplete = async () => {
    setCompleting(true);
    await onComplete(order.orderId);
    setCompleting(false);
  };

  return (
    <div className={`rounded-xl border p-5 transition-all duration-200
      ${completed
        ? 'bg-green-50 border-green-200'
        : 'bg-white border-gray-200 hover:shadow-lg hover:border-blue-300'}`}>

      {/* Header row */}
      <div className="flex justify-between items-start mb-4">
        <div>
          <p className="text-xs text-gray-400 font-mono">Order #{order.orderId}</p>
          <h3 className="text-base font-bold text-gray-800 mt-0.5">
            {serviceDetails?.name || serviceId}
          </h3>
        </div>
        <span className={`px-2 py-0.5 rounded-full text-xs font-semibold
          ${completed ? 'bg-green-100 text-green-700' : 
            order.orderStatus === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-700' :
            order.orderStatus === 'AGENT_ASSIGNED' ? 'bg-yellow-100 text-yellow-700' :
            'bg-orange-100 text-orange-700'}`}>
          {completed ? '✓ Completed' : 
           order.orderStatus === 'IN_PROGRESS' ? '🔧 In Progress' :
           order.orderStatus === 'AGENT_ASSIGNED' ? '👷 Accepted' :
           '● Pending'}
        </span>
      </div>

      {/* Details grid */}
      <div className="grid grid-cols-2 gap-3 mb-4">
        <div className="bg-gray-50 rounded-lg p-3">
          <p className="text-xs text-gray-400 mb-0.5">Location</p>
          <p className="text-sm font-semibold text-gray-700">📍 {order.location}</p>
        </div>
        <div className="bg-gray-50 rounded-lg p-3">
          <p className="text-xs text-gray-400 mb-0.5">Earnings</p>
          <p className="text-sm font-bold text-green-600">₹{serviceDetails?.price?.replace('₹','') || '—'}</p>
        </div>
      </div>

      {/* Service description */}
      {serviceDetails?.description && (
        <p className="text-xs text-gray-500 mb-4 leading-relaxed">{serviceDetails.description}</p>
      )}

      {/* Action buttons — only for pending orders */}
      {!completed && order.orderStatus === 'PENDING_NOT_ASSIGNED' && (
        <div className="flex gap-3 mt-2">
          <button
            onClick={handleAccept}
            disabled={accepting || rejecting}
            className="flex-1 bg-green-500 hover:bg-green-600 disabled:bg-green-300
              text-white font-semibold py-2.5 rounded-lg transition-all duration-200
              flex items-center justify-center gap-2 text-sm"
          >
            {accepting ? <><span className="animate-spin">⏳</span> Accepting...</> : <><span>✓</span> Accept</>}
          </button>
          <button
            onClick={handleReject}
            disabled={accepting || rejecting}
            className="flex-1 bg-red-500 hover:bg-red-600 disabled:bg-red-300
              text-white font-semibold py-2.5 rounded-lg transition-all duration-200
              flex items-center justify-center gap-2 text-sm"
          >
            {rejecting ? <><span className="animate-spin">⏳</span> Rejecting...</> : <><span>✕</span> Reject</>}
          </button>
        </div>
      )}

      {!completed && order.orderStatus === 'AGENT_ASSIGNED' && (
        <div className="flex gap-3 mt-2">
          <button
            onClick={handleStart}
            disabled={starting}
            className="w-full bg-[#1c4e80] hover:bg-[#153a60] disabled:bg-[#1c4e80]/50
              text-white font-semibold py-2.5 rounded-lg transition-all duration-200
              flex items-center justify-center gap-2 text-sm"
          >
            {starting ? <><span className="animate-spin">⏳</span> Starting...</> : <><span>🚀</span> Start Service</>}
          </button>
        </div>
      )}

      {!completed && order.orderStatus === 'IN_PROGRESS' && (
        <div className="flex gap-3 mt-2">
          <button
            onClick={handleComplete}
            disabled={completing}
            className="w-full bg-green-500 hover:bg-green-600 disabled:bg-green-300
              text-white font-semibold py-2.5 rounded-lg transition-all duration-200
              flex items-center justify-center gap-2 text-sm"
          >
            {completing ? <><span className="animate-spin">⏳</span> Finishing...</> : <><span>🏁</span> Finish Service</>}
          </button>
        </div>
      )}
    </div>
  );
};

/* ─── Agent Home Page ───────────────────────────────────────────────────────── */
function AgentHome() {
  const [orders, setOrders] = useState({ pending: {}, completed: {} });
  const [loading, setLoading] = useState(true);
  const agentId = getUser().userId || localStorage.getItem('agentId');

  /* Fetch orders from backend */
  const fetchOrders = useCallback(async () => {
    try {
      const response = await api.get(`/agents/${agentId}/orders`);
      const serviceAgent = response.data.data;

      const pendingOrders = serviceAgent.pending_orders || {};
      const completedOrders = serviceAgent.completed_orders || {};

      setOrders({ pending: pendingOrders, completed: completedOrders });
    } catch (error) {
      console.error('Error fetching orders:', error);
    } finally {
      setLoading(false);
    }
  }, [agentId]);

  /* Initial load */
  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  /* ─── Real-time WebSocket notifications ─────────────────────────────────── */
  /* Replaces the old setInterval(fetchOrders, 5000) polling */
  const { isConnected } = useAgentNotifications(
    agentId,
    useCallback(() => fetchOrders(), [fetchOrders]) // called when new order arrives
  );

  /* Accept/Reject handlers */
  const handleAccept = async (orderId, serviceId) => {
    try {
      await api.post('/orders/accept', {
        agentId: agentId,
        orderId: orderId,
        itemId: serviceId,
      });
      toast.success('Order accepted! Customer has been notified.', { position: 'top-center' });
      await fetchOrders();
    } catch (error) {
      toast.error('Failed to accept order. Please try again.');
    }
  };

  const handleReject = async (orderId, serviceId) => {
    try {
      await api.post('/orders/reject', {
        agentId: agentId,
        orderId: orderId,
        itemId: serviceId,
      });
      toast.info('Order rejected.', { position: 'top-center' });
      await fetchOrders();
    } catch (error) {
      toast.error('Failed to reject order. Please try again.');
    }
  };

  const handleStart = async (orderId) => {
    try {
      await api.post('/orders/start', { agentId, orderId });
      toast.success('Service started!', { position: 'top-center' });
      await fetchOrders();
    } catch (error) {
      toast.error('Failed to start service.');
    }
  };

  const handleComplete = async (orderId) => {
    try {
      await api.post('/orders/complete', { agentId, orderId });
      toast.success('Service completed successfully!', { position: 'top-center' });
      await fetchOrders();
    } catch (error) {
      toast.error('Failed to complete service.');
    }
  };

  // Safely calculate lengths since pending/completed values are arrays
  const pendingCount = Object.values(orders.pending).reduce((acc, list) => acc + (Array.isArray(list) ? list.length : 0), 0);
  const completedCount = Object.values(orders.completed).reduce((acc, list) => acc + (Array.isArray(list) ? list.length : 0), 0);

  return (
    <Layout>
      <ToastContainer limit={4} />

      <div className="bg-[#eaf0f7] min-h-screen p-6">
        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-8">
          <div>
            <h1 className="text-[#1c4e80] text-3xl font-bold">Service Agent Dashboard</h1>
            <p className="text-gray-500 text-sm mt-1">
              Accept orders assigned to you in real time
            </p>
          </div>
          <ConnectionBadge isConnected={isConnected} />
        </div>

        {/* Stats row */}
        <div className="grid grid-cols-2 gap-4 mb-8">
          <div className="bg-white rounded-xl p-4 shadow-sm border border-orange-100">
            <p className="text-sm text-gray-500">Pending Orders</p>
            <p className="text-3xl font-bold text-orange-500 mt-1">{pendingCount}</p>
          </div>
          <div className="bg-white rounded-xl p-4 shadow-sm border border-green-100">
            <p className="text-sm text-gray-500">Completed Orders</p>
            <p className="text-3xl font-bold text-green-500 mt-1">{completedCount}</p>
          </div>
        </div>

        {loading ? (
          <div className="grid gap-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="h-44 bg-white rounded-xl animate-pulse shadow-sm" />
            ))}
          </div>
        ) : (
          <>
            {/* ── Pending Orders ─────────────────────────────────────────── */}
            <section className="bg-white rounded-xl p-6 shadow-lg mb-6">
              <h2 className="text-xl font-bold text-orange-600 mb-5 flex items-center gap-2">
                <span className="w-2 h-2 bg-orange-500 rounded-full animate-pulse inline-block" />
                Pending Orders
                {pendingCount > 0 && (
                  <span className="ml-2 bg-orange-100 text-orange-700 text-xs font-bold px-2 py-0.5 rounded-full">
                    {pendingCount}
                  </span>
                )}
              </h2>

              {pendingCount === 0 ? (
                <EmptyState
                  icon="📭"
                  title="No pending orders"
                  subtitle="New orders will appear here instantly — no refresh needed!"
                />
              ) : (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {Object.entries(orders.pending).flatMap(([serviceId, orderList]) => 
                    orderList.map(order => (
                      <OrderCard
                        key={`${order.orderId}-${serviceId}`}
                        serviceId={serviceId}
                        order={order}
                        onAccept={handleAccept}
                        onReject={handleReject}
                        onStart={handleStart}
                        onComplete={handleComplete}
                      />
                    ))
                  )}
                </div>
              )}
            </section>

            {/* ── Completed Orders ─────────────────────────────────────────── */}
            <section className="bg-white rounded-xl p-6 shadow-lg">
              <h2 className="text-xl font-bold text-green-600 mb-5">
                ✓ Completed Orders
                {completedCount > 0 && (
                  <span className="ml-2 bg-green-100 text-green-700 text-xs font-bold px-2 py-0.5 rounded-full">
                    {completedCount}
                  </span>
                )}
              </h2>

              {completedCount === 0 ? (
                <EmptyState
                  icon="🏁"
                  title="No completed orders yet"
                  subtitle="Accept your first order to get started!"
                />
              ) : (
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {Object.entries(orders.completed).flatMap(([serviceId, orderList]) => 
                    orderList.map(order => (
                      <OrderCard
                        key={`completed-${order.orderId}-${serviceId}`}
                        serviceId={serviceId}
                        order={order}
                        onAccept={handleAccept}
                        onReject={handleReject}
                        completed
                      />
                    ))
                  )}
                </div>
              )}
            </section>
          </>
        )}
      </div>
    </Layout>
  );
}

export default AgentHome;