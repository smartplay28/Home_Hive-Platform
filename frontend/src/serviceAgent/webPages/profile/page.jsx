import React, { useState, useEffect } from 'react';
import Layout from '../../layout.jsx';
import { FaUser, FaPhone, FaEnvelope } from 'react-icons/fa';
import { useNavigate } from 'react-router-dom';
import { FaMapMarkerAlt } from "react-icons/fa";
import api from '../../../lib/api';


const AgentProfile = () => {

  const [profileData, setProfileData] = useState({
    name: '',
    email: '',
    location: '',
    range: '',
    skills: [],
    avgRating: 0,
    completedOrderCount: 0,
  });

  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    const role = localStorage.getItem('userRole');
    if (!token || role !== 'AGENT') {
      navigate("/agent/SignIn");
    } else {
      setIsAuthenticated(true);
      fetchProfileData();
    };
  }, [navigate]);

  const fetchProfileData = async () => {
    try {
      const response = await api.get('/agents/me');
      const { data } = response.data;
      setProfileData({
        name: data.name,
        email: data.email,
        location: data.location,
        range: data.range,
        skills: data.skill || [],
        avgRating: data.avgRating || 0,
        completedOrderCount: data.completedOrderCount || 0,
      });
    } catch (error) {
      console.error('Error fetching profile:', error);
      const msg = error.response?.data?.message || 'Failed to fetch profile.';
      alert(msg);
    }
  };

  if (!isAuthenticated) return null;

  return (
    <Layout>
      <div className="max-w-4xl mx-auto my-12 px-4">
        {/* Top Profile Card */}
        <div className="bg-white rounded-2xl shadow-xl overflow-hidden">
          <div className="bg-gradient-to-r from-[#1c4e80] to-[#2d7abc] px-8 py-10 text-white flex flex-col md:flex-row items-center gap-6">
            <div className="w-24 h-24 bg-white/20 rounded-full flex items-center justify-center text-4xl shadow-inner backdrop-blur-sm">
              🧑‍🔧
            </div>
            <div className="text-center md:text-left">
              <h1 className="text-3xl font-bold">{profileData.name}</h1>
              <p className="text-white/80 mt-1 flex items-center gap-2 justify-center md:justify-start">
                <FaMapMarkerAlt /> {profileData.location}
              </p>
            </div>
            <div className="md:ml-auto flex gap-4 mt-6 md:mt-0">
              <div className="bg-white/10 p-4 rounded-xl backdrop-blur-sm text-center border border-white/20">
                <p className="text-sm text-white/70 font-medium">Rating</p>
                <p className="text-2xl font-bold text-yellow-300">★ {profileData.avgRating > 0 ? profileData.avgRating.toFixed(1) : 'New'}</p>
              </div>
              <div className="bg-white/10 p-4 rounded-xl backdrop-blur-sm text-center border border-white/20">
                <p className="text-sm text-white/70 font-medium">Completed</p>
                <p className="text-2xl font-bold text-green-300">✓ {profileData.completedOrderCount}</p>
              </div>
            </div>
          </div>

          <div className="p-8 grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Contact Details */}
            <div className="space-y-6">
              <h3 className="text-xl font-bold text-gray-800 border-b pb-2">Contact Details</h3>
              <div className="flex items-center gap-4 text-gray-700">
                <div className="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center text-[#1c4e80]">
                  <FaUser />
                </div>
                <div>
                  <p className="text-sm text-gray-500">Full Name</p>
                  <p className="font-semibold">{profileData.name}</p>
                </div>
              </div>
              <div className="flex items-center gap-4 text-gray-700">
                <div className="w-10 h-10 rounded-full bg-blue-50 flex items-center justify-center text-[#1c4e80]">
                  <FaEnvelope />
                </div>
                <div>
                  <p className="text-sm text-gray-500">Email Address</p>
                  <p className="font-semibold">{profileData.email}</p>
                </div>
              </div>
            </div>

            {/* Service Area & Skills */}
            <div className="space-y-6">
              <h3 className="text-xl font-bold text-gray-800 border-b pb-2">Service Details</h3>
              <div className="flex items-center gap-4 text-gray-700">
                <div className="w-10 h-10 rounded-full bg-green-50 flex items-center justify-center text-green-600">
                  📍
                </div>
                <div>
                  <p className="text-sm text-gray-500">Service Radius</p>
                  <p className="font-semibold">{profileData.range} km from {profileData.location}</p>
                </div>
              </div>
              <div className="flex gap-4 text-gray-700">
                <div className="w-10 h-10 rounded-full bg-purple-50 flex items-center justify-center text-purple-600 shrink-0">
                  🔧
                </div>
                <div>
                  <p className="text-sm text-gray-500 mb-1">Skills & Services</p>
                  <div className="flex flex-wrap gap-2">
                    {profileData.skills.map((skill, i) => (
                      <span key={i} className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm font-medium border border-gray-200">
                        {skill}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default AgentProfile;
