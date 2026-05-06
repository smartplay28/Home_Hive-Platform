import Layout from '../../layout.jsx';
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const AdminProfile = () => {

  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Checks if the admin is authenticated
  useEffect(() => {
    const adminToken = localStorage.getItem("adminId");
    if (!adminToken) {
      navigate("/admin/SignIn");  // Redirect to login if no token is found
    } else {
      setIsAuthenticated(true);   // Mark as authenticated
    }
  }, [navigate]);

  // Only render the page if the admin is authenticated
  if (!isAuthenticated) {
    return null;
  }

  return (
    <Layout>
      <div className="admin-home bg-[#eaf0f7] min-h-screen p-6">
        <h1 className="text-[#1c4e80] text-3xl font-bold mb-4">Welcome to the Admin Profile Page</h1>
        <p className="text-gray-700 mb-6">Manage and oversee site content with ease.</p>
      </div>
    </Layout>
  );
}

export default AdminProfile;
