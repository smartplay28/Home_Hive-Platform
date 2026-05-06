import React, { useState, useEffect } from 'react';
import Layout from '../../layout.jsx'
import { useNavigate } from 'react-router-dom';
import { ServicesList } from '../../../customer/components/ServicesList';
import api from '../../../lib/api';

function GardeningLandscapingPage() {
  const [activeCategory, setActiveCategory] = useState('gardening');
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
            Gardening & Landscaping Services
          </h1>

          <div className="flex justify-center mb-6">
            <button
              onClick={() => setActiveCategory('gardening')}
              className={`px-6 py-2 mr-2 rounded-lg ${activeCategory === 'gardening' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'}`}
            >
              Design Services
            </button>
            <button
              onClick={() => setActiveCategory('planting')}
              className={`px-6 py-2 rounded-lg ${activeCategory === 'planting' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700'}`}
            >
              Planting & Installation
            </button>
          </div>

          <ServicesList
            category={activeCategory}
            onBookService={addServiceToCart}
            onRemoveService={removeFromCart}
            cartItems={cartItems}
          />

          {/* Service Notes */}
          <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-100 p-4 rounded-lg">
              <p className="text-blue-800">
                🌿 Free garden consultation available
              </p>
            </div>
            <div className="bg-blue-100 p-4 rounded-lg">
              <p className="text-blue-800">
                ✓ Expert horticulturists & landscapers
              </p>
            </div>
            <div className="bg-blue-100 p-4 rounded-lg">
              <p className="text-blue-800">
                💧 Sustainable & eco-friendly practices
              </p>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default GardeningLandscapingPage;