import NavBar from "./components/navBar.jsx";
import Footer from "./components/footer.jsx";

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