import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import api from "../../api/axios";

const loginSchema = Yup.object({
  email: Yup.string()
    .email("Enter a valid email")
    .required("Email is required"),
  password: Yup.string().required("Password is required"),
});

export default function Login() {
  const navigate = useNavigate();
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const location = useLocation();
  const from = location.state?.from?.pathname || "/dashboard";

  const initialValues = {
    email: "",
    password: "",
  };

  const handleSubmit = async (values, { setSubmitting }) => {
    setApiError("");
    setSuccessMessage("");

    try {
      const res = await api.post("/auth/login", values);

      localStorage.setItem("token", res.data.token);
      localStorage.setItem("user", JSON.stringify({
        userId: res.data.userId,
        name: res.data.name,
        email: res.data.email,
        role: res.data.role,
      }));

      setSuccessMessage("Login successful");
      navigate(from, { replace: true });
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Login failed. Please try again."
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container py-5">
      <div className="row justify-content-center">
        <div className="col-md-6 col-lg-5">
          <div className="card shadow-sm">
            <div className="card-body p-4">
              <h2 className="text-center mb-4">Login</h2>

              {apiError && <div className="alert alert-danger">{apiError}</div>}
              {successMessage && (
                <div className="alert alert-success">{successMessage}</div>
              )}

              <Formik
                initialValues={initialValues}
                validationSchema={loginSchema}
                onSubmit={handleSubmit}
              >
                {({ isSubmitting }) => (
                  <Form>
                    <div className="mb-3">
                      <label className="form-label">Email</label>
                      <Field
                        type="email"
                        name="email"
                        className="form-control"
                        placeholder="Enter your email"
                      />
                      <ErrorMessage
                        name="email"
                        component="div"
                        className="text-danger small mt-1"
                      />
                    </div>

                    <div className="mb-3">
                      <label className="form-label">Password</label>
                      <div className="input-group">
                        <Field
                          type={showPassword ? "text" : "password"}
                          name="password"
                          className="form-control"
                          placeholder="Enter your password"
                        />
                        <button
                          type="button"
                          className="btn btn-outline-secondary"
                          onClick={() => setShowPassword((prev) => !prev)}
                        >
                          {showPassword ? "Hide" : "Show"}
                        </button>
                      </div>
                      <ErrorMessage
                        name="password"
                        component="div"
                        className="text-danger small mt-1"
                      />
                    </div>

                    <div className="d-grid mb-3">
                      <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={isSubmitting}
                      >
                        {isSubmitting ? "Logging in..." : "Login"}
                      </button>
                    </div>
                  </Form>
                )}
              </Formik>

              <div className="text-center mb-2">
                <Link to="/forgot-password" className="text-decoration-none">
                  Forgot password?
                </Link>
              </div>

              <div className="text-center">
                Don&apos;t have an account?{" "}
                <Link to="/register" className="text-decoration-none">
                  Register
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}