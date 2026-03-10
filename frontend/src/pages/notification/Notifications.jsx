import { useEffect, useState } from "react";
import MainLayout from "../../layouts/MainLayout";
import api from "../../api/axios";

function formatDateTime(value) {
  if (!value) return "-";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString("en-IN");
}

function getTypeBadgeClass(type) {
  if (type === "BUDGET_ALERT") return "text-bg-warning";
  if (type === "SYSTEM") return "text-bg-primary";
  return "text-bg-secondary";
}

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();
  }, []);

  const fetchNotifications = async () => {
    setLoading(true);
    setApiError("");

    try {
      const res = await api.get("/notifications");
      setNotifications(res.data || []);
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to load notifications."
      );
    } finally {
      setLoading(false);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const res = await api.get("/notifications/unread-count");
      setUnreadCount(res.data?.unreadCount ?? 0);
    } catch {
      setUnreadCount(0);
    }
  };

  const handleMarkAsRead = async (id) => {
    setApiError("");
    setSuccessMessage("");

    try {
      await api.put(`/notifications/${id}/read`);
      setSuccessMessage("Notification marked as read.");
      fetchNotifications();
      fetchUnreadCount();
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to mark notification as read."
      );
    }
  };

  const handleMarkAllAsRead = async () => {
    setApiError("");
    setSuccessMessage("");

    try {
      const res = await api.put("/notifications/read-all");
      setSuccessMessage(
        res.data?.message || "All notifications marked as read."
      );
      fetchNotifications();
      fetchUnreadCount();
    } catch (error) {
      setApiError(
        error.response?.data?.message ||
          "Failed to mark all notifications as read."
      );
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm("Delete this notification?");
    if (!confirmed) return;

    setApiError("");
    setSuccessMessage("");

    try {
      const res = await api.delete(`/notifications/${id}`);
      setSuccessMessage(
        res.data?.message || "Notification deleted successfully."
      );
      fetchNotifications();
      fetchUnreadCount();
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to delete notification."
      );
    }
  };

  return (
    <MainLayout>
      <div className="container py-4">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h2 className="page-title mb-1">Notifications</h2>
            <p className="page-subtitle mb-0">
              You have {unreadCount} unread notification{unreadCount === 1 ? "" : "s"}
            </p>
          </div>

          <button
            className="btn btn-outline-primary"
            onClick={handleMarkAllAsRead}
            disabled={notifications.length === 0}
          >
            Mark All as Read
          </button>
        </div>

        {apiError && <div className="alert alert-danger">{apiError}</div>}
        {successMessage && (
          <div className="alert alert-success">{successMessage}</div>
        )}

        <div className="card shadow-sm border-0">
          <div className="card-body">
            {loading ? (
              <div className="text-center py-4">
                <div className="spinner-border" role="status" />
              </div>
            ) : notifications.length === 0 ? (
              <p className="text-muted mb-0">No notifications found.</p>
            ) : (
              <div className="list-group list-group-flush">
                {notifications.map((notification) => (
                  <div
                    key={notification.id}
                    className={`list-group-item px-0 py-3 ${
                      !notification.read ? "bg-light" : ""
                    }`}
                  >
                    <div className="d-flex justify-content-between align-items-start gap-3">
                      <div className="flex-grow-1">
                        <div className="d-flex align-items-center gap-2 mb-2 flex-wrap">
                          <h6 className="mb-0">{notification.title}</h6>
                          <span
                            className={`badge ${getTypeBadgeClass(
                              notification.type
                            )}`}
                          >
                            {notification.type}
                          </span>
                          {!notification.read && (
                            <span className="badge text-bg-dark">Unread</span>
                          )}
                        </div>

                        <p className="mb-1">{notification.message}</p>
                        <small className="text-muted">
                          {formatDateTime(notification.createdAt)}
                        </small>
                      </div>

                      <div className="d-flex gap-2">
                        {!notification.read && (
                          <button
                            className="btn btn-sm btn-outline-primary"
                            onClick={() => handleMarkAsRead(notification.id)}
                          >
                            Mark Read
                          </button>
                        )}

                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleDelete(notification.id)}
                        >
                          Delete
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </MainLayout>
  );
}