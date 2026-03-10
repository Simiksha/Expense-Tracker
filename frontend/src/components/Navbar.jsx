import { Link, NavLink, useNavigate } from "react-router-dom";
import { FaBell, FaSignOutAlt } from "react-icons/fa";
import { useEffect, useState } from "react";
import api from "../api/axios";

export default function Navbar() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "null");
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    fetchUnreadCount();
  }, []);

  const fetchUnreadCount = async () => {
    try {
      const res = await api.get("/notifications/unread-count");
      setUnreadCount(res.data?.unreadCount ?? 0);
    } catch {
      setUnreadCount(0);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    navigate("/login", { replace: true });
  };

  const linkClass = ({ isActive }) =>
    `nav-link custom-nav-link ${isActive ? "active fw-semibold" : ""}`;

  return (
    <nav className="navbar navbar-expand-lg navbar-dark app-navbar shadow-sm">
      <div className="container">
        <Link className="navbar-brand fw-bold brand-text" to="/dashboard">
          Smart Spend
        </Link>

        <button
          className="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#mainNavbar"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        <div className="collapse navbar-collapse" id="mainNavbar">
          <div className="navbar-nav me-auto">
            <NavLink className={linkClass} to="/dashboard">
              Dashboard
            </NavLink>
            <NavLink className={linkClass} to="/categories">
              Categories
            </NavLink>
            <NavLink className={linkClass} to="/transactions">
              Transactions
            </NavLink>
            <NavLink className={linkClass} to="/budgets">
              Budgets
            </NavLink>
            <NavLink className={linkClass} to="/reports">
              Reports
            </NavLink>
          </div>

          <div className="d-flex align-items-center gap-3 text-white mt-3 mt-lg-0">
            <NavLink
              to="/notifications"
              className="notification-link position-relative"
              title="Notifications"
            >
              <FaBell size={18} />
              {unreadCount > 0 && (
                <span className="notification-badge">
                  {unreadCount > 9 ? "9+" : unreadCount}
                </span>
              )}
            </NavLink>

            <span className="small user-greeting mb-0">
              {user?.name ? `Hi, ${user.name}` : "User"}
            </span>

            <button className="btn btn-light btn-sm logout-btn" onClick={handleLogout}>
              <FaSignOutAlt className="me-1" />
              Logout
            </button>
          </div>
        </div>
      </div>
    </nav>
  );
}