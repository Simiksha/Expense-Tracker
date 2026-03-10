import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import api from "../../api/axios";

const resetPasswordSchema = Yup.object({
  newPassword: Yup.string()
    .min(6, "Password must be at least 6 characters")
    .required("New password is required"),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref("newPassword")], "Passwords do not match")
    .required("Confirm password is required"),
});

export default function ResetPassword() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const tokenFromUrl = searchParams.get("token") || "";

  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const initialValues = {
    token: tokenFromUrl,
    newPassword: "",
    confirmPassword: "",
  };

  const handleSubmit = async (values, { setSubmitting, resetForm }) => {
    setApiError("");
    setSuccessMessage("");

    try {
      const payload = {
        token: values.token,
        newPassword: values.newPassword,
      };

      const res = await api.post("/auth/reset-password", payload);

      setSuccessMessage(
        res.data.message || "Password reset successful. Redirecting to login..."
      );
      resetForm();

      setTimeout(() => {
        navigate("/login");
      }, 1500);
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Failed to reset password."
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
              <h2 className="text-center mb-4">Reset Password</h2>

              {apiError && <div className="alert alert-danger">{apiError}</div>}
              {successMessage && (
                <div className="alert alert-success">{successMessage}</div>
              )}

              <Formik
                initialValues={initialValues}
                enableReinitialize
                validationSchema={resetPasswordSchema}
                onSubmit={handleSubmit}
              >
                {({ isSubmitting, values }) => (
                  <Form>
                    <div className="mb-3">
                      <label className="form-label">Reset Token</label>
                      <Field
                        type="text"
                        name="token"
                        className="form-control"
                        placeholder="Enter reset token"
                      />
                      {!values.token && (
                        <div className="text-muted small mt-1">
                          Paste the token from your email.
                        </div>
                      )}
                    </div>

                    <div className="mb-3">
                      <label className="form-label">New Password</label>
                      <div className="input-group">
                        <Field
                          type={showPassword ? "text" : "password"}
                          name="newPassword"
                          className="form-control"
                          placeholder="Enter new password"
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
                        name="newPassword"
                        component="div"
                        className="text-danger small mt-1"
                      />
                    </div>

                    <div className="mb-3">
                      <label className="form-label">Confirm New Password</label>
                      <div className="input-group">
                        <Field
                          type={showConfirmPassword ? "text" : "password"}
                          name="confirmPassword"
                          className="form-control"
                          placeholder="Confirm new password"
                        />
                        <button
                          type="button"
                          className="btn btn-outline-secondary"
                          onClick={() =>
                            setShowConfirmPassword((prev) => !prev)
                          }
                        >
                          {showConfirmPassword ? "Hide" : "Show"}
                        </button>
                      </div>
                      <ErrorMessage
                        name="confirmPassword"
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
                        {isSubmitting ? "Resetting..." : "Reset Password"}
                      </button>
                    </div>
                  </Form>
                )}
              </Formik>

              <div className="text-center">
                <Link to="/login" className="text-decoration-none">
                  Back to Login
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}