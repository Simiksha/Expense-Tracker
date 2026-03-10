import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Formik, Form, Field, ErrorMessage } from "formik";
import * as Yup from "yup";
import api from "../../api/axios";

const registerSchema = Yup.object({
  name: Yup.string()
    .trim()
    .required("Name is required"),
  email: Yup.string()
    .email("Enter a valid email")
    .required("Email is required"),
  password: Yup.string()
    .min(6, "Password must be at least 6 characters")
    .required("Password is required"),
  confirmPassword: Yup.string()
    .oneOf([Yup.ref("password")], "Passwords do not match")
    .required("Confirm password is required"),
});

export default function Register() {
  const navigate = useNavigate();
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const initialValues = {
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  };

  const handleSubmit = async (values, { setSubmitting, resetForm }) => {
    setApiError("");
    setSuccessMessage("");

    try {
      const payload = {
        name: values.name,
        email: values.email,
        password: values.password,
      };

      const res = await api.post("/auth/register", payload);

      setSuccessMessage(
        res.data.message || "Registration successful. Please verify your email."
      );
      resetForm();

      setTimeout(() => {
        navigate("/login");
      }, 1500);
    } catch (error) {
      setApiError(
        error.response?.data?.message || "Registration failed. Please try again."
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
              <h2 className="text-center mb-4">Register</h2>

              {apiError && <div className="alert alert-danger">{apiError}</div>}
              {successMessage && (
                <div className="alert alert-success">{successMessage}</div>
              )}

              <Formik
                initialValues={initialValues}
                validationSchema={registerSchema}
                onSubmit={handleSubmit}
              >
                {({ isSubmitting }) => (
                  <Form>
                    <div className="mb-3">
                      <label className="form-label">Name</label>
                      <Field
                        type="text"
                        name="name"
                        className="form-control"
                        placeholder="Enter your name"
                      />
                      <ErrorMessage
                        name="name"
                        component="div"
                        className="text-danger small mt-1"
                      />
                    </div>

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

                    <div className="mb-3">
                      <label className="form-label">Confirm Password</label>
                      <div className="input-group">
                        <Field
                          type={showConfirmPassword ? "text" : "password"}
                          name="confirmPassword"
                          className="form-control"
                          placeholder="Confirm your password"
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
                        {isSubmitting ? "Registering..." : "Register"}
                      </button>
                    </div>
                  </Form>
                )}
              </Formik>

              <div className="text-center">
                Already have an account?{" "}
                <Link to="/login" className="text-decoration-none">
                  Login
                </Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}