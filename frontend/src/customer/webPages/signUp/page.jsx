import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import logo from '../../../assets/logo.jpg';
import api, { saveAuth } from '../../../lib/api';

const CustomerSignUp = () => {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (password !== confirmPassword) {
        alert("Passwords do not match");
        return;
      }
      const response = await api.post('/auth/customer/register', {
        name,
        email,
        phone: phone,
        password,
      });

      const { data } = response.data; // ApiResponse<LoginResponse>
      saveAuth(data);
      navigate('/customer/home');
    } catch (error) {
      const msg = error.response?.data?.message || "Registration failed. Please try again.";
      alert(msg);
    }
  }

  return (
    <div className="min-h-screen bg-[#eaf0f7] p-8">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-[#1c4e80] hover:text-[#2d7abc] mb-8"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          className="h-5 w-5"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          />
        </svg>
        Back
      </button>

      {/* Logo and Company Name */}
      <div className="flex flex-col items-center mb-7">
        <img
          src={logo}
          alt="HomeHive Logo"
          className="w-24 h-24 rounded-full mb-4"
        />
        <h1 className="text-3xl font-bold text-[#1c4e80]">HomeHive</h1>
      </div>

      {/* Intro Section */}
      <div className="text-center mb-3 ml-4">
        <h2 className="text-2xl font-semibold text-[#1c4e80]">Ready to embark on a new journey with us?</h2>
        {/* <h2 className="text-2xl font-semibold text-[#1c4e80]">We're excited to have you back!</h2>  */}
      </div>

      {/* Sign Up Form */}
      <div className="max-w-md mx-auto bg-white rounded-lg shadow-lg p-8">
        <h2 className="text-2xl font-semibold text-[#1c4e80] mb-8">Let's go!</h2>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <input
              type="text"
              placeholder="Name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none transition"
            />
          </div>

          <div>
            <input
              type="email"
              placeholder="Email ID"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none transition"
            />
          </div>

          <div>
            <input
              type="tel"
              placeholder="Phone Number"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none transition"
            />
          </div>


          <div>
            <input
              type="password"
              placeholder="Set Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none transition"
            />
          </div>

          <div>
            <input
              type="password"
              placeholder="Confirm Password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-[#1c4e80] focus:border-transparent outline-none transition"
            />
          </div>

          <button
            type="submit"
            className="w-full bg-[#1c4e80] text-white py-3 rounded-lg hover:bg-[#2d7abc] transition duration-300"
          >
            Sign Up
          </button>

          <div className="text-center mt-6">
            <span className="text-gray-600">Already have an account? </span>
            <Link to="/customer/SignIn">
              Sign In
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CustomerSignUp;