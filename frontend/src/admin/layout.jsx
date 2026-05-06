import NavBar from "./components/navBar.jsx";
import Footer from "./components/footer.jsx";

/**
 * Layout component containing the navigation bar and footer.
 */
function Layout({ children }) {
  return (
    <div className="layout">
      <NavBar />
      <main>{children}</main>
      <Footer />
    </div>
  );
}

export default Layout;