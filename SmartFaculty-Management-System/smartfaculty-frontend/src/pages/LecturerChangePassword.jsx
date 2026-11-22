import { useState } from "react";
import axios from "axios";

function LecturerChangePassword() {

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

    if (!currentPassword) {
        setError("Please enter your current password.");
        return;
    }

    if (!newPassword) {
        setError("Please enter your new password.");
        return;
    }

    if (newPassword.length < 6) {
        setError("New password must contain at least 6 characters.");
        return;
    }

    if (newPassword !== confirmNewPassword) {
        setError("New password and confirm password do not match.");
        return;
    }

    if (currentPassword === newPassword) {
        setError("New password must be different from your current password.");
        return;
    }

    const token = localStorage.getItem("token");

    try {

        setLoading(true);

        await axios.put(
            "http://localhost:8080/api/auth/change-password",
            {
                currentPassword,
                newPassword,
                confirmNewPassword
            },
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

        setSuccess("Password changed successfully.");

        setCurrentPassword("");
        setNewPassword("");
        setConfirmNewPassword("");

    } catch (err) {

        console.error(err);

        setError(
            err.response?.data?.message ||
            err.response?.data ||
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
            padding: "25px",
            borderRadius: "12px",
            border: "1px solid #d9e3eb",
            boxShadow: "0 2px 8px rgba(0,0,0,0.05)"
        }}
    >

        <h2
            style={{
                marginTop: 0,
                color: "#23415f"
            }}
        >
            Change Password
        </h2>

        <p
            style={{
                color: "#666",
                marginBottom: "20px"
            }}
        >
            Update your lecturer account password securely.
        </p>

        {error && (
            <div
                style={{
                    backgroundColor: "#ffe8e8",
                    color: "#b00020",
                    padding: "12px",
                    borderRadius: "8px",
                    marginBottom: "15px"
                }}
            >
                {error}
            </div>
        )}

        {success && (
            <div
                style={{
                    backgroundColor: "#e9f8ef",
                    color: "#157347",
                    padding: "12px",
                    borderRadius: "8px",
                    marginBottom: "15px"
                }}
            >
                {success}
            </div>
        )}

        <form onSubmit={handleSubmit}>

            <div style={{ marginBottom: "18px" }}>
                <label style={{ display: "block", marginBottom: "6px" }}>
                    Current Password
                </label>

                <input
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    style={inputStyle}
                />
            </div>

            <div style={{ marginBottom: "18px" }}>
                <label style={{ display: "block", marginBottom: "6px" }}>
                    New Password
                </label>

                <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    style={inputStyle}
                />
            </div>

            <div style={{ marginBottom: "20px" }}>
                <label style={{ display: "block", marginBottom: "6px" }}>
                    Confirm New Password
                </label>

                <input
                    type="password"
                    value={confirmNewPassword}
                    onChange={(e) => setConfirmNewPassword(e.target.value)}
                    style={inputStyle}
                />
            </div>

            <button
                type="submit"
                disabled={loading}
                style={{
                    backgroundColor: "#23415f",
                    color: "#fff",
                    padding: "11px 18px",
                    border: "none",
                    borderRadius: "7px",
                    cursor: loading ? "not-allowed" : "pointer",
                    fontWeight: "600"
                }}
            >
                {loading ? "Updating..." : "Change Password"}
            </button>

        </form>

    </div>
);


}

const inputStyle = {
width: "100%",
padding: "10px 12px",
borderRadius: "7px",
border: "1px solid #ced4da",
fontSize: "14px",
boxSizing: "border-box"
};

export default LecturerChangePassword;
