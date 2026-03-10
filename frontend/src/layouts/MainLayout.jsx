import Navbar from "../components/Navbar";

export default function MainLayout({ children }) {
  return (
    <div className="bg-light min-vh-100">
      <Navbar />
      <main>{children}</main>
    </div>
  );
}