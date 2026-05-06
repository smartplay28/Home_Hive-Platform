import React, { useState, useEffect } from 'react';
import Layout from '../../layout.jsx';
import { useNavigate } from 'react-router-dom';
import { ServicesList } from '../../../customer/components/ServicesList.jsx';
import api from '../../../lib/api';

function SalonPage() {
  const [activeCategory, setActiveCategory] = useState('men');
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

  // Fetch cart items on mount
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

  if (!isAuthenticated) {
    return null;
  }

  const addServiceToCart = async (serviceId) => {
    const customerId = localStorage.getItem('userId');
    try {
      await api.post('/orders/cart/add', { serviceId, customerId: customerId });
      setCartItems(prev => new Set([...prev, serviceId]));
    } catch (error) { alert("Error adding to cart"); }
  };

  const removeFromCart = async (serviceId) => {
    const customerId = localStorage.getItem('userId');
    try {
      await api.post('/orders/cart/remove', { serviceId, customerId: customerId });
      setCartItems(prev => { const s = new Set(prev); s.delete(serviceId); return s; });
    } catch (error) { alert("Error removing from cart"); }
  };

  return (
    <Layout>
      <div className="flex flex-col min-h-screen bg-gray-100">
        <div className="container mx-auto px-4 py-8 flex-grow">
          <h1 className="text-3xl font-bold text-center mb-6 text-blue-600">
            Salon Services
          </h1>

          <div className="flex justify-center mb-6">
            <button
              onClick={() => setActiveCategory('men')}
              className={`px-4 py-2 mr-2 rounded-lg ${activeCategory === 'men'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700'
                }`}
            >
              Men's Services
            </button>
            <button
              onClick={() => setActiveCategory('women')}
              className={`px-4 py-2 rounded-lg ${activeCategory === 'women'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-200 text-gray-700'
                }`}
            >
              Women's Services
            </button>
          </div>

          {ServicesList({
            category: activeCategory,
            onBookService: addServiceToCart,
            onRemoveService: removeFromCart,
            cartItems: cartItems
          })}

          <div className="mt-6 text-center bg-blue-100 p-4 rounded-lg border border-blue-300">
            <p className="text-blue-800 font-semibold">
              🏠 All Services Available for Home Delivery
            </p>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default SalonPage;