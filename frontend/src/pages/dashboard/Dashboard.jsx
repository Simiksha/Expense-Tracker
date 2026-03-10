import { useEffect, useState } from "react";
import { FaSyncAlt } from "react-icons/fa";
import MainLayout from "../../layouts/MainLayout";
import api from "../../api/axios";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";

function formatCurrency(value) {
  const amount = Number(value || 0);
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(amount);
}

function formatMonthLabel(year, month) {
  const date = new Date(year, month - 1, 1);
  return date.toLocaleString("en-US", { month: "short", year: "numeric" });
}

export default function Dashboard() {
  const [summary, setSummary] = useState({
    totalIncome: 0,
    totalExpense: 0,
    balance: 0,
  });

  const [monthlyTrend, setMonthlyTrend] = useState([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState([]);

  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState("");

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    setLoading(true);
    setApiError("");

    try {
      const [summaryRes, trendRes, categoryRes] = await Promise.all([
        api.get("/dashboard/summary"),
        api.get("/dashboard/monthly-trend"),
        api.get("/dashboard/category-breakdown"),
      ]);

      setSummary({
        totalIncome: summaryRes.data.totalIncome ?? 0,
        totalExpense: summaryRes.data.totalExpense ?? 0,
        balance: summaryRes.data.balance ?? 0,
      });

      const trendData = (trendRes.data || []).map((item) => ({
        ...item,
        label: formatMonthLabel(item.year, item.month),
        income: Number(item.income || 0),
        expense: Number(item.expense || 0),
      }));

      const categoryData = (categoryRes.data || []).map((item) => ({
        ...item,
        totalAmount: Number(item.totalAmount || 0),
      }));

      setMonthlyTrend(trendData);
      setCategoryBreakdown(categoryData);
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to load dashboard data."
      );
    } finally {
      setLoading(false);
    }
  };

  const pieColors = [
    "#0d6efd",
    "#198754",
    "#ffc107",
    "#dc3545",
    "#6f42c1",
    "#20c997",
    "#fd7e14",
    "#6c757d",
  ];

  return (
    <MainLayout>
      <div className="container py-4">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <div>
            <h2 className="mb-1">Dashboard</h2>
            <p className="text-muted mb-0">Overview of your finances</p>
          </div>
          <button
            className="btn btn-outline-primary refresh-icon-btn"
            onClick={fetchDashboardData}
            title="Refresh"
          >
            <FaSyncAlt />
          </button>
        </div>

        {apiError && <div className="alert alert-danger">{apiError}</div>}

        {loading ? (
          <div className="text-center py-5">
            <div className="spinner-border" role="status" />
          </div>
        ) : (
          <>
            <div className="row g-4 mb-4">
              <div className="col-md-4">
                <div className="card shadow-sm border-0 h-100 dashboard-stat-card">
                  <div className="card-body p-4">
                    <p className="label">Total Income</p>
                    <h3 className="value">{formatCurrency(summary.totalIncome)}</h3>
                  </div>
                </div>
              </div>

              <div className="col-md-4">
                <div className="card shadow-sm border-0 h-100 dashboard-stat-card">
                  <div className="card-body p-4">
                    <p className="label">Total Expense</p>
                    <h3 className="value">{formatCurrency(summary.totalExpense)}</h3>
                  </div>
                </div>
              </div>

              <div className="col-md-4">
                <div className="card shadow-sm border-0 h-100 dashboard-stat-card">
                  <div className="card-body p-4">
                    <p className="label">Balance</p>
                    <h3 className="value">{formatCurrency(summary.balance)}</h3>
                  </div>
                </div>
              </div>
            </div>

            <div className="row g-4">
              <div className="col-lg-7">
                <div className="card shadow-sm border-0 h-100 rounded-4">
                  <div className="card-body">
                    <h5 className="card-title mb-3">Monthly Income vs Expense</h5>

                    {monthlyTrend.length === 0 ? (
                      <p className="text-muted mb-0">No monthly trend data available.</p>
                    ) : (
                      <div style={{ width: "100%", height: 350 }}>
                        <ResponsiveContainer>
                          <BarChart data={monthlyTrend}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="label" />
                            <YAxis />
                            <Tooltip
                              formatter={(value) => formatCurrency(value)}
                            />
                            <Legend />
                            <Bar dataKey="income" name="Income" fill="#198754" />
                            <Bar dataKey="expense" name="Expense" fill="#dc3545" />
                          </BarChart>
                        </ResponsiveContainer>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              <div className="col-lg-5">
                <div className="card shadow-sm border-0 h-100 rounded-4">
                  <div className="card-body">
                    <h5 className="card-title mb-3">Expense by Category</h5>

                    {categoryBreakdown.length === 0 ? (
                      <p className="text-muted mb-0">
                        No category breakdown data available.
                      </p>
                    ) : (
                      <div style={{ width: "100%", height: 350 }}>
                        <ResponsiveContainer>
                          <PieChart>
                            <Pie
                              data={categoryBreakdown}
                              dataKey="totalAmount"
                              nameKey="categoryName"
                              cx="50%"
                              cy="50%"
                              outerRadius={110}
                              label={({ categoryName, percent }) =>
                                `${categoryName} ${(percent * 100).toFixed(0)}%`
                              }
                            >
                              {categoryBreakdown.map((entry, index) => (
                                <Cell
                                  key={`cell-${entry.categoryId}`}
                                  fill={pieColors[index % pieColors.length]}
                                />
                              ))}
                            </Pie>
                            <Tooltip
                              formatter={(value) => formatCurrency(value)}
                            />
                            <Legend />
                          </PieChart>
                        </ResponsiveContainer>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            </div>

            {categoryBreakdown.length > 0 && (
              <div className="card shadow-sm border-0 mt-4 rounded-4">
                <div className="card-body p-4">
                  <h5 className="card-title mb-3">Category Breakdown</h5>

                  <div className="table-responsive">
                    <table className="table align-middle mb-0">
                      <thead>
                        <tr>
                          <th>Category</th>
                          <th className="text-end">Amount</th>
                        </tr>
                      </thead>
                      <tbody>
                        {categoryBreakdown.map((item) => (
                          <tr key={item.categoryId}>
                            <td>{item.categoryName}</td>
                            <td className="text-end">
                              {formatCurrency(item.totalAmount)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </div>
    </MainLayout>
  );
}