import React, { useState, useEffect } from 'react';
import Layout from '../../layout.jsx'
import { useNavigate } from 'react-router-dom';
import { ServicesList } from '../../../customer/components/ServicesList';
import api from '../../../lib/api';

function EPCServicesPage() {
  const [activeCategory, setActiveCategory] = useState('electrical');
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [cartItems, setCartItems] = useState(new Set());
  const customerToken = localStorage.getItem('customerId');

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const role = localStorage.getItem('userRole');
    if (!token || role !== 'CUSTOMER') {
      navigate("/customer/SignIn");
    } else {
      setIsAuthenticated(true);
    }
  }, [navigate]);

  useEffect(() => {
    const fetchCartItems = async () => {
      const customerId = localStorage.getItem('userId');
      try {
        const response = await api.get(`/orders/cart/${customerId}`);
        const { data } = response.data;
        setCartItems(new Set(data.cartItems));
      } catch (error) {
        console.error('Error fetching cart items:', error);
      }
    };
    if (isAuthenticated) fetchCartItems();
  }, [isAuthenticated]);

  const addServiceToCart = async (serviceId) => {
    const customerId = localStorage.getItem('userId');
    try {
      await api.post('/orders/cart/add', { serviceId, customerId: customerId });
      setCartItems(prev => new Set([...prev, serviceId]));
    } catch (error) { alert("Error adding to cart"); }
  }

  const removeFromCart = async (serviceId) => {
    const customerId = localStorage.getItem('userId');
    try {
      await api.post('/orders/cart/remove', { serviceId, customerId: customerId });
      setCartItems(prev => { const s = new Set(prev); s.delete(serviceId); return s; });
    } catch (error) { alert("Error removing from cart"); }
  }

  if (!isAuthenticated) return null;

  return (
    <Layout>
      <div className="flex flex-col min-h-screen bg-gray-100">
        <div className="container mx-auto px-4 py-8 flex-grow">
          <h1 className="text-3xl font-bold text-center mb-6 text-blue-600">
            Home Services
          </h1>

          <div className="flex justify-center mb-6 space-x-4">
            <button
              onClick={() => setActiveCategory('electrical')}
              className={`px-6 py-2 rounded-lg transition-colors duration-200 ${activeCategory === 'electrical'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
            >
              Electrical
            </button>

            <button
              onClick={() => setActiveCategory('carpenter')}
              className={`px-6 py-2 rounded-lg transition-colors duration-200 ${activeCategory === 'carpenter'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
            >
              Carpenter
            </button>
          </div>

          <ServicesList
            category={activeCategory}
            onBookService={addServiceToCart}
            onRemoveService={removeFromCart}
            cartItems={cartItems}
          />

          <div className="mt-6 text-center bg-blue-100 p-4 rounded-lg border border-blue-300">
            <p className="text-blue-800 font-semibold">
              🏠 Professional Home Services Available 24/7
            </p>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default EPCServicesPage;