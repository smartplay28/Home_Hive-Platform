import React, { useRef, useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Layout from '../../layout.jsx';
import aclogo from '../../../assets/Website_Images/Home/ac.jpeg';
import gardeninglogo from '../../../assets/Website_Images/Home/gardening.jpeg';
import paintinglogo from '../../../assets/Website_Images/Home/painting.png';
import plumbinglogo from '../../../assets/Website_Images/Home/plumbing.png';
import pest from '../../../assets/Website_Images/Home/pest.png';
import salon from '../../../assets/Website_Images/Home/salon.png';
import profServices from '../../../assets/Website_Images/Home/profImage.jpg';

const CustomerHome = () => {
  const scrollContainerRef = useRef(null);
  const navigate = useNavigate();
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  const services = [
    { img: aclogo, title: 'AC & Appliance', link: '/customer/services/ACRepair' },
    { img: salon, title: 'Salon', link: '/customer/services/salon' },
    { img: paintinglogo, title: 'Painting & Decor', link: '/customer/services/painting' },
    { img: pest, title: 'Cleaning & Pest Control', link: '/customer/services/cleaningpest' },
    { img: plumbinglogo, title: 'Electrician, Plumber & Carpenter', link: '/customer/services/epc' },
    { img: gardeninglogo, title: 'Gardening & Landscaping', link: '/customer/services/gardening' },
  ];

  useEffect(() => {
    const customerToken = localStorage.getItem('customerId');
    if (!customerToken) {
      navigate('/customer/SignIn');
    } else {
      setIsAuthenticated(true);
    }
  }, [navigate]);

  if (!isAuthenticated) {
    return null;
  }

  const slideScroll = (direction) => {
    if (scrollContainerRef.current) {
      scrollContainerRef.current.scrollBy({
        left: direction === 'left' ? -300 : 300,
        behavior: 'smooth',
      });
    }
  };

  return (
    <Layout>
      {/* Hero Section */}
      <section className="bg-gradient-to-r from-[#003366] to-[#66b3e0] text-white p-8 md:flex md:justify-between md:items-center">
        <div className="max-w-md">
          <h1 className="text-4xl font-bold mb-4 text-center">
            Professional Home Services <br /> Just a Click Away!
          </h1>
          <p className="mb-6 text-lg text-center">
            Find trusted experts for all your home needs. From repairs to wellness, we've got you covered.
          </p>
        </div>
        <div className="hidden md:block w-1/2">
          <img
            src={profServices}
            alt="Home Services"
            className="rounded-lg shadow-lg"
            style={{ width: '625px', height: '550px' }}
          />
        </div>
      </section>

      {/* Services Section */}
      <section className="py-12 bg-white">
        <div className="max-w-7xl mx-auto">
          <h2 className="text-3xl font-bold text-center text-[#003366] mb-8">Our Popular Services</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-6">
            {services.map((service, index) => (
              <Link
                to={service.link}
                key={index}
                className="relative p-4 bg-white rounded-lg shadow hover:shadow-xl hover:scale-105 transition transform flex flex-col items-center group"
              >
                {/* Applied Blueish Tint for the Card */}
                <div className="absolute inset-0 bg-gradient-to-b from-[#eaf0f7] to-[#d0e3f7] opacity-70 rounded-lg group-hover:opacity-40 transition"></div>

                <img
                  src={service.img}
                  alt={service.title}
                  className="relative w-24 h-24 object-cover rounded-full mb-4 z-10 border-4 border-[#003366] transform transition-all duration-300 group-hover:scale-110"
                />
                <h3 className="relative text-lg font-semibold text-[#003366] z-10">
                  {service.title}
                </h3>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Scrollable Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto relative">
          <h2 className="text-4xl font-bold text-center text-[#003366] mb-12">Discover More</h2>
          <div className="relative">
            {/* Scroll Container */}
            <div
              ref={scrollContainerRef}
              className="flex overflow-x-hidden space-x-6 pb-4"
            >
              {services.map((service, index) => (
                <div
                  key={index}
                  className="min-w-[250px] bg-gradient-to-b from-[#eaf0f7] to-[#d0e3f7] p-6 rounded-lg shadow-lg hover:shadow-2xl hover:scale-105 transition transform flex flex-col items-center justify-between"
                >
                  <img
                    src={service.img}
                    alt={service.title}
                    className="w-24 h-24 object-cover rounded-full mb-4 border-4 border-[#003366] transform transition-all duration-300 group-hover:scale-110"
                  />
                  <h3 className="text-lg font-semibold text-[#003366] mb-4">{service.title}</h3>
                  <p className="text-sm text-center text-[#6c757d] mb-4">{service.description}</p>
                  <Link
                    to={service.link}
                    className="mt-auto px-6 py-2 text-white bg-[#003366] rounded-full text-sm font-semibold hover:bg-[#4e66a4] transition duration-300"
                  >
                    Learn More
                  </Link>
                </div>
              ))}
            </div>

            {/* Left Scroll Button */}
            <button
              onClick={() => scrollContainerRef.current.scrollBy({ left: -500, behavior: "smooth" })}
              className="absolute top-1/2 left-0 transform -translate-y-1/2 bg-[#003366] text-white p-2 rounded-full shadow-lg hover:bg-[#4e66a4] transition duration-300"
            >
              &lt;
            </button>

            {/* Right Scroll Button */}
            <button
              onClick={() => scrollContainerRef.current.scrollBy({ left: 500, behavior: "smooth" })}
              className="absolute top-1/2 right-0 transform -translate-y-1/2 bg-[#003366] text-white p-2 rounded-full shadow-lg hover:bg-[#4e66a4] transition duration-300"
            >
              &gt;
            </button>
          </div>
        </div>
      </section>
    </Layout>
  );
};

export default CustomerHome;
