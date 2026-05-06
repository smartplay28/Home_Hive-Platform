import Layout from '../../layout.jsx'
import React, { useState } from 'react';
import housepaintingImage from "../../../assets/Website_Images/Painting and Decor/housePainting.jpg";
import wallpaintingImage from "../../../assets/Website_Images/Painting and Decor/wallPainting.jpg";

function PaintingDecorServices() {
  const [selectedService, setSelectedService] = useState(null);
  //const [cart, setCart] = useState([]); // Track items in the cart
  const services = {
    fullHome: {
      id: 1,
      name: "Full Home Painting",
      basePrice: "₹15,000",
      priceNote: "Final price varies based on home size",
      image: housepaintingImage,
      shortDesc: "Complete home painting solution with premium quality paints and professional service.",
      details: {
        overview: "Transform your entire home with our comprehensive painting service. We use premium quality paints and professional techniques to ensure a perfect finish.",
        packages: [
          {
            name: "Essential Package",
            price: "₹15,000 onwards",
            features: [
              "2 coat painting",
              "Basic wall preparation",
              "Standard color options",
              "7 days completion"
            ]
          },
          {
            name: "Premium Package",
            price: "₹25,000 onwards",
            features: [
              "3 coat painting",
              "Advanced wall preparation",
              "Premium emulsion paints",
              "Designer color options",
              "5 days completion"
            ]
          },
          {
            name: "Luxury Package",
            price: "₹35,000 onwards",
            features: [
              "3 coat luxury painting",
              "Complete wall restoration",
              "Textured options available",
              "Custom color mixing",
              "Premium finish guarantee"
            ]
          }
        ],
        benefits: [
          "Professional color consultation",
          "Premium quality paints",
          "Experienced painters",
          "Furniture protection",
          "Complete cleanup after work"
        ],
        process: [
          "Initial consultation and color selection",
          "Surface preparation and cleaning",
          "Primer application",
          "Multiple coat application",
          "Final touches and cleanup"
        ],
        warranty: "3 years warranty on paint work"
      }
    },
    rooms: {
      id: 2,
      name: "Rooms/Walls Painting",
      basePrice: "₹4,000",
      priceNote: "Per room, varies based on size",
      image: wallpaintingImage,
      shortDesc: "Professional painting service for individual rooms or specific walls.",
      details: {
        overview: "Perfect solution for refreshing individual rooms or accent walls. Choose from various finishes and colors to match your vision.",
        packages: [
          {
            name: "Basic Room Package",
            price: "₹4,000 per room",
            features: [
              "2 coat painting",
              "Basic preparation",
              "Standard colors",
              "2-3 days completion"
            ]
          },
          {
            name: "Premium Room Package",
            price: "₹6,000 per room",
            features: [
              "3 coat painting",
              "Full wall preparation",
              "Premium colors",
              "Accent wall options",
              "1-2 days completion"
            ]
          }
        ],
        benefits: [
          "Room-specific color consultation",
          "Quality paint products",
          "Skilled painters",
          "Furniture protection",
          "Same-day cleanup"
        ],
        process: [
          "Room assessment and planning",
          "Furniture protection",
          "Wall preparation",
          "Painting execution",
          "Same day cleanup"
        ],
        warranty: "2 years warranty on paint work"
      }
    }
  };

  // SVG icons
  const Clock = () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
  );

  const Check = () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
    </svg>
  );

  const ChevronDown = () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
    </svg>
  );

  const ChevronUp = () => (
    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 15l7-7 7 7" />
    </svg>
  );

  return (
    <Layout>
      <div className="min-h-screen bg-gray-100">
        <div className="container mx-auto px-4 py-8">
          <h1 className="text-3xl font-bold text-center mb-8 text-blue-600">
            Painting & Decor Services
          </h1>
          {/* Services Cards */}
          <div className="space-y-6">
            {Object.values(services).map((service) => (
              <div
                key={service.id}
                className="bg-white rounded-lg shadow-md overflow-hidden"
              >
                {/* Main Card Content */}
                <div className="relative">
                  <img
                    src={service.image}
                    alt={service.name}
                    className="w-full h-64 object-cover"
                  />
                  <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black to-transparent p-4">
                    <h2 className="text-white text-2xl font-bold">{service.name}</h2>
                    <p className="text-white opacity-90">Starting from {service.basePrice}</p>
                  </div>
                </div>

                <div className="p-6">
                  <p className="text-gray-600 mb-4">{service.shortDesc}</p>
                  <div className="flex justify-between items-center">
                    <p className="text-sm text-gray-500">{service.priceNote}</p>
                    <button
                      onClick={() => setSelectedService(selectedService === service.id ? null : service.id)}
                      className="flex items-center text-blue-600 hover:text-blue-700"
                    >
                      {selectedService === service.id ? (
                        <>
                          Show Less <ChevronUp />
                        </>
                      ) : (
                        <>
                          View Details <ChevronDown />
                        </>
                      )}
                    </button>
                  </div>

                  {/* Expanded Details */}
                  {selectedService === service.id && (
                    <div className="mt-6 border-t pt-6">
                      {/* Overview */}
                      <div className="mb-6">
                        <h3 className="text-xl font-semibold mb-3">Overview</h3>
                        <p className="text-gray-600">{service.details.overview}</p>
                      </div>

                      {/* Packages */}
                      <div className="mb-6">
                        <h3 className="text-xl font-semibold mb-3">Available Packages</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                          {service.details.packages.map((pkg, index) => (
                            <div key={index} className="border rounded-lg p-4">
                              <h4 className="font-semibold text-lg mb-2">{pkg.name}</h4>
                              <p className="text-blue-600 font-bold mb-3">{pkg.price}</p>
                              <ul className="space-y-2">
                                {pkg.features.map((feature, idx) => (
                                  <li key={idx} className="flex items-center text-sm text-gray-600">
                                    <span className="text-green-500 mr-2"><Check /></span>
                                    {feature}
                                  </li>
                                ))}
                              </ul>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Benefits */}
                      <div className="mb-6">
                        <h3 className="text-xl font-semibold mb-3">Benefits</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                          {service.details.benefits.map((benefit, index) => (
                            <div key={index} className="flex items-center space-x-2 text-gray-600">
                              <span className="text-green-500"><Check /></span>
                              <span>{benefit}</span>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Process */}
                      <div className="mb-6">
                        <h3 className="text-xl font-semibold mb-3">Our Process</h3>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                          {service.details.process.map((step, index) => (
                            <div key={index} className="flex items-center space-x-2 text-gray-600">
                              <div className="bg-blue-100 rounded-full p-2">
                                <span className="text-blue-600 font-bold">{index + 1}</span>
                              </div>
                              <span>{step}</span>
                            </div>
                          ))}
                        </div>
                      </div>

                      {/* Warranty & Booking */}
                      <div className="flex flex-col md:flex-row justify-between items-center bg-gray-50 p-4 rounded-lg">
                        <div className="flex items-center mb-4 md:mb-0">
                          <span className="text-blue-600 mr-2"><Clock /></span>
                          <span className="text-gray-600">{service.details.warranty}</span>
                        </div>
                        <button className="bg-blue-600 text-white px-8 py-3 rounded-lg hover:bg-blue-700 transition-colors">
                          Book Consultation
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* Service Notes */}
          <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-100 p-4 rounded-lg">
              <p className="text-blue-800">
                🎨 Free color consultation available
              </p>
            </div>
            <div className="bg-blue-100 p-4 rounded-lg">
              <p className="text-blue-800">
                ✓ Premium quality paints used
              </p>
            </div>
            <div className="bg-blue-100 p-4 rounded-lg">
              <p className="text-blue-800">
                💧 Eco-friendly options available
              </p>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}
export default PaintingDecorServices;