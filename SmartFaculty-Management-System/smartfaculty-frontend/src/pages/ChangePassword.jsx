import { useState } from "react";
import axios from "axios";

function ChangePassword() {

    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmNewPassword, setConfirmNewPassword] = useState("");

    const [loading, setLoading] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const handleSubmit = async (event) => {

        event.preventDefault();

        setError("");
        setSuccess("");

        // =================================================
        // VALIDATION
        // =================================================

        if (!currentPassword) {
            setError("Please enter your current password.");
            return;
        }

        if (!newPassword) {
            setError("Please enter your new password.");
            return;
        }

        if (!confirmNewPassword) {
            setError("Please confirm your new password.");
            return;
        }

        if (newPassword.length < 6) {
            setError(
                "New password must contain at least 6 characters."
            );
            return;
        }

        if (newPassword !== confirmNewPassword) {
            setError(
                "New password and confirm password do not match."
            );
            return;
        }

        if (currentPassword === newPassword) {
            setError(
                "New password must be different from your current password."
            );
            return;
        }

        // =================================================
        // TOKEN
        // =================================================

        const token = localStorage.getItem("token");

        if (!token) {
            setError("You are not logged in.");
            return;
        }

        // =================================================
        // REQUEST
        // =================================================

        const requestData = {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmNewPassword: confirmNewPassword
        };

        try {

            setLoading(true);

            await axios.put(
                "http://localhost:8080/api/auth/change-password",
                requestData,
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            setSuccess(
                "Password changed successfully."
            );

            setCurrentPassword("");
            setNewPassword("");
            setConfirmNewPassword("");

        } catch (err) {

            console.error(
                "Change password error:",
                err
            );

            console.error(
                "Server response:",
                err.response?.data
            );

            setError(
                err.response?.data?.message ||
                "Unable to change password."
            );

        } finally {

            setLoading(false);
        }
    };

    return (

        <div
            style={{
                backgroundColor: "#ffffff",
                border: "1px solid #d5e0e8",
                borderRadius: "10px",
                padding: "25px 30px",
                boxShadow:
                    "0 2px 8px rgba(0,0,0,0.05)"
            }}
        >

            <h3
                style={{
                    margin: "0 0 8px",
                    color: "#23415f",
                    fontSize: "20px"
                }}
            >
                Change Password
            </h3>

            <p
                style={{
                    margin: "0 0 22px",
                    color: "#687b8d",
                    fontSize: "14px"
                }}
            >
                Update your account password securely.
            </p>

            {/* =================================================
                ERROR
            ================================================= */}

            {error && (

                <div
                    style={{
                        backgroundColor: "#fdecea",
                        color: "#c0392b",
                        border: "1px solid #f5c6cb",
                        borderRadius: "6px",
                        padding: "12px 14px",
                        marginBottom: "18px",
                        fontSize: "13px"
                    }}
                >
                    {error}
                </div>

            )}

            {/* =================================================
                SUCCESS
            ================================================= */}

            {success && (

                <div
                    style={{
                        backgroundColor: "#eaf7ee",
                        color: "#18794e",
                        border: "1px solid #b7dfc6",
                        borderRadius: "6px",
                        padding: "12px 14px",
                        marginBottom: "18px",
                        fontSize: "13px"
                    }}
                >
                    {success}
                </div>

            )}

            <form onSubmit={handleSubmit}>

                {/* =================================================
                    CURRENT PASSWORD
                ================================================= */}

                <div
                    style={{
                        marginBottom: "16px"
                    }}
                >

                    <label
                        style={{
                            display: "block",
                            marginBottom: "7px",
                            fontSize: "13px",
                            fontWeight: "600",
                            color: "#23415f"
                        }}
                    >
                        Current Password
                    </label>

                    <input
                        type="password"
                        value={currentPassword}
                        onChange={(event) =>
                            setCurrentPassword(
                                event.target.value
                            )
                        }
                        placeholder="Enter current password"
                        disabled={loading}
                        style={{
                            width: "100%",
                            boxSizing: "border-box",
                            padding: "11px 12px",
                            border: "1px solid #d5e0e8",
                            borderRadius: "6px",
                            fontSize: "14px",
                            outline: "none"
                        }}
                    />

                </div>


                {/* =================================================
                    NEW PASSWORD
                ================================================= */}

                <div
                    style={{
                        marginBottom: "16px"
                    }}
                >

                    <label
                        style={{
                            display: "block",
                            marginBottom: "7px",
                            fontSize: "13px",
                            fontWeight: "600",
                            color: "#23415f"
                        }}
                    >
                        New Password
                    </label>

                    <input
                        type="password"
                        value={newPassword}
                        onChange={(event) =>
                            setNewPassword(
                                event.target.value
                            )
                        }
                        placeholder="Enter new password"
                        disabled={loading}
                        style={{
                            width: "100%",
                            boxSizing: "border-box",
                            padding: "11px 12px",
                            border: "1px solid #d5e0e8",
                            borderRadius: "6px",
                            fontSize: "14px",
                            outline: "none"
                        }}
                    />

                </div>


                {/* =================================================
                    CONFIRM PASSWORD
                ================================================= */}

                <div
                    style={{
                        marginBottom: "22px"
                    }}
                >

                    <label
                        style={{
                            display: "block",
                            marginBottom: "7px",
                            fontSize: "13px",
                            fontWeight: "600",
                            color: "#23415f"
                        }}
                    >
                        Confirm New Password
                    </label>

                    <input
                        type="password"
                        value={confirmNewPassword}
                        onChange={(event) =>
                            setConfirmNewPassword(
                                event.target.value
                            )
                        }
                        placeholder="Confirm new password"
                        disabled={loading}
                        style={{
                            width: "100%",
                            boxSizing: "border-box",
                            padding: "11px 12px",
                            border: "1px solid #d5e0e8",
                            borderRadius: "6px",
                            fontSize: "14px",
                            outline: "none"
                        }}
                    />

                </div>


                {/* =================================================
                    SUBMIT
                ================================================= */}

                <button
                    type="submit"
                    disabled={loading}
                    style={{
                        padding: "10px 18px",
                        border: "none",
                        borderRadius: "6px",
                        backgroundColor: "#23415f",
                        color: "#ffffff",
                        cursor:
                            loading
                                ? "not-allowed"
                                : "pointer",
                        fontSize: "14px",
                        fontWeight: "600",
                        opacity:
                            loading
                                ? 0.7
                                : 1
                    }}
                >
                    {loading
                        ? "Changing Password..."
                        : "Change Password"}
                </button>

            </form>

        </div>
    );
}

export default ChangePassword;