import { Link } from 'react-router-dom';
import React, { useState } from 'react';
import logo from '../../assets/logo.jpg';

/**
 * This is the navbar for the admin panel
 */
const NavBar = () => {
  return (
    <div>
      <header className="bg-[#1c4e80] py-2 sticky top-0 w-full z-10">
        <div className="max-w-full mx-auto px-8">
          <nav className="flex items-center justify-between gap-5">
            {/* Left Section: Logo and Brand Name */}
            <Link to="/admin/home">
              <div className="flex items-center gap-5">
                {/* Logo */}
                <div>
                  <img src={logo} alt="Logo" className="h-[65px] w-[65px] rounded-full" />
                </div>
                <h1 className="text-[#e8f0e5] font-medium text-xl">HomeHive</h1>
              </div>
            </Link>

            {/* Right Section: Agent List and Profile */}
            <div className="flex items-center gap-8">
              {/* Profile Section */}
              <div className="flex items-center gap-5">
                <Link to="/admin/SignIn">
                  <button
                    onClick={() => {
                      localStorage.removeItem("adminId"); // Remove the admin token from local storage
                    }}
                    className="text-white border border-white px-4 py-1 rounded transition duration-300 hover:bg-[#66b3e0] hover:text-white text-lg"
                  >
                    Logout
                  </button>
                </Link>
              </div>
            </div>
          </nav>
        </div>
      </header>
    </div>
  );
};

export default NavBar;