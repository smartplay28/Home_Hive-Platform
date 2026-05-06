import { services } from '../../components/servicesData.jsx';
import { ServiceCard } from '../../components/ServiceCard';
import Layout from '../../layout.jsx';
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../../lib/api';

const LOCATIONS = [
  "Electronic City", "Bommasandra", "Kormangala", "Marathahalli", "Whitefield",
  "Hebbal", "Kengeri", "Yelahanka", "Nagawara", "Doddaballapur", "Kundalahalli",
  "Kadugodi", "Jayanagar", "K R Puram", "Majestic", "Rajarajeshwari Nagar",
  "M G Road", "Mysore Road", "Vijaynagar", "Peenya", "Attiguppe", "Kengeri Signal",
  "K R Pet Signal", "RR Nagar", "Attibele", "Hoskote", "HSR Layout",
];

const CustomerShoppingCart = () => {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [showLocationPopup, setShowLocationPopup] = useState(false);
  const [selectedLocation, setSelectedLocation] = useState('');
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const navigate = useNavigate();

  const customerId = localStorage.getItem('customerId');

  useEffect(() => {
    if (!customerId) {
      navigate('/customer/SignIn');
    } else {
      setIsAuthenticated(true);
    }
  }, [navigate, customerId]);

  useEffect(() => {
    if (!isAuthenticated) return;

    const fetchCartItems = async () => {
      try {
        const response = await api.get(`/orders/cart/${customerId}`);
        const cartItemIds = response.data.data.cartItems;

        const cartServices = Object.values(services)
          .flat()
          .filter((service) => cartItemIds.includes(service.id));

        setCartItems(cartServices);
      } catch (err) {
        setError('Failed to load cart. Please refresh.');
      } finally {
        setLoading(false);
      }
    };

    fetchCartItems();
  }, [isAuthenticated, customerId]);

  const removeFromCart = async (serviceId) => {
    try {
      await api.post('/orders/cart/remove', {
        serviceId,
        customerId: customerId,
      });
      setCartItems((prev) => prev.filter((item) => item.id !== serviceId));
    } catch (err) {
      setError('Failed to remove item. Please try again.');
    }
  };

  const proceedToCheckout = async () => {
    if (!selectedLocation) {
      setError('Please select a location.');
      return;
    }

    setCheckoutLoading(true);
    const totalPrice = cartItems.reduce((total, item) => {
      return total + Number(item.price.replace('₹', ''));
    }, 0);

    try {
      await api.post('/orders/checkout', {
        customerId: customerId,
        totalprice: totalPrice,
        location: selectedLocation,
      });
      setShowLocationPopup(false);
      navigate('/customer/home');
    } catch (err) {
      setError(err.response?.data?.message || 'Checkout failed. Please try again.');
    } finally {
      setCheckoutLoading(false);
    }
  };

  const cartTotal = cartItems.reduce((total, item) =>
    total + Number(item.price.replace('₹', '')), 0);

  const LocationPopup = () => (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white p-6 rounded-lg shadow-xl w-96">
        <h2 className="text-xl font-bold text-[#1c4e80] mb-4">Select Your Location</h2>
        <select
          value={selectedLocation}
          onChange={(e) => setSelectedLocation(e.target.value)}
          className="w-full p-2 border border-gray-300 rounded mb-4"
        >
          <option value="">Select a location</option>
          {LOCATIONS.map((loc) => (
            <option key={loc} value={loc}>{loc}</option>
          ))}
        </select>
        {error && <p className="text-red-600 text-sm mb-3">{error}</p>}
        <div className="flex justify-end gap-2">
          <button
            onClick={() => { setShowLocationPopup(false); setError(''); }}
            className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
          >
            Cancel
          </button>
          <button
            onClick={proceedToCheckout}
            disabled={checkoutLoading}
            className="px-4 py-2 bg-[#1c4e80] text-white rounded hover:bg-[#153a61] disabled:opacity-60"
          >
            {checkoutLoading ? 'Placing Order...' : 'Confirm Checkout'}
          </button>
        </div>
      </div>
    </div>
  );

  return (
    <Layout>
      <div className="min-h-screen bg-white py-8">
        <div className="container mx-auto px-4">
          <h1 className="text-3xl font-bold text-[#1c4e80] mb-6 text-center">Your Shopping Cart</h1>

          {error && !showLocationPopup && (
            <div className="mb-4 p-3 bg-red-50 border border-red-300 text-red-700 rounded-lg text-sm text-center">
              {error}
            </div>
          )}

          {loading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="h-48 bg-gray-200 rounded-lg animate-pulse" />
              ))}
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {cartItems.map((service) => (
                  <ServiceCard
                    key={service.id}
                    service={service}
                    onRemove={removeFromCart}
                    isInCart={true}
                  />
                ))}
              </div>

              {cartItems.length === 0 && (
                <div className="text-center py-16 text-gray-500">
                  <p className="text-xl mb-2">Your cart is empty</p>
                  <p className="text-sm">Browse services and add them to your cart.</p>
                </div>
              )}

              {cartItems.length > 0 && (
                <div className="mt-8 bg-white p-6 rounded-lg shadow-md">
                  <div className="flex justify-between items-center">
                    <h2 className="text-xl font-bold text-[#1c4e80]">Cart Total</h2>
                    <p className="text-2xl font-bold text-[#1c4e80]">₹{cartTotal}</p>
                  </div>
                  <button
                    onClick={() => { setError(''); setShowLocationPopup(true); }}
                    className="w-full mt-4 bg-[#1c4e80] text-white py-3 rounded-lg hover:bg-[#153a61] transition-colors"
                  >
                    Proceed to Checkout
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </div>
      {showLocationPopup && <LocationPopup />}
    </Layout>
  );
};

export default CustomerShoppingCart;
