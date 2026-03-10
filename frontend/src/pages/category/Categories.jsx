import { useEffect, useState } from "react";
import MainLayout from "../../layouts/MainLayout";
import api from "../../api/axios";

export default function Categories() {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({ name: "", description: "" });
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    setLoading(true);
    setApiError("");

    try {
      const res = await api.get("/categories");
      setCategories(res.data || []);
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to load categories."
      );
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setForm({ name: "", description: "" });
    setEditingId(null);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleEdit = (category) => {
    setEditingId(category.id);
    setForm({
      name: category.name || "",
      description: category.description || "",
    });
    setSuccessMessage("");
    setApiError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setApiError("");
    setSuccessMessage("");

    try {
      const payload = {
        name: form.name.trim(),
        description: form.description.trim(),
      };

      if (editingId) {
        await api.put(`/categories/${editingId}`, payload);
        setSuccessMessage("Category updated successfully.");
      } else {
        await api.post("/categories", payload);
        setSuccessMessage("Category created successfully.");
      }

      resetForm();
      fetchCategories();
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to save category."
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm("Delete this category?");
    if (!confirmed) return;

    setApiError("");
    setSuccessMessage("");

    try {
      const res = await api.delete(`/categories/${id}`);
      setSuccessMessage(res.data?.message || "Category deleted successfully.");
      fetchCategories();
      if (editingId === id) {
        resetForm();
      }
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to delete category."
      );
    }
  };

  return (
    <MainLayout>
      <div className="container py-4">
        <div className="mb-4">
          <h2 className="page-title mb-1">Categories</h2>
          <p className="page-subtitle mb-0">Manage your expense and income categories</p>
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
                  {editingId ? "Edit Category" : "Add Category"}
                </h5>

                <form onSubmit={handleSubmit}>
                  <div className="mb-3">
                    <label className="form-label">Name</label>
                    <input
                      type="text"
                      name="name"
                      className="form-control"
                      value={form.name}
                      onChange={handleChange}
                      placeholder="Enter category name"
                      required
                    />
                  </div>

                  <div className="mb-3">
                    <label className="form-label">Description</label>
                    <textarea
                      name="description"
                      className="form-control"
                      rows="3"
                      value={form.description}
                      onChange={handleChange}
                      placeholder="Enter description"
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
                          ? "Update Category"
                          : "Add Category"}
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
                <h5 className="card-title mb-3">Category List</h5>

                {loading ? (
                  <div className="text-center py-4">
                    <div className="spinner-border" role="status" />
                  </div>
                ) : categories.length === 0 ? (
                  <p className="text-muted mb-0">No categories found.</p>
                ) : (
                  <div className="table-responsive">
                    <table className="table align-middle mb-0">
                      <thead>
                        <tr>
                          <th>Name</th>
                          <th>Description</th>
                          <th className="text-end">Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {categories.map((category) => (
                          <tr key={category.id}>
                            <td>{category.name}</td>
                            <td>{category.description || "-"}</td>
                            <td className="text-end">
                              <button
                                className="btn btn-sm btn-outline-primary me-2"
                                onClick={() => handleEdit(category)}
                              >
                                Edit
                              </button>
                              <button
                                className="btn btn-sm btn-outline-danger"
                                onClick={() => handleDelete(category.id)}
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