import { Link } from 'react-router-dom';
import React from 'react';
import logo from '../../assets/logo.jpg';
import { FaShoppingCart } from 'react-icons/fa';

const NavBar = () => {
    const handleLogout = () => {
        localStorage.removeItem('customerId');
    };

    return (
        <div>
            <header className="bg-[#1c4e80] py-2 sticky top-0 w-full z-10">
                <div className="max-w-full mx-auto px-8">
                    <nav className="flex items-center justify-between gap-5">
                        {/* Left Section: Logo and Brand Name */}
                        <div className="flex items-center gap-5">
                            {/* Logo */}
                            <Link to="/customer/home">
                                <div>
                                    <img src={logo} alt="Logo" className="h-[65px] w-[65px] rounded-full" />
                                </div>
                            </Link>
                            <Link to="/customer/home">
                                <h1 className="text-[#e8f0e5] font-medium text-xl">HomeHive</h1>
                            </Link>
                        </div>

                        {/* Right Section: Navigation, Shopping Cart and Profile */}
                        <div className="flex items-center gap-8">
                            <div className="flex items-center gap-8">
                                <Link to="/customer/order-history" className="text-[#e8f0e5] font-medium cursor-pointer transition-colors duration-300 hover:text-[#99cceb] text-lg">
                                    Order History
                                </Link>
                                <Link to="/customer/aboutUs">
                                    <div className="text-[#e8f0e5] font-medium cursor-pointer transition-colors duration-300 hover:text-[#99cceb] text-lg">
                                        About Us
                                    </div>
                                </Link>
                            </div>

                            {/* Shopping Cart */}
                            <Link to="/customer/cart" className="flex items-center">
                                <FaShoppingCart className="text-[#e8f0e5] w-[30px] h-[30px] cursor-pointer hover:text-[#99cceb]" />
                            </Link>

                            {/* Profile Section */}
                            <div className="flex items-center gap-5">
                                <div className="w-[45px] h-[45px] rounded-full border-2 border-white flex items-center justify-center overflow-hidden mr-2">
                                    <Link to="/customer/profile">
                                        <img
                                            src="https://static.vecteezy.com/system/resources/previews/036/885/313/non_2x/blue-profile-icon-free-png.png"
                                            alt="Profile"
                                            className="w-[100px] h-[40px] rounded-full transition duration-300 brightness-53 hover:brightness-50"
                                        />
                                    </Link>
                                </div>
                                <Link to="/customer/SignIn">
                                    <button
                                        onClick={handleLogout}
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