import React, { useState, useEffect, useCallback } from 'react';
import Layout from '../../layout.jsx';
import { useNavigate } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { services } from '../../components/servicesData';
import api, { getUser } from '../../../lib/api.js';
import { useOrderTracking } from '../../hooks/useOrderTracking.jsx';

/* ─── Status config ─────────────────────────────────────────────────────────── */
const STATUS_CONFIG = {
  PENDING_NOT_ASSIGNED: {
    label: 'Finding Agent',
    color: 'bg-yellow-100 text-yellow-700 border-yellow-300',
    dot: 'bg-yellow-400 animate-pulse',
    icon: '🔍',
    step: 1,
  },
  AGENT_ASSIGNED: {
    label: 'Agent Assigned',
    color: 'bg-blue-100 text-blue-700 border-blue-300',
    dot: 'bg-blue-500',
    icon: '👷',
    step: 2,
  },
  IN_PROGRESS: {
    label: 'In Progress',
    color: 'bg-indigo-100 text-indigo-700 border-indigo-300',
    dot: 'bg-indigo-500 animate-pulse',
    icon: '🔧',
    step: 3,
  },
  COMPLETED: {
    label: 'Completed',
    color: 'bg-green-100 text-green-700 border-green-300',
    dot: 'bg-green-500',
    icon: '✅',
    step: 4,
  },
  RATED: {
    label: 'Rated ★',
    color: 'bg-emerald-100 text-emerald-700 border-emerald-300',
    dot: 'bg-emerald-500',
    icon: '⭐',
    step: 4,
  },
  NO_AGENTS_AVAILABLE: {
    label: 'No Agents',
    color: 'bg-red-100 text-red-700 border-red-300',
    dot: 'bg-red-400',
    icon: '⚠️',
    step: 0,
  },
};

/* ─── Star Rating Component ──────────────────────────────────────────────────── */
const StarRating = ({ rating, onRate, size = 'text-3xl' }) => {
  const [hover, setHover] = useState(0);

  return (
    <div className="flex gap-1 justify-center">
      {[1, 2, 3, 4, 5].map(star => (
        <button
          key={star}
          onClick={() => onRate(star)}
          onMouseEnter={() => setHover(star)}
          onMouseLeave={() => setHover(0)}
          className={`${size} transition-all duration-150 transform hover:scale-125 cursor-pointer
            ${star <= (hover || rating) ? 'text-yellow-400 drop-shadow-md' : 'text-gray-300'}`}
        >
          ★
        </button>
      ))}
    </div>
  );
};

/* ─── Rating & Payment Modal ─────────────────────────────────────────────────── */
const RatingModal = ({ order, onClose, onSubmitSuccess }) => {
  const [step, setStep] = useState('rate'); // 'rate' | 'payment' | 'success'
  const [rating, setRating] = useState(0);
  const [submitting, setSubmitting] = useState(false);
  const [agentName, setAgentName] = useState('');
  const [newAvgRating, setNewAvgRating] = useState(0);

  const handleSubmitRating = async () => {
    if (rating === 0) {
      toast.warning('Please select a rating!');
      return;
    }
    setSubmitting(true);
    try {
      const res = await api.post('/orders/rate', {
        orderId: order.orderId,
        rating,
      });
      setAgentName(res.data.data?.agentName || 'Your Agent');
      setNewAvgRating(res.data.data?.newAvgRating || rating);
      setStep('payment');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit rating.');
    } finally {
      setSubmitting(false);
    }
  };

  const handlePayment = () => {
    setStep('success');
    setTimeout(() => {
      onSubmitSuccess();
      onClose();
    }, 3000);
  };

  const ratingLabels = ['', 'Poor', 'Fair', 'Good', 'Great', 'Excellent'];

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
         onClick={onClose}>
      <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full overflow-hidden transform transition-all"
           onClick={e => e.stopPropagation()}>

        {/* ─── Step 1: Rate ───── */}
        {step === 'rate' && (
          <div className="p-8">
            <div className="text-center mb-6">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-3">
                <span className="text-3xl">✅</span>
              </div>
              <h2 className="text-2xl font-bold text-gray-800">Service Complete!</h2>
              <p className="text-gray-500 mt-1 text-sm">How was your experience?</p>
            </div>

            <div className="bg-gray-50 rounded-xl p-4 mb-6">
              <p className="text-xs text-gray-400 font-mono">Order #{order.orderId}</p>
              <p className="text-sm font-semibold text-gray-700 mt-1">
                {order.cart?.map(id => {
                  const s = Object.values(services).flat().find(s => s.id === id);
                  return s?.name || id;
                }).join(', ')}
              </p>
              <p className="text-xs text-gray-500 mt-1">📍 {order.location}</p>
            </div>

            <StarRating rating={rating} onRate={setRating} />
            {rating > 0 && (
              <p className="text-center text-sm font-medium text-gray-600 mt-2 animate-fade-in">
                {ratingLabels[rating]}
              </p>
            )}

            <button
              onClick={handleSubmitRating}
              disabled={submitting || rating === 0}
              className="w-full mt-6 bg-[#1c4e80] hover:bg-[#153a60] disabled:bg-gray-300
                text-white font-semibold py-3 rounded-xl transition-all duration-200
                flex items-center justify-center gap-2"
            >
              {submitting ? (
                <><span className="animate-spin">⏳</span> Submitting...</>
              ) : (
                <>Submit Rating & Continue to Payment</>
              )}
            </button>
          </div>
        )}

        {/* ─── Step 2: Payment ───── */}
        {step === 'payment' && (
          <div className="p-8">
            <div className="text-center mb-6">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
                <span className="text-3xl">💳</span>
              </div>
              <h2 className="text-2xl font-bold text-gray-800">Payment</h2>
              <p className="text-gray-500 mt-1 text-sm">Complete your payment</p>
            </div>

            <div className="bg-gradient-to-br from-[#1c4e80] to-[#2d7abc] rounded-xl p-5 text-white mb-6">
              <p className="text-sm text-white/70">Total Amount</p>
              <p className="text-3xl font-bold mt-1">₹{order.totalPrice}</p>
              <div className="flex justify-between items-center mt-4 text-sm">
                <span className="text-white/70">Agent: {agentName}</span>
                <span className="flex items-center gap-1">
                  {'★'.repeat(rating)}{'☆'.repeat(5 - rating)}
                </span>
              </div>
            </div>

            {/* Payment options */}
            <div className="space-y-3 mb-6">
              {[
                { icon: '💳', name: 'Credit/Debit Card', sub: '•••• 4242' },
                { icon: '📱', name: 'UPI', sub: 'Google Pay / PhonePe' },
                { icon: '💵', name: 'Cash on Delivery', sub: 'Pay in person' },
              ].map((method, i) => (
                <label key={i}
                  className="flex items-center gap-3 border rounded-xl p-3 cursor-pointer
                    hover:border-[#1c4e80] hover:bg-blue-50/50 transition-all">
                  <input type="radio" name="payment" defaultChecked={i === 0}
                    className="accent-[#1c4e80]" />
                  <span className="text-xl">{method.icon}</span>
                  <div>
                    <p className="text-sm font-semibold text-gray-700">{method.name}</p>
                    <p className="text-xs text-gray-400">{method.sub}</p>
                  </div>
                </label>
              ))}
            </div>

            <button
              onClick={handlePayment}
              className="w-full bg-green-500 hover:bg-green-600 text-white font-bold py-3 rounded-xl
                transition-all duration-200 flex items-center justify-center gap-2 text-lg"
            >
              💳 Pay ₹{order.totalPrice}
            </button>
          </div>
        )}

        {/* ─── Step 3: Success ───── */}
        {step === 'success' && (
          <div className="p-8 text-center">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4
              animate-bounce">
              <span className="text-4xl">🎉</span>
            </div>
            <h2 className="text-2xl font-bold text-green-600 mb-2">Payment Successful!</h2>
            <p className="text-gray-500 mb-4">Thank you for using HomeHive</p>

            <div className="bg-green-50 rounded-xl p-4 mb-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-500">Amount Paid</span>
                <span className="font-bold text-green-700">₹{order.totalPrice}</span>
              </div>
              <div className="flex justify-between text-sm mt-2">
                <span className="text-gray-500">Agent Rating</span>
                <span className="font-bold text-yellow-600">
                  {'★'.repeat(rating)} ({ratingLabels[rating]})
                </span>
              </div>
              <div className="flex justify-between text-sm mt-2">
                <span className="text-gray-500">Agent Avg Rating</span>
                <span className="font-bold text-blue-600">{newAvgRating} ★</span>
              </div>
            </div>

            <p className="text-xs text-gray-400 animate-pulse">Redirecting back...</p>
          </div>
        )}
      </div>
    </div>
  );
};

/* ─── Progress Stepper ───────────────────────────────────────────────────────── */
const StatusStepper = ({ status }) => {
  const steps = ['Finding Agent', 'Agent Assigned', 'In Progress', 'Completed'];
  const currentStep = STATUS_CONFIG[status]?.step || 0;

  return (
    <div className="flex items-center gap-0 mt-4 mb-2">
      {steps.map((step, idx) => {
        const stepNum = idx + 1;
        const done = stepNum < currentStep;
        const active = stepNum === currentStep;

        return (
          <React.Fragment key={step}>
            <div className="flex flex-col items-center">
              <div className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold border-2
                transition-all duration-500
                ${done    ? 'bg-green-500 border-green-500 text-white'
                : active  ? 'bg-[#1c4e80] border-[#1c4e80] text-white scale-110'
                          : 'bg-gray-100 border-gray-300 text-gray-400'}`}>
                {done ? '✓' : stepNum}
              </div>
              <p className={`text-[10px] mt-1 text-center max-w-[56px] leading-tight
                ${active ? 'text-[#1c4e80] font-semibold' : 'text-gray-400'}`}>
                {step}
              </p>
            </div>
            {idx < steps.length - 1 && (
              <div className={`flex-1 h-0.5 mx-1 mb-5 transition-all duration-500
                ${done ? 'bg-green-400' : 'bg-gray-200'}`} />
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
};

/* ─── Order Card ─────────────────────────────────────────────────────────────── */
const OrderCard = ({ order, isLive, onRateClick }) => {
  const [currentStatus, setCurrentStatus] = useState(order.orderStatus);
  const config = STATUS_CONFIG[currentStatus] || STATUS_CONFIG['PENDING_NOT_ASSIGNED'];

  useEffect(() => {
    setCurrentStatus(order.orderStatus);
  }, [order.orderStatus]);

  const isCompleted = currentStatus === 'COMPLETED';
  const isRated = currentStatus === 'RATED';

  return (
    <div className={`bg-white rounded-2xl shadow-md border overflow-hidden transition-all duration-300
      ${isLive ? 'ring-2 ring-[#1c4e80] ring-offset-1' : ''}`}>

      {/* Card header */}
      <div className="bg-gradient-to-r from-[#1c4e80] to-[#2d7abc] p-4">
        <div className="flex justify-between items-center">
          <div>
            <p className="text-white/70 text-xs font-mono">Order #{order.orderId}</p>
            <p className="text-white font-bold text-lg mt-0.5">₹{order.totalPrice}</p>
          </div>
          <span className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full border text-xs font-semibold
            ${config.color}`}>
            <span className={`w-1.5 h-1.5 rounded-full ${config.dot}`} />
            {config.icon} {config.label}
          </span>
        </div>
      </div>

      {/* Card body */}
      <div className="p-4">
        <StatusStepper status={currentStatus} />

        <div className="flex items-center gap-2 text-sm text-gray-600 mb-3 mt-2">
          <span>📍</span>
          <span>{order.location}</span>
        </div>

        <div className="border-t pt-3">
          <p className="text-xs font-semibold text-gray-500 mb-2 uppercase tracking-wide">Services</p>
          <ul className="space-y-1.5">
            {order.cart?.map((serviceId, idx) => {
              const service = Object.values(services).flat().find(s => s.id === serviceId);
              return (
                <li key={idx} className="flex justify-between items-center bg-gray-50 px-3 py-2 rounded-lg">
                  <span className="text-sm text-gray-700">{service?.name || serviceId}</span>
                  <span className="text-sm font-semibold text-gray-800">{service?.price || ''}</span>
                </li>
              );
            })}
          </ul>
        </div>

        {/* Rate & Pay button — only for completed (not yet rated) orders */}
        {isCompleted && (
          <button
            onClick={() => onRateClick(order)}
            className="w-full mt-4 bg-gradient-to-r from-yellow-400 to-orange-400 hover:from-yellow-500 hover:to-orange-500
              text-white font-bold py-3 rounded-xl transition-all duration-200
              flex items-center justify-center gap-2 shadow-md hover:shadow-lg transform hover:scale-[1.02]"
          >
            ⭐ Rate & Pay
          </button>
        )}

        {isRated && (
          <div className="mt-4 bg-emerald-50 border border-emerald-200 rounded-xl p-3 text-center">
            <p className="text-emerald-700 font-semibold text-sm">✅ Rated & Paid</p>
          </div>
        )}
      </div>
    </div>
  );
};

/* ─── Order History Page ─────────────────────────────────────────────────────── */
const OrderHistory = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [liveOrderId, setLiveOrderId] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [ratingOrder, setRatingOrder] = useState(null); // order being rated
  const navigate = useNavigate();

  const customerId = getUser().userId || localStorage.getItem('customerId');

  useEffect(() => {
    if (!customerId) {
      navigate('/customer/SignIn');
    } else {
      setIsAuthenticated(true);
    }
  }, [navigate, customerId]);

  const fetchOrders = useCallback(async () => {
    try {
      const response = await api.post('/orders/history', {
        customerId: customerId,
      });
      setOrders(response.data.data?.orders || response.data.data || []);
    } catch (err) {
      setError('Failed to fetch orders. Please refresh.');
    } finally {
      setLoading(false);
    }
  }, [customerId]);

  useEffect(() => {
    if (isAuthenticated) fetchOrders();
  }, [isAuthenticated, fetchOrders]);

  /* ─── Real-time order status tracking via WebSocket ───────────────────────── */
  const handleStatusUpdate = useCallback((update) => {
    setLiveOrderId(update.orderId);

    setOrders(prev =>
      prev.map(order =>
        order.orderId === update.orderId
          ? { ...order, orderStatus: update.newStatus }
          : order
      )
    );

    // Show toast for completed orders
    if (update.newStatus === 'COMPLETED') {
      toast.success('🎉 Your service is complete! Rate your agent.', { position: 'top-center' });
    }

    setTimeout(() => setLiveOrderId(null), 5000);
  }, []);

  useOrderTracking(customerId, handleStatusUpdate);

  const handleRateClick = (order) => {
    setRatingOrder(order);
  };

  const handleRatingSuccess = () => {
    fetchOrders(); // refresh to show RATED status
    toast.success('Payment successful! Thank you! 🎉');
  };

  if (!isAuthenticated) return null;

  return (
    <Layout>
      <ToastContainer position="bottom-right" limit={3} />

      {/* Rating Modal */}
      {ratingOrder && (
        <RatingModal
          order={ratingOrder}
          onClose={() => setRatingOrder(null)}
          onSubmitSuccess={handleRatingSuccess}
        />
      )}

      <div className="bg-gradient-to-b from-white to-[#eaf0f7] min-h-screen py-12 px-6">

        {/* Header */}
        <div className="max-w-6xl mx-auto mb-10">
          <h1 className="text-3xl font-bold text-gray-800 text-center">Your Order History</h1>
          <p className="text-gray-500 text-center mt-2 text-sm">
            Order status updates in real time — no refresh needed
          </p>
        </div>

        <div className="max-w-6xl mx-auto">
          {loading && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="h-64 bg-white rounded-2xl animate-pulse shadow-md" />
              ))}
            </div>
          )}

          {error && (
            <div className="text-red-600 text-center p-6 bg-red-50 rounded-2xl border border-red-200">
              {error}
            </div>
          )}

          {!loading && !error && orders.length === 0 && (
            <div className="text-center py-20 text-gray-400">
              <div className="text-6xl mb-4">📋</div>
              <p className="text-xl font-medium text-gray-500">No orders yet</p>
              <p className="text-sm mt-2">Book a service to see your orders here.</p>
            </div>
          )}

          {!loading && !error && orders.length > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {orders.map(order => (
                <OrderCard
                  key={order.id || order.orderId}
                  order={order}
                  isLive={liveOrderId === order.orderId}
                  onRateClick={handleRateClick}
                />
              ))}
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default OrderHistory;
