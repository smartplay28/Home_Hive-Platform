import Layout from '../../layout.jsx';
import storyImage from '../../../assets/Website_Images/AboutUs/HappyCustomers.png';
import successImage from '../../../assets/Website_Images/AboutUs/OurStory2.jpg';

const AgentAboutUs = () => {
    return (
        <Layout>
            <div className="bg-gradient-to-b from-white to-blue-50 min-h-screen py-16">
                <div className="container mx-auto px-8 lg:px-16">
                    {/* Hero Section */}
                    <section className="text-center mb-12">
                        <h1 className="text-5xl font-extrabold text-[#1c4e80] mb-4">
                            About <span className="text-[#004aad]">HomeHive</span>
                        </h1>
                        <p className="text-lg text-gray-700 max-w-2xl mx-auto">
                            Your one-stop solution for reliable home services. From expert plumbing to professional painting, we make your home a better place—hassle-free and with unmatched quality.
                        </p>
                    </section>

                    {/* Section Divider */}
                    <hr className="border-t-2 border-blue-200 mb-12" />

                    {/* Story Section */}
                    <section className="flex flex-col lg:flex-row items-center mb-16">
                        <div className="lg:w-1/2 mb-8 lg:mb-0">
                            <img
                                src={successImage}
                                alt="Our story"
                                className="rounded-lg shadow-lg w-full"
                            />
                        </div>
                        <div className="lg:w-1/2 lg:pl-12">
                            <h2 className="text-3xl font-bold text-[#1c4e80] mb-4">
                                Our Story
                            </h2>
                            <p className="text-gray-700 leading-relaxed">
                                HomeHive was inspired by a simple idea: making expert home services accessible, reliable, and stress-free.
                                From fixing leaky faucets to transforming spaces with stunning decor, our journey began with a mission to take the hassle out of finding trustworthy service providers.
                            </p>
                        </div>
                    </section>

                    {/* Testimonials Section */}
                    <section className="bg-blue-50 py-12 rounded-lg shadow-md mb-16">
                        <h2 className="text-3xl font-bold text-center text-[#1c4e80] mb-6">
                            What Our Customers Say
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                            <div className="bg-white p-6 rounded-lg shadow-lg">
                                <p className="text-gray-700 italic">
                                    "HomeHive transformed my living room into a masterpiece. Their painting service was professional and timely!"
                                </p>
                                <p className="text-sm text-gray-500 mt-4">- Sarah J., Mumbai</p>
                            </div>
                            <div className="bg-white p-6 rounded-lg shadow-lg">
                                <p className="text-gray-700 italic">
                                    "Finding a reliable plumber has never been easier. Thank you, HomeHive!"
                                </p>
                                <p className="text-sm text-gray-500 mt-4">- Aman K., Delhi</p>
                            </div>
                            <div className="bg-white p-6 rounded-lg shadow-lg">
                                <p className="text-gray-700 italic">
                                    "From booking to execution, the service was seamless. Highly recommend their salon services!"
                                </p>
                                <p className="text-sm text-gray-500 mt-4">- Priya S., Bangalore</p>
                            </div>
                        </div>
                    </section>

                    {/* Trust Section */}
                    <section className="text-center mb-16">
                        <h2 className="text-3xl font-bold text-[#1c4e80] mb-6">
                            Why Choose Us?
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                            <div className="flex flex-col items-center">
                                <div className="text-5xl font-extrabold text-[#004aad]">10K+</div>
                                <p className="text-gray-700 mt-2">Happy Customers</p>
                            </div>
                            <div className="flex flex-col items-center">
                                <div className="text-5xl font-extrabold text-[#004aad]">99%</div>
                                <p className="text-gray-700 mt-2">Positive Feedback</p>
                            </div>
                            <div className="flex flex-col items-center">
                                <div className="text-5xl font-extrabold text-[#004aad]">24/7</div>
                                <p className="text-gray-700 mt-2">Customer Support</p>
                            </div>
                        </div>
                    </section>

                    {/* Mission and Vision Section */}
                    <section className="flex flex-col lg:flex-row items-center">
                        <div className="lg:w-1/2 lg:pr-12">
                            <h2 className="text-3xl font-bold text-[#1c4e80] mb-4">
                                Our Mission & Vision
                            </h2>
                            <p className="text-gray-700 leading-relaxed">
                                At HomeHive, our mission is simple: to provide unparalleled home services that enhance your lifestyle.
                                With a vision to become the go-to platform for all home needs, we’re committed to delivering solutions that are efficient, reliable, and tailored to your requirements.
                            </p>
                        </div>
                        <div className="lg:w-1/2 mt-8 lg:mt-0">
                            <img
                                src={storyImage}
                                alt="Mission and Vision"
                                className="rounded-lg shadow-lg w-full"
                            />
                        </div>
                    </section>

                    {/* Call to Action Section */}
                    <section className="mt-16 text-center">
                        <h2 className="text-3xl font-bold text-[#1c4e80] mb-4">
                            Join the HomeHive Journey
                        </h2>
                        <p className="text-gray-700 max-w-2xl mx-auto mb-6">
                            Ready to transform your home? Explore our wide range of services and experience the HomeHive difference today.
                            Let’s create the home of your dreams, one service at a time.
                        </p>
                    </section>
                </div>
            </div>
        </Layout>
    );
};
export default AgentAboutUs;