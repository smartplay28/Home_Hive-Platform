import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import Login from './authentication/login.jsx';

import AdminSignIn from './admin/webPages/signIn/page.jsx';
import CustomerSignIn from './customer/webPages/signIn/page.jsx';
import AgentSignIn from './serviceAgent/webPages/signIn/page.jsx';

import CustomerSignUp from './customer/webPages/signUp/page.jsx';

import AdminHome from './admin/webPages/home/page.jsx';

import AgentHome from './serviceAgent/webPages/home/page.jsx';
import AgentProfile from './serviceAgent/webPages/profile/page.jsx';
import AgentSchedule from './serviceAgent/webPages/schedule/page.jsx';

import CustomerHome from './customer/webPages/home/page.jsx';
import CustomerProfile from './customer/webPages/profile/page.jsx';
import CustomerShoppingCart from './customer/webPages/cart/page.jsx';
import OrderHistory from './customer/webPages/orderHistory/page.jsx';

import ACRepairPage from './customer/webPages/services/ACRepair.jsx';
import CleaningPestPage from './customer/webPages/services/CleanPest.jsx';
import EPCPage from './customer/webPages/services/epc.jsx';
import PaintingPage from './customer/webPages/services/paintdecor.jsx';
import SalonPage from './customer/webPages/services/Salon.jsx';
import GardeningPage from './customer/webPages/services/GardenLands.jsx';

import CustomerAboutUs from './customer/webPages/aboutUs/page.jsx';
import AgentAboutUs from './serviceAgent/webPages/aboutUs/page.jsx';

const App = () => {
  return (
    <Router>
      {/*
        Global toast container — pages don't need their own unless they need different positioning.
        Individual pages (AgentHome, OrderHistory) have their own for local overrides.
      */}
      <ToastContainer
        position="top-right"
        autoClose={5000}
        hideProgressBar={false}
        newestOnTop
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
        limit={5}
      />

      <Routes>
        {/* Initial Login Selection Page */}
        <Route path="/" element={<Login />} />

        {/* Separate Sign-In Pages for Each User Type */}
        <Route path="/customer/SignIn" element={<CustomerSignIn />} />
        <Route path="/agent/SignIn" element={<AgentSignIn />} />
        <Route path="/admin/SignIn" element={<AdminSignIn />} />

        {/* Customer Sign-Up Page */}
        <Route path="/customer/SignUp" element={<CustomerSignUp />} />

        {/* Home, Order History and Profile Pages for Each User Type */}
        <Route path="/customer/home" element={<CustomerHome />} />
        <Route path="/customer/profile" element={<CustomerProfile />} />
        <Route path="/customer/order-history" element={<OrderHistory />} />

        <Route path="/agent/home" element={<AgentHome />} />
        <Route path="/agent/profile" element={<AgentProfile />} />
        <Route path="/agent/schedule" element={<AgentSchedule />} />

        <Route path="/admin/home" element={<AdminHome />} />

        {/* Customer Service Pages */}
        <Route path="/customer/services/ACRepair" element={<ACRepairPage />} />
        <Route path="/customer/services/cleaningpest" element={<CleaningPestPage />} />
        <Route path="/customer/services/epc" element={<EPCPage />} />
        <Route path="/customer/services/painting" element={<PaintingPage />} />
        <Route path="/customer/services/salon" element={<SalonPage />} />
        <Route path="/customer/services/gardening" element={<GardeningPage />} />

        {/* Customer Shopping Cart */}
        <Route path="/customer/cart" element={<CustomerShoppingCart />} />

        {/* About Us */}
        <Route path="/customer/aboutUs" element={<CustomerAboutUs />} />
        <Route path="/agent/aboutUs" element={<AgentAboutUs />} />
      </Routes>
    </Router>
  );
};

export default App;
