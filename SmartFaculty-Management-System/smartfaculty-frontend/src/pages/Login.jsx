import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

import { login } from "../services/authService";
import "./Login.css";


function Login() {

    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);


    // =====================================================
    // LOGIN
    // =====================================================

    const handleLogin = async (e) => {

        e.preventDefault();

        setError("");
        setLoading(true);


        try {

            // =================================================
            // CALL LOGIN API
            // =================================================

            const data = await login(
                username.trim(),
                password
            );


            console.log(
                "Login response:",
                data
            );


            // =================================================
            // CHECK LOGIN RESPONSE
            // =================================================

            if (!data || !data.token) {

                throw new Error(
                    "Invalid login response from server."
                );
            }


            // =================================================
            // SAVE LOGIN INFORMATION
            // =================================================

            localStorage.setItem(
                "token",
                data.token
            );


            localStorage.setItem(
                "username",
                data.username || username.trim()
            );


            localStorage.setItem(
                "role",
                data.role
            );


            console.log(
                "Logged in role:",
                data.role
            );


            // =================================================
            // ADMIN
            // =================================================

            if (data.role === "ADMIN") {

                navigate(
                    "/admin-dashboard"
                );

                return;
            }


            // =================================================
            // HOD
            // =================================================

            if (data.role === "HOD") {

                navigate(
                    "/hod-dashboard"
                );

                return;
            }


            // =================================================
            // LECTURER
            // =================================================

            if (data.role === "LECTURER") {

                navigate(
                    "/lecturer-dashboard"
                );

                return;
            }


            // =================================================
            // STUDENT
            // =================================================

            if (data.role === "STUDENT") {

                await handleStudentLogin(
                    data.username || username.trim(),
                    data.token
                );

                return;
            }


            // =================================================
            // UNKNOWN ROLE
            // =================================================

            setError(
                "Your account role is not recognized."
            );

        } catch (error) {

            console.error(
                "Login error:",
                error
            );


            // =================================================
            // CLEAR LOGIN INFORMATION IF LOGIN FAILED
            // =================================================

            if (!error.response) {

                localStorage.removeItem(
                    "token"
                );

                localStorage.removeItem(
                    "username"
                );

                localStorage.removeItem(
                    "role"
                );

                localStorage.removeItem(
                    "studentId"
                );
            }


            // =================================================
            // ERROR MESSAGE
            // =================================================

            if (
                error.response?.data?.message
            ) {

                setError(
                    error.response.data.message
                );

            } else if (
                typeof error.response?.data === "string"
            ) {

                setError(
                    error.response.data
                );

            } else if (
                error.message
            ) {

                setError(
                    error.message
                );

            } else {

                setError(
                    "Invalid username or password."
                );
            }

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // STUDENT LOGIN
    // =====================================================

    const handleStudentLogin = async (
        loggedInUsername,
        token
    ) => {

        const config = {
            headers: {
                Authorization: `Bearer ${token}`
            }
        };


        // =================================================
        // GET STUDENT PROFILE
        // =================================================

        const studentResponse = await axios.get(
            "http://localhost:8080/api/students",
            config
        );


        const students =
            Array.isArray(studentResponse.data)
                ? studentResponse.data
                : [];


        // =================================================
        // FIND LOGGED-IN STUDENT
        // =================================================

        const currentStudent =
            students.find(
                (student) =>
                    student.registrationNumber ===
                        loggedInUsername ||

                    student.username ===
                        loggedInUsername ||

                    student.email ===
                        loggedInUsername
            );


        if (!currentStudent) {

            throw new Error(
                "Student profile could not be found."
            );
        }


        // =================================================
        // GET STUDENT ID
        // =================================================

        const studentId =
            currentStudent.studentId ||
            currentStudent.student_id;


        if (!studentId) {

            throw new Error(
                "Student ID could not be found."
            );
        }


        // =================================================
        // SAVE STUDENT ID
        // =================================================

        localStorage.setItem(
            "studentId",
            String(studentId)
        );


        // =================================================
        // CHECK FIRST-TIME REGISTRATION STATUS
        // =================================================
        //
        // IMPORTANT:
        //
        // We do NOT check whether subject registrations exist.
        //
        // Compulsory subjects can already be registered
        // automatically when the student account is created.
        //
        // Instead, the backend registration-status endpoint
        // determines whether the registration process itself
        // has been completed.
        //
        // =================================================

        const statusResponse =
            await axios.get(
                "http://localhost:8080/api/student-subject-registrations/status/me",
                config
            );


        const status =
            statusResponse.data || {};


        console.log(
            "Student registration status:",
            status
        );


        // =================================================
        // REGISTRATION COMPLETED
        // =================================================

        if (
            status.complete === true
        ) {

            console.log(
                "Student registration already completed."
            );


            navigate(
                "/student-dashboard"
            );

            return;
        }


        // =================================================
        // REGISTRATION NOT COMPLETED
        // =================================================

        console.log(
            "Student registration is not completed yet."
        );


        navigate(
            "/student-registration"
        );
    };


    // =====================================================
    // LOGIN PAGE
    // =====================================================

    return (

        <div className="login-page">

            <div className="login-container">

                <h1>
                    Smart Faculty
                </h1>


                <h2>
                    Login
                </h2>


                <form
                    className="login-form"
                    onSubmit={handleLogin}
                >

                    {/* =================================================
                        USERNAME
                    ================================================= */}

                    <label>
                        Username
                    </label>


                    <input
                        type="text"
                        value={username}
                        onChange={(e) =>
                            setUsername(
                                e.target.value
                            )
                        }
                        placeholder="Enter username"
                        required
                    />


                    <br />
                    <br />


                    {/* =================================================
                        PASSWORD
                    ================================================= */}

                    <label>
                        Password
                    </label>


                    <input
                        type="password"
                        value={password}
                        onChange={(e) =>
                            setPassword(
                                e.target.value
                            )
                        }
                        placeholder="Enter password"
                        required
                    />


                    {/* =================================================
                        LOGIN BUTTON
                    ================================================= */}

                    <button
                        type="submit"
                        disabled={loading}
                    >

                        {loading
                            ? "Logging in..."
                            : "Login"}

                    </button>

                </form>


                {/* =====================================================
                    ERROR MESSAGE
                ===================================================== */}

                {error && (

                    <p className="login-error">
                        {error}
                    </p>

                )}

            </div>

        </div>
    );
}


export default Login;