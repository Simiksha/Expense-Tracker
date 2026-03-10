import { useEffect, useState } from "react";
import MainLayout from "../../layouts/MainLayout";
import api from "../../api/axios";

function formatCurrency(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(amount);
}

function getStatusBadgeClass(status) {
  if (status === "EXCEEDED") return "text-bg-danger";
  if (status === "NEARING_LIMIT") return "text-bg-warning";
  return "text-bg-success";
}

export default function Budgets() {
  const today = new Date();

  const [budgets, setBudgets] = useState([]);
  const [categories, setCategories] = useState([]);

  const [form, setForm] = useState({
    categoryId: "",
    limitAmount: "",
    month: today.getMonth() + 1,
    year: today.getFullYear(),
  });

  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    fetchCategories();
    fetchBudgets();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await api.get("/categories");
      setCategories(res.data || []);
    } catch {
      setCategories([]);
    }
  };

  const fetchBudgets = async () => {
    setLoading(true);
    setApiError("");

    try {
      const res = await api.get("/budgets");
      setBudgets(res.data || []);
    } catch (error) {
      setApiError(error.response?.data?.message || "Failed to load budgets.");
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm({
      categoryId: "",
      limitAmount: "",
      month: today.getMonth() + 1,
      year: today.getFullYear(),
    });
    setEditingId(null);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleEdit = (budget) => {
    setEditingId(budget.id);
    setForm({
      categoryId: budget.categoryId,
      limitAmount: budget.limitAmount,
      month: budget.month,
      year: budget.year,
    });
    setApiError("");
    setSuccessMessage("");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setApiError("");
    setSuccessMessage("");

    try {
      const payload = {
        categoryId: Number(form.categoryId),
        limitAmount: Number(form.limitAmount),
        month: Number(form.month),
        year: Number(form.year),
      };

      if (editingId) {
        await api.put(`/budgets/${editingId}`, payload);
        setSuccessMessage("Budget updated successfully.");
      } else {
        await api.post("/budgets", payload);
        setSuccessMessage("Budget created successfully.");
      }

      resetForm();
      fetchBudgets();
    } catch (error) {
      setApiError(error.response?.data?.message || "Failed to save budget.");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm("Delete this budget?");
    if (!confirmed) return;

    setApiError("");
    setSuccessMessage("");

    try {
      const res = await api.delete(`/budgets/${id}`);
      setSuccessMessage(res.data?.message || "Budget deleted successfully.");
      fetchBudgets();
      if (editingId === id) {
        resetForm();
      }
    } catch (error) {
      setApiError(error.response?.data?.message || "Failed to delete budget.");
    }
  };

  return (
    <MainLayout>
      <div className="container py-4">
        <div className="mb-4">
          <h2 className="page-title mb-1">Budgets</h2>
          <p className="page-subtitle mb-0">Manage monthly category budgets</p>
        </div>

        {apiError && <div className="alert alert-danger">{apiError}</div>}
        {successMessage && (
          <div className="alert alert-success">{successMessage}</div>
        )}

        <div className="row g-4">
          <div className="col-lg-4">
            <div className="card shadow-sm border-0">
              <div className="card-body">
                <h5 className="card-title mb-3">
                  {editingId ? "Edit Budget" : "Add Budget"}
                </h5>

                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label className="form-label">Category</label>
                    <select
                      name="categoryId"
                      className="form-select"
                      value={form.categoryId}
                      onChange={handleChange}
                      required
                    >
                      <option value="">Select category</option>
                      {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Limit Amount</label>
                    <input
                      type="number"
                      step="0.01"
                      name="limitAmount"
                      className="form-control"
                      value={form.limitAmount}
                      onChange={handleChange}
                      required
                    />
                  </div>

                  <div className="row g-2 mb-3">
                    <div className="col-6">
                      <label className="form-label">Month</label>
                      <select
                        name="month"
                        className="form-select"
                        value={form.month}
                        onChange={handleChange}
                        required
                      >
                        {Array.from({ length: 12 }, (_, index) => (
                          <option key={index + 1} value={index + 1}>
                            {index + 1}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div className="col-6">
                      <label className="form-label">Year</label>
                      <input
                        type="number"
                        name="year"
                        className="form-control"
                        value={form.year}
                        onChange={handleChange}
                        required
                      />
                    </div>
                  </div>

                  <div className="d-flex gap-2">
                    <button
                      type="submit"
                      className="btn btn-primary"
                      disabled={saving}
                    >
                      {saving
                        ? "Saving..."
                        : editingId
                        ? "Update Budget"
                        : "Add Budget"}
                    </button>

                    {editingId && (
                      <button
                        type="button"
                        className="btn btn-outline-secondary"
                        onClick={resetForm}
                      >
                        Cancel
                      </button>
                    )}
                  </div>
                </form>
              </div>
            </div>
          </div>

          <div className="col-lg-8">
            <div className="card shadow-sm border-0">
              <div className="card-body">
                <h5 className="card-title mb-3">Budget List</h5>

                {loading ? (
                  <div className="text-center py-4">
                    <div className="spinner-border" role="status" />
                  </div>
                ) : budgets.length === 0 ? (
                  <p className="text-muted mb-0">No budgets found.</p>
                ) : (
                  <div className="table-responsive">
                    <table className="table align-middle mb-0">
                      <thead>
                        <tr>
                          <th>Category</th>
                          <th>Month/Year</th>
                          <th className="text-end">Limit</th>
                          <th className="text-end">Spent</th>
                          <th className="text-end">Remaining</th>
                          <th>Status</th>
                          <th className="text-end">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {budgets.map((budget) => (
                          <tr key={budget.id}>
                            <td>{budget.categoryName}</td>
                            <td>
                              {budget.month}/{budget.year}
                            </td>
                            <td className="text-end">
                              {formatCurrency(budget.limitAmount)}
                            </td>
                            <td className="text-end">
                              {formatCurrency(budget.spentAmount)}
                            </td>
                            <td className="text-end">
                              {formatCurrency(budget.remainingAmount)}
                            </td>
                            <td>
                              <div className="d-flex flex-column gap-1">
                                <span
                                  className={`badge ${getStatusBadgeClass(
                                    budget.status
                                  )}`}
                                >
                                  {budget.status}
                                </span>
                                <small className="text-muted">
                                  {budget.usagePercentage}%
                                </small>
                              </div>
                            </td>
                            <td className="text-end">
                              <button
                                className="btn btn-sm btn-outline-primary me-2"
                                onClick={() => handleEdit(budget)}
                              >
                                Edit
                              </button>
                              <button
                                className="btn btn-sm btn-outline-danger"
                                onClick={() => handleDelete(budget.id)}
                              >
                                Delete
                              </button>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}