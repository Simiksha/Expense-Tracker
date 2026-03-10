import { useEffect, useMemo, useState } from "react";
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

export default function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);

  const [form, setForm] = useState({
    title: "",
    amount: "",
    type: "EXPENSE",
    transactionDate: "",
    description: "",
    categoryId: "",
  });

  const [filters, setFilters] = useState({
    keyword: "",
    type: "",
    categoryId: "",
    startDate: "",
    endDate: "",
    minAmount: "",
    maxAmount: "",
    sortBy: "transactionDate",
    sortDir: "desc",
  });

  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      const res = await api.get("/categories");
      setCategories(res.data || []);
    } catch {
      setCategories([]);
    }
  };

  const queryString = useMemo(() => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== "" && value !== null && value !== undefined) {
        params.append(key, value);
      }
    });

    return params.toString();
  }, [filters]);

  useEffect(() => {
    fetchTransactions();
  }, [queryString]);

  const fetchTransactions = async () => {
    setLoading(true);
    setApiError("");

    try {
      const res = await api.get(
        `/transactions${queryString ? `?${queryString}` : ""}`
      );
      setTransactions(res.data || []);
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to load transactions."
      );
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm({
      title: "",
      amount: "",
      type: "EXPENSE",
      transactionDate: "",
      description: "",
      categoryId: "",
    });
    setEditingId(null);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const handleEdit = (transaction) => {
    setEditingId(transaction.id);
    setForm({
      title: transaction.title || "",
      amount: transaction.amount || "",
      type: transaction.type || "EXPENSE",
      transactionDate: transaction.transactionDate || "",
      description: transaction.description || "",
      categoryId: transaction.categoryId || "",
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
        title: form.title.trim(),
        amount: Number(form.amount),
        type: form.type,
        transactionDate: form.transactionDate,
        description: form.description.trim(),
        categoryId: Number(form.categoryId),
      };

      if (editingId) {
        await api.put(`/transactions/${editingId}`, payload);
        setSuccessMessage("Transaction updated successfully.");
      } else {
        await api.post("/transactions", payload);
        setSuccessMessage("Transaction added successfully.");
      }

      resetForm();
      fetchTransactions();
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to save transaction."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm("Delete this transaction?");
    if (!confirmed) return;

    setApiError("");
    setSuccessMessage("");

    try {
      const res = await api.delete(`/transactions/${id}`);
      setSuccessMessage(
        res.data?.message || "Transaction deleted successfully."
      );
      fetchTransactions();
      if (editingId === id) {
        resetForm();
      }
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to delete transaction."
      );
    }
  };

  const clearFilters = () => {
    setFilters({
      keyword: "",
      type: "",
      categoryId: "",
      startDate: "",
      endDate: "",
      minAmount: "",
      maxAmount: "",
      sortBy: "transactionDate",
      sortDir: "desc",
    });
  };

  return (
    <MainLayout>
      <div className="container py-4">
        <div className="mb-4">
          <h2 className="page-title mb-1">Transactions</h2>
          <p className="page-subtitle mb-0">Track your income and expenses</p>
        </div>

        {apiError && <div className="alert alert-danger">{apiError}</div>}
        {successMessage && (
          <div className="alert alert-success">{successMessage}</div>
        )}

        <div className="card shadow-sm border-0 rounded-4 mb-4">
          <div className="card-body p-4">
            <h5 className="card-title mb-3">Filters</h5>

            <div className="row g-3">
              <div className="col-md-3">
                <label className="form-label">Keyword</label>
                <input
                  type="text"
                  name="keyword"
                  className="form-control"
                  value={filters.keyword}
                  onChange={handleFilterChange}
                  placeholder="Search"
                />
              </div>

              <div className="col-md-2">
                <label className="form-label">Type</label>
                <select
                  name="type"
                  className="form-select"
                  value={filters.type}
                  onChange={handleFilterChange}
                >
                  <option value="">All</option>
                  <option value="EXPENSE">Expense</option>
                  <option value="INCOME">Income</option>
                </select>
              </div>

              <div className="col-md-2">
                <label className="form-label">Category</label>
                <select
                  name="categoryId"
                  className="form-select"
                  value={filters.categoryId}
                  onChange={handleFilterChange}
                >
                  <option value="">All</option>
                  {categories.map((category) => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="col-md-2">
                <label className="form-label">Start Date</label>
                <input
                  type="date"
                  name="startDate"
                  className="form-control"
                  value={filters.startDate}
                  onChange={handleFilterChange}
                />
              </div>

              <div className="col-md-2">
                <label className="form-label">End Date</label>
                <input
                  type="date"
                  name="endDate"
                  className="form-control"
                  value={filters.endDate}
                  onChange={handleFilterChange}
                />
              </div>

              <div className="col-md-1 d-flex align-items-end">
                <button
                  type="button"
                  className="btn btn-outline-secondary w-100"
                  onClick={clearFilters}
                >
                  Clear
                </button>
              </div>

              <div className="col-md-2">
                <label className="form-label">Min Amount</label>
                <input
                  type="number"
                  step="0.01"
                  name="minAmount"
                  className="form-control"
                  value={filters.minAmount}
                  onChange={handleFilterChange}
                />
              </div>

              <div className="col-md-2">
                <label className="form-label">Max Amount</label>
                <input
                  type="number"
                  step="0.01"
                  name="maxAmount"
                  className="form-control"
                  value={filters.maxAmount}
                  onChange={handleFilterChange}
                />
              </div>

              <div className="col-md-2">
                <label className="form-label">Sort By</label>
                <select
                  name="sortBy"
                  className="form-select"
                  value={filters.sortBy}
                  onChange={handleFilterChange}
                >
                  <option value="transactionDate">Date</option>
                  <option value="amount">Amount</option>
                  <option value="title">Title</option>
                  <option value="createdAt">Created At</option>
                </select>
              </div>

              <div className="col-md-2">
                <label className="form-label">Direction</label>
                <select
                  name="sortDir"
                  className="form-select"
                  value={filters.sortDir}
                  onChange={handleFilterChange}
                >
                  <option value="desc">Descending</option>
                  <option value="asc">Ascending</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <div className="row g-4">
          <div className="col-lg-4">
            <div className="card shadow-sm border-0 rounded-4">
              <div className="card-body p-4">
                <h5 className="card-title mb-3">
                  {editingId ? "Edit Transaction" : "Add Transaction"}
                </h5>

                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label className="form-label">Title</label>
                    <input
                      type="text"
                      name="title"
                      className="form-control"
                      value={form.title}
                      onChange={handleFormChange}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Amount</label>
                    <input
                      type="number"
                      step="0.01"
                      name="amount"
                      className="form-control"
                      value={form.amount}
                      onChange={handleFormChange}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Type</label>
                    <select
                      name="type"
                      className="form-select"
                      value={form.type}
                      onChange={handleFormChange}
                      required
                    >
                      <option value="EXPENSE">Expense</option>
                      <option value="INCOME">Income</option>
                    </select>
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Date</label>
                    <input
                      type="date"
                      name="transactionDate"
                      className="form-control"
                      value={form.transactionDate}
                      onChange={handleFormChange}
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Category</label>
                    <select
                      name="categoryId"
                      className="form-select"
                      value={form.categoryId}
                      onChange={handleFormChange}
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
                    <label className="form-label">Description</label>
                    <textarea
                      name="description"
                      className="form-control"
                      rows="3"
                      value={form.description}
                      onChange={handleFormChange}
                    />
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
                        ? "Update Transaction"
                        : "Add Transaction"}
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
            <div className="card shadow-sm border-0 rounded-4">
              <div className="card-body p-4">
                <h5 className="card-title mb-3">Transaction List</h5>

                {loading ? (
                  <div className="text-center py-4">
                    <div className="spinner-border" role="status" />
                  </div>
                ) : transactions.length === 0 ? (
                  <p className="text-muted mb-0">No transactions found.</p>
                ) : (
                  <div className="table-responsive">
                    <table className="table align-middle mb-0">
                      <thead>
                        <tr>
                          <th>Title</th>
                          <th>Type</th>
                          <th>Category</th>
                          <th>Date</th>
                          <th className="text-end">Amount</th>
                          <th className="text-end">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {transactions.map((transaction) => (
                          <tr key={transaction.id}>
                            <td>
                              <div>{transaction.title}</div>
                              <small className="text-muted">
                                {transaction.description || "-"}
                              </small>
                            </td>
                            <td>
                              <span
                                className={`badge ${
                                  transaction.type === "INCOME"
                                    ? "text-bg-success"
                                    : "text-bg-danger"
                                }`}
                              >
                                {transaction.type}
                              </span>
                            </td>
                            <td>{transaction.categoryName}</td>
                            <td>{transaction.transactionDate}</td>
                            <td className="text-end">
                              {formatCurrency(transaction.amount)}
                            </td>
                            <td className="text-end">
                              <button
                                className="btn btn-sm btn-outline-primary me-2"
                                onClick={() => handleEdit(transaction)}
                              >
                                Edit
                              </button>
                              <button
                                className="btn btn-sm btn-outline-danger"
                                onClick={() => handleDelete(transaction.id)}
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