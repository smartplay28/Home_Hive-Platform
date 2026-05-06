import React from 'react';
import { Link } from 'react-router-dom';

const Login = () => {
  return (
    <div className="relative flex justify-center items-center min-h-screen bg-[#eaf0f7] p-4 overflow-hidden">

      {/* Main Login Box */}
      <div className="relative z-10 p-8 rounded-lg shadow-lg bg-[#1c4e80] text-center w-full max-w-xs">

        {/* Heading */}
        <h2 className="mb-5">
          <span style={{ fontSize: '21px', fontFamily: 'Copperplate', display: 'block', color: 'white' }}>Embarking into</span>
          <span style={{ fontSize: '36px', fontFamily: 'Copperplate', fontWeight: 'bold', color: 'white' }}>HomeHive</span>
        </h2>

        {/* Login Type Selection */}
        <p className="text-white mb-5">Select your login type:</p>

        {/* Customer Button */}
        <div className="mb-2">
          <Link to="/customer/SignIn" className="block">
            <button className="w-full py-2 rounded-md border-2 border-white bg-[#003366] text-white font-bold text-lg hover:bg-[#66b3e0] transition duration-300 hover:shadow-md">
              Customer
            </button>
          </Link>
        </div>

        {/* Agent Button */}
        <div className="mb-2">
          <Link to="/agent/SignIn" className="block">
            <button className="w-full py-2 rounded-md border-2 border-white bg-[#003366] text-white font-bold text-lg hover:bg-[#66b3e0] transition duration-300 hover:shadow-md">
              Agent
            </button>
          </Link>
        </div>

        {/* Admin Button */}
        <div className="mb-2">
          <Link to="/admin/SignIn" className="block">
            <button className="w-full py-2 rounded-md border-2 border-white bg-[#003366] text-white font-bold text-lg hover:bg-[#66b3e0] transition duration-300 hover:shadow-md">
              Admin
            </button>
          </Link>
        </div>
      </div>

      {/* Wavy Background */}
      <svg
        className="absolute bottom-[-130px] left-0 w-full h-auto"
        viewBox="0 0 1440 320"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fill="#1c4e80"
          fillOpacity="1"
          d="M0,160L48,140C96,120,192,80,288,96C384,112,480,160,576,160C672,160,768,112,864,106.7C960,101,1056,139,1152,149.3C1248,160,1344,120,1392,100L1440,80L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"

        ></path>
      </svg>

      {/* Top Wavy Background */}
      <svg
        className="absolute top-[-100px] left-0 w-full h-auto"
        viewBox="0 0 1440 320"
        xmlns="http://www.w3.org/2000/svg"
      >
        <path
          fill="#1c4e80"
          fillOpacity="1"
          d="M0,160L48,170.7C96,181,192,203,288,192C384,181,480,139,576,138.7C672,139,768,181,864,186.7C960,192,1056,160,1152,165.3C1248,171,1344,213,1392,234.7L1440,256L1440,0L1392,0C1344,0,1248,0,1152,0C1056,0,960,0,864,0C768,0,672,0,576,0C480,0,384,0,288,0C192,0,96,0,48,0L0,0Z"
        ></path>
      </svg>
    </div>
  );
};

export default Login;

