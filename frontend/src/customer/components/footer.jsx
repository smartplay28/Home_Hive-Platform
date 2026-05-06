import logo from '../../assets/logo.jpg';

/**
 * Footer component for the customer page.
 */
function Footer() {
    return (
        <footer className="bg-[#1c4e80] p-1 __className_8cc72f">
        <div className="w-full max-w-screen-xl mx-auto p-0 md:py-3">
          <div className="sm:flex sm:items-center sm:justify-between">
            <a className="flex items-center mb-4 sm:mb-0 space-x-1 rtl:space-x-reverse ml-5" href="/customer/home">
              <span className="__className_37c115 self-center text-2xl font-semibold whitespace-nowrap text-[#e8f0e5]">Urban</span>
              <img src={logo} className="h-10" alt="HomeHive Logo" style={{borderRadius: "50%", background: "#f6a32f", display: "block"}}/>
              <span className="__className_37c115 self-center text-2xl font-semibold whitespace-nowrap text-[#e8f0e5]">Crap</span>
            </a>
              <ul className="flex flex-wrap items-center mb-6 text-sm font-medium text-[#e8f0e5] sm:mb-0">
                  <li>
                    <a href="/customer/aboutUs" className="text-base hover:underline me-4 md:me-9">About Us</a>
                  </li>
              </ul>
          </div>
          <hr className="my-6 border-[#e8f0e5] sm:mx-auto lg:my-8"/>
          <span className="block text-sm text-[#e8f0e5] sm:text-center">
            © 2024 <a className="hover:underline me-4 md:me-6" href="/customer/home">HomeHive™</a> All Rights Reserved
          </span>
        </div>
      </footer>
    );
  }
  
  export default Footer;