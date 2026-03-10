import { useState } from "react";
import { Link } from "react-router-dom";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import api from "../../api/axios";

const forgotPasswordSchema = Yup.object({
  email: Yup.string()
    .email("Enter a valid email")
    .required("Email is required"),
});

export default function ForgotPassword() {
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const initialValues = {
    email: "",
  };

  const handleSubmit = async (values, { setSubmitting, resetForm }) => {
    setApiError("");
    setSuccessMessage("");

    try {
      const res = await api.post("/auth/forgot-password", values);
      setSuccessMessage(
        res.data.message || "Password reset instructions sent to your email."
      );
      resetForm();
    } catch (error) {
      setApiError(
        error.response?.data?.message ||
          "Unable to process forgot password request."
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
              <h2 className="text-center mb-4">Forgot Password</h2>

              {apiError && <div className="alert alert-danger">{apiError}</div>}
              {successMessage && (
                <div className="alert alert-success">{successMessage}</div>
              )}

              <Formik
                initialValues={initialValues}
                validationSchema={forgotPasswordSchema}
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
                        placeholder="Enter your registered email"
                      />
                      <ErrorMessage
                        name="email"
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
                        {isSubmitting ? "Sending..." : "Send Reset Link"}
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