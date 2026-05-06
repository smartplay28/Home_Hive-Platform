import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import logo from '../../../assets/logo.jpg';
import api, { saveAuth } from '../../../lib/api';

const CustomerSignIn = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/customer/login', { email, password });
      const loginData = response.data.data; // ApiResponse<LoginResponse>.data

      // Save JWT tokens + user info
      saveAuth(loginData);

      navigate('/customer/home');
    } catch (err) {
      const msg = err.response?.data?.message || 'Login failed. Please try again.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#eaf0f7] p-8">
      {/* Back Button */}
      <Link to="/">
        <button className="flex items-center gap-2 text-[#1c4e80] hover:text-[#2d7abc] mb-8">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none"
            viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back
        </button>
      </Link>

      {/* Logo */}
      <div className="flex flex-col items-center mb-6">
        <img src={logo} alt="HomeHive Logo" className="w-24 h-24 rounded-full mb-4" />
        <h1 className="text-3xl font-bold text-[#1c4e80]">HomeHive</h1>
      </div>

      <div className="text-center mb-3">
        <h2 className="text-2xl font-semibold text-[#1c4e80]">We're excited to have you back!</h2>
      </div>

      {/* Sign In Form */}
      <div className="max-w-md mx-auto bg-white rounded-lg shadow-lg p-8">
        <h2 className="text-2xl font-semibold text-[#1c4e80] mb-8">Welcome Back!</h2>

        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-300 text-red-700 rounded-lg text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <input
            type="email"
            placeholder="Email ID"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none transition"
          />

          <input
            type="password"
            placeholder="Password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
            className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none transition"
          />

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-[#1c4e80] text-white py-3 rounded-lg hover:bg-[#2d7abc] transition duration-300 disabled:opacity-60 disabled:cursor-not-allowed"
          >
            {loading ? 'Signing in...' : 'Sign In'}
          </button>

          <div className="text-center mt-6">
            <span className="text-gray-600">Don't have an account? </span>
            <Link to="/customer/SignUp" className="text-blue-600 hover:text-blue-800 transition-colors duration-300">
              Sign Up
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CustomerSignIn;