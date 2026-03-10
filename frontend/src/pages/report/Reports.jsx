import { useEffect, useState } from "react";
import MainLayout from "../../layouts/MainLayout";
import api from "../../api/axios";

export default function Reports() {
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({
    type: "",
    categoryId: "",
    startDate: "",
    endDate: "",
  });

  const [loadingCategories, setLoadingCategories] = useState(true);
  const [downloading, setDownloading] = useState("");
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    setLoadingCategories(true);
    try {
      const res = await api.get("/categories");
      setCategories(res.data || []);
    } catch {
      setCategories([]);
    } finally {
      setLoadingCategories(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const buildQueryString = () => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
      if (value !== "" && value !== null && value !== undefined) {
        params.append(key, value);
      }
    });

    return params.toString();
  };

  const downloadFile = async (type) => {
    setApiError("");
    setSuccessMessage("");
    setDownloading(type);

    try {
      const queryString = buildQueryString();
      const endpoint =
        type === "pdf"
          ? `/reports/pdf${queryString ? `?${queryString}` : ""}`
          : `/reports/excel${queryString ? `?${queryString}` : ""}`;

      const response = await api.get(endpoint, {
        responseType: "blob",
      });

      const contentType = response.headers["content-type"];
      const extension = type === "pdf" ? "pdf" : "xlsx";
      const blob = new Blob([response.data], { type: contentType });
      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");
      link.href = url;
      link.download = `transactions-report.${extension}`;
      document.body.appendChild(link);
      link.click();
      link.remove();

      window.URL.revokeObjectURL(url);

      setSuccessMessage(
        type === "pdf"
          ? "PDF report downloaded successfully."
          : "Excel report downloaded successfully."
      );
    } catch (error) {
      setApiError(
        error.response?.data?.message || `Failed to download ${type.toUpperCase()} report.`
      );
    } finally {
      setDownloading("");
    }
  };

  const clearFilters = () => {
    setFilters({
      type: "",
      categoryId: "",
      startDate: "",
      endDate: "",
    });
  };

  return (
    <MainLayout>
      <div className="container py-4">
        <div className="mb-4">
          <h2 className="page-title mb-1">Reports</h2>
          <p className="page-subtitle mb-0">Download transaction reports in PDF or Excel format</p>
        </div>

        {apiError && <div className="alert alert-danger">{apiError}</div>}
        {successMessage && (
          <div className="alert alert-success">{successMessage}</div>
        )}

        <div className="row justify-content-center">
          <div className="col-lg-8">
            <div className="card shadow-sm border-0">
              <div className="card-body p-4">
                <h5 className="card-title mb-4">Report Filters</h5>

                <div className="row g-3">
                  <div className="col-md-6">
                    <label className="form-label">Type</label>
                    <select
                      name="type"
                      className="form-select"
                      value={filters.type}
                      onChange={handleChange}
                    >
                      <option value="">All</option>
                      <option value="INCOME">Income</option>
                      <option value="EXPENSE">Expense</option>
                    </select>
                  </div>

                  <div className="col-md-6">
                    <label className="form-label">Category</label>
                    <select
                      name="categoryId"
                      className="form-select"
                      value={filters.categoryId}
                      onChange={handleChange}
                      disabled={loadingCategories}
                    >
                      <option value="">All</option>
                      {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="col-md-6">
                    <label className="form-label">Start Date</label>
                    <input
                      type="date"
                      name="startDate"
                      className="form-control"
                      value={filters.startDate}
                      onChange={handleChange}
                    />
                  </div>

                  <div className="col-md-6">
                    <label className="form-label">End Date</label>
                    <input
                      type="date"
                      name="endDate"
                      className="form-control"
                      value={filters.endDate}
                      onChange={handleChange}
                    />
                  </div>
                </div>

                <div className="d-flex flex-wrap gap-2 mt-4">
                  <button
                    className="btn btn-danger"
                    onClick={() => downloadFile("pdf")}
                    disabled={downloading === "pdf"}
                  >
                    {downloading === "pdf" ? "Downloading PDF..." : "Download PDF"}
                  </button>

                  <button
                    className="btn btn-success"
                    onClick={() => downloadFile("excel")}
                    disabled={downloading === "excel"}
                  >
                    {downloading === "excel"
                      ? "Downloading Excel..."
                      : "Download Excel"}
                  </button>

                  <button
                    className="btn btn-outline-secondary"
                    onClick={clearFilters}
                  >
                    Clear Filters
                  </button>
                </div>

                <hr className="my-4" />

                <div className="text-muted small">
                  You can export all transactions or filter by type, category, and date range before downloading.
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}