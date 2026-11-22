import { useEffect, useState } from "react";
import axios from "axios";
import ConfirmDialog from "../../components/ConfirmDialog";
import StatusDialog from "../../components/StatusDialog";


function AdminAccountManagement() {

    // =====================================================
    // STATES
    // =====================================================

    const [accounts, setAccounts] = useState([]);

    const [loading, setLoading] = useState(true);

    const [actionLoading, setActionLoading] =
        useState(null);

    const [error, setError] = useState("");

    const [success, setSuccess] = useState("");

    const [confirmDialog, setConfirmDialog] = useState({
    open: false,
    message: "",
    onConfirm: null
});

const showConfirmDialog = (message, onConfirm) => {
    setConfirmDialog({
        open: true,
        message,
        onConfirm
    });
};

const closeConfirmDialog = () => {
    setConfirmDialog({
        open: false,
        message: "",
        onConfirm: null
    });
};

const handleConfirmAction = () => {
    const action = confirmDialog.onConfirm;

    closeConfirmDialog();

    if (action) {
        action();
    }
};
    const [roleEditAccount, setRoleEditAccount] = useState(null);

    const [selectedRole, setSelectedRole] = useState("");

    const [roleLoading, setRoleLoading] = useState(false);


    // =====================================================
    // LOAD ACCOUNTS
    // =====================================================

    useEffect(() => {

        loadAccounts();

    }, []);


    const loadAccounts = async () => {

        try {

            setLoading(true);

            setError("");

            const token =
                localStorage.getItem("token");


            if (!token) {

                setError(
                    "You are not logged in."
                );

                return;
            }


            const response =
                await axios.get(
                    "http://localhost:8080/api/user-accounts",
                    {
                        headers: {
                            Authorization:
                                `Bearer ${token}`
                        }
                    }
                );


            setAccounts(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );


        } catch (err) {

            console.error(
                "Error loading user accounts:",
                err
            );


            if (
                err.response?.status === 403
            ) {

                setError(
                    "Access denied. Only administrators can manage user accounts."
                );

            } else {

                setError(
                    err.response?.data?.message ||
                    "Unable to load user accounts."
                );
            }

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // ENABLE ACCOUNT
    // =====================================================

    const enableAccount = async (
        userId
    ) => {

        try {

            setActionLoading(userId);

            setError("");

            setSuccess("");


            const token =
                localStorage.getItem("token");


            await axios.put(
                `http://localhost:8080/api/user-accounts/${userId}/enable`,
                {},
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            setSuccess(
                "User account enabled successfully."
            );


            await loadAccounts();


        } catch (err) {

            console.error(
                "Error enabling account:",
                err
            );


            setError(
                err.response?.data?.message ||
                "Unable to enable user account."
            );

        } finally {

            setActionLoading(null);
        }
    };


    // =====================================================
    // DISABLE ACCOUNT
    // =====================================================

    const disableAccount = async (
        account
    ) => {

        if (
            account.role === "ADMIN"
        ) {

            setError(
                "Administrator accounts cannot be disabled."
            );

            return;
        }


        const confirmed =
            window.confirm(
                `Are you sure you want to disable the account "${account.username}"?`
            );


        if (!confirmed) {

            return;
        }


        try {

            setActionLoading(
                account.userId
            );

            setError("");

            setSuccess("");


            const token =
                localStorage.getItem("token");


            await axios.put(
                `http://localhost:8080/api/user-accounts/${account.userId}/disable`,
                {},
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            setSuccess(
                `Account "${account.username}" has been disabled.`
            );


            await loadAccounts();


        } catch (err) {

            console.error(
                "Error disabling account:",
                err
            );


            setError(
                err.response?.data?.message ||
                "Unable to disable user account."
            );

        } finally {

            setActionLoading(null);
        }
    };

    // =====================================================
// EDIT USER ROLE
// =====================================================

const openRoleEditor = (account) => {

    if (
        account.role !== "LECTURER" &&
        account.role !== "HOD"
    ) {
        return;
    }

    setRoleEditAccount(account);

    setSelectedRole(
        account.role === "LECTURER"
            ? "HOD"
            : "LECTURER"
    );

    setError("");

    setSuccess("");
};


// =====================================================
// CLOSE ROLE EDITOR
// =====================================================

const closeRoleEditor = () => {

    if (roleLoading) {
        return;
    }

    setRoleEditAccount(null);

    setSelectedRole("");
};


// =====================================================
// UPDATE USER ROLE
// =====================================================

const updateUserRole = () => {
    if (!roleEditAccount?.userId) {
        setError("User account could not be found.");
        return;
    }

    if (selectedRole !== "LECTURER" && selectedRole !== "HOD") {
        setError("Please select a valid role.");
        return;
    }

    const currentRole = roleEditAccount.role;

    if (currentRole === selectedRole) {
        setError("The selected role is already assigned to this account.");
        return;
    }

    const username = roleEditAccount.username;
    const userId = roleEditAccount.userId;
    const newRole = selectedRole;

    showConfirmDialog(
        `Are you sure you want to change the role of "${username}" from ${currentRole} to ${newRole}?`,
        async () => {
            try {
                setRoleLoading(true);
                setError("");
                setSuccess("");

                const token = localStorage.getItem("token");

                const response = await axios.put(
                    `http://localhost:8080/api/user-accounts/${userId}/role`,
                    {},
                    {
                        headers: {
                            Authorization: `Bearer ${token}`
                        },
                        params: {
                            role: newRole
                        }
                    }
                );

                const updatedAccount = response.data;

                setAccounts(previousAccounts =>
                    previousAccounts.map(account =>
                        account.userId === userId
                            ? updatedAccount
                            : account
                    )
                );

                setSuccess(
                    `Role changed successfully from ${formatRole(currentRole)} to ${newRole === "HOD" ? "HOD" : "Lecturer"}.`
                );

                setRoleEditAccount(null);
                setSelectedRole("");

            }catch (err) {
    console.error("========== ROLE UPDATE ERROR ==========");
    console.error("Error:", err);
    console.error("HTTP Status:", err.response?.status);
    console.error("Response Data:", err.response?.data);
    console.error("Response Headers:", err.response?.headers);

    const serverMessage = err.response?.data;

    let errorMessage = "Unable to update user role.";

    if (typeof serverMessage === "string") {
        errorMessage = serverMessage;
    } else if (serverMessage?.message) {
        errorMessage = serverMessage.message;
    } else if (serverMessage?.error) {
        errorMessage = serverMessage.error;
    } else if (err.message) {
        errorMessage = err.message;
    }

    setError(errorMessage);

} finally {
    setRoleUpdateLoading(false);
}
        }
    );
};

    // =====================================================
    // FORMAT ROLE
    // =====================================================

    const formatRole = (
        role
    ) => {

        if (!role) {

            return "-";
        }


        return role
            .replaceAll("_", " ")
            .toLowerCase()
            .replace(
                /\b\w/g,
                letter =>
                    letter.toUpperCase()
            );
    };


    // =====================================================
    // LOADING
    // =====================================================

    if (loading) {

        return (

            <div
                style={{
                    backgroundColor: "#ffffff",
                    padding: "30px",
                    borderRadius: "10px",
                    border: "1px solid #d5e0e8",
                    textAlign: "center"
                }}
            >

                <p
                    style={{
                        margin: 0,
                        color: "#687b8d"
                    }}
                >
                    Loading user accounts...
                </p>

            </div>
        );
    }


    // =====================================================
    // MAIN
    // =====================================================

    return (

        <div
            style={{
                backgroundColor: "#ffffff",
                borderRadius: "10px",
                border: "1px solid #d5e0e8",
                padding: "25px 30px",
                boxShadow:
                    "0 2px 8px rgba(0,0,0,0.05)"
            }}
        >

            {/* =================================================
                HEADER
            ================================================= */}

            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    gap: "15px",
                    flexWrap: "wrap",
                    marginBottom: "20px"
                }}
            >

                <div>

                    <h2
                        style={{
                            margin: "0 0 6px",
                            color: "#23415f"
                        }}
                    >
                        Account Management
                    </h2>


                    <p
                        style={{
                            margin: 0,
                            color: "#687b8d",
                            fontSize: "14px"
                        }}
                    >
                        Manage student, lecturer and HOD
                        user accounts.
                    </p>

                </div>


                <button
                    type="button"
                    onClick={loadAccounts}
                    style={{
                        padding: "9px 15px",
                        border: "1px solid #23415f",
                        borderRadius: "6px",
                        backgroundColor: "#ffffff",
                        color: "#23415f",
                        cursor: "pointer",
                        fontWeight: "600"
                    }}
                >
                    Refresh
                </button>

            </div>


            {/* =================================================
                ERROR
            ================================================= */}

            {error && (

                <div
                    style={{
                        backgroundColor: "#ffe8e8",
                        color: "#b00020",
                        padding: "12px 15px",
                        borderRadius: "7px",
                        marginBottom: "15px",
                        fontSize: "14px"
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
                        backgroundColor: "#e9f8ef",
                        color: "#157347",
                        padding: "12px 15px",
                        borderRadius: "7px",
                        marginBottom: "15px",
                        fontSize: "14px"
                    }}
                >
                    {success}
                </div>

            )}


            {/* =================================================
                NO ACCOUNTS
            ================================================= */}

            {accounts.length === 0 ? (

                <div
                    style={{
                        padding: "35px",
                        textAlign: "center",
                        border: "1px solid #e4e9ed",
                        borderRadius: "8px",
                        color: "#687b8d",
                        backgroundColor: "#f8fafb"
                    }}
                >
                    No user accounts found.
                </div>

            ) : (

                /* =================================================
                   TABLE
                ================================================= */

                <div
                    style={{
                        overflowX: "auto"
                    }}
                >

                    <table
                        style={{
                            width: "100%",
                            borderCollapse: "collapse"
                        }}
                    >

                        <thead>

                            <tr>

                                <th style={headerStyle}>
                                    #
                                </th>

                                <th style={headerStyle}>
                                    Username
                                </th>

                                <th style={headerStyle}>
                                    Role
                                </th>

                                <th style={headerStyle}>
                                    Status
                                </th>

                                <th style={headerStyle}>
                                    Actions
                                </th>

                            </tr>

                        </thead>


                        <tbody>

                            {accounts.map(
                                (account, index) => (

                                    <tr
                                        key={
                                            account.userId
                                        }
                                        style={{
                                            borderBottom:
                                                "1px solid #e4e9ed"
                                        }}
                                    >

                                        {/* NUMBER */}

                                        <td style={cellStyle}>
                                            {index + 1}
                                        </td>


                                        {/* USERNAME */}

                                        <td style={cellStyle}>

                                            <strong>
                                                {
                                                    account.username
                                                }
                                            </strong>

                                        </td>


                                        {/* ROLE */}

                                        <td style={cellStyle}>

                                            {formatRole(
                                                account.role
                                            )}

                                        </td>


                                        {/* STATUS */}

                                        <td style={cellStyle}>

                                            {account.enabled ? (

                                                <span
                                                    style={{
                                                        display:
                                                            "inline-block",
                                                        backgroundColor:
                                                            "#e9f8ef",
                                                        color:
                                                            "#157347",
                                                        borderRadius:
                                                            "20px",
                                                        padding:
                                                            "5px 12px",
                                                        fontSize:
                                                            "12px",
                                                        fontWeight:
                                                            "600"
                                                    }}
                                                >
                                                    Active
                                                </span>

                                            ) : (

                                                <span
                                                    style={{
                                                        display:
                                                            "inline-block",
                                                        backgroundColor:
                                                            "#ffe8e8",
                                                        color:
                                                            "#b00020",
                                                        borderRadius:
                                                            "20px",
                                                        padding:
                                                            "5px 12px",
                                                        fontSize:
                                                            "12px",
                                                        fontWeight:
                                                            "600"
                                                    }}
                                                >
                                                    Disabled
                                                </span>

                                            )}

                                        </td>


                                        {/* ACTIONS */}

<td style={cellStyle}>

    <div
        style={{
            display: "grid",
            gridTemplateColumns: "90px 90px 125px",
            justifyContent: "center",
            alignItems: "center",
            gap: "10px"
        }}
    >

        {/* EDIT ROLE */}

        {(
            account.role === "LECTURER" ||
            account.role === "HOD"
        ) ? (

            <button
                type="button"
                onClick={() =>
                    openRoleEdit(account)
                }
                style={editRoleButtonStyle}
            >
                Edit Role
            </button>

        ) : (

            <div></div>

        )}


        {/* ENABLE / DISABLE */}

        <button
            type="button"
            onClick={() =>
                changeAccountStatus(
                    account,
                    enabled
                        ? "disable"
                        : "enable"
                )
            }
            disabled={
                accountActionLoading === actionKey
            }
            style={
                enabled
                    ? disableAccountButtonStyle
                    : enableAccountButtonStyle
            }
        >
            {
                accountActionLoading === actionKey
                    ? "Please wait..."
                    : enabled
                        ? "Disable"
                        : "Enable"
            }
        </button>


        {/* RESET PASSWORD */}

        <button
            type="button"
            onClick={() =>
                openPasswordReset(account)
            }
            style={resetPasswordButtonStyle}
        >
            Reset Password
        </button>

    </div>

</td>

                                    </tr>

                                )
                            )}

                        </tbody>

                    </table>

                </div>

            )}
            {/* =================================================
    ROLE EDIT MODAL
================================================= */}

{roleEditAccount && (

    <div
        style={{
            position: "fixed",
            inset: 0,
            backgroundColor:
                "rgba(0, 0, 0, 0.45)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 1000,
            padding: "20px"
        }}
    >

        <div
            style={{
                width: "100%",
                maxWidth: "430px",
                backgroundColor: "#ffffff",
                borderRadius: "10px",
                padding: "25px",
                boxShadow:
                    "0 10px 30px rgba(0,0,0,0.2)"
            }}
        >

            {/* HEADER */}

            <div
                style={{
                    display: "flex",
                    justifyContent:
                        "space-between",
                    alignItems: "center",
                    marginBottom: "20px"
                }}
            >

                <h3
                    style={{
                        margin: 0,
                        color: "#23415f"
                    }}
                >
                    Edit User Role
                </h3>

                <button
                    type="button"
                    onClick={closeRoleEditor}
                    disabled={roleLoading}
                    style={{
                        border: "none",
                        backgroundColor:
                            "transparent",
                        fontSize: "24px",
                        color: "#687b8d",
                        cursor:
                            roleLoading
                                ? "not-allowed"
                                : "pointer"
                    }}
                >
                    ×
                </button>

            </div>


            {/* USERNAME */}

            <div
                style={{
                    marginBottom: "15px"
                }}
            >

                <label
                    style={{
                        display: "block",
                        marginBottom: "6px",
                        fontWeight: "600",
                        color: "#23415f"
                    }}
                >
                    Username
                </label>

                <div
                    style={{
                        padding: "10px 12px",
                        backgroundColor:
                            "#f4f7f9",
                        border:
                            "1px solid #d5e0e8",
                        borderRadius: "6px",
                        color: "#687b8d"
                    }}
                >
                    {roleEditAccount.username}
                </div>

            </div>


            {/* CURRENT ROLE */}

            <div
                style={{
                    marginBottom: "15px"
                }}
            >

                <label
                    style={{
                        display: "block",
                        marginBottom: "6px",
                        fontWeight: "600",
                        color: "#23415f"
                    }}
                >
                    Current Role
                </label>

                <div
                    style={{
                        padding: "10px 12px",
                        backgroundColor:
                            "#f4f7f9",
                        border:
                            "1px solid #d5e0e8",
                        borderRadius: "6px",
                        color: "#23415f",
                        fontWeight: "600"
                    }}
                >
                    {formatRole(
                        roleEditAccount.role
                    )}
                </div>

            </div>


            {/* NEW ROLE */}

            <div
                style={{
                    marginBottom: "22px"
                }}
            >

                <label
                    htmlFor="newUserRole"
                    style={{
                        display: "block",
                        marginBottom: "6px",
                        fontWeight: "600",
                        color: "#23415f"
                    }}
                >
                    New Role
                </label>

                <select
                    id="newUserRole"
                    value={selectedRole}
                    onChange={(e) =>
                        setSelectedRole(
                            e.target.value
                        )
                    }
                    disabled={roleLoading}
                    style={{
                        width: "100%",
                        padding: "10px 12px",
                        border:
                            "1px solid #cbd5df",
                        borderRadius: "6px",
                        backgroundColor: "#ffffff",
                        color: "#23415f",
                        fontSize: "14px",
                        outline: "none"
                    }}
                >

                    <option value="LECTURER">
                        Lecturer
                    </option>

                    <option value="HOD">
                        HOD
                    </option>

                </select>

            </div>


            {/* BUTTONS */}

            <div
                style={{
                    display: "flex",
                    justifyContent: "flex-end",
                    gap: "10px"
                }}
            >

                <button
                    type="button"
                    onClick={closeRoleEditor}
                    disabled={roleLoading}
                    style={{
                        padding: "9px 16px",
                        border:
                            "1px solid #cbd5df",
                        borderRadius: "6px",
                        backgroundColor:
                            "#ffffff",
                        color: "#23415f",
                        cursor:
                            roleLoading
                                ? "not-allowed"
                                : "pointer",
                        fontWeight: "600"
                    }}
                >
                    Cancel
                </button>

                <button
                    type="button"
                    onClick={updateUserRole}
                    disabled={roleLoading}
                    style={{
                        padding: "9px 16px",
                        border: "none",
                        borderRadius: "6px",
                        backgroundColor:
                            "#23415f",
                        color: "#ffffff",
                        cursor:
                            roleLoading
                                ? "not-allowed"
                                : "pointer",
                        fontWeight: "600",
                        opacity:
                            roleLoading
                                ? 0.7
                                : 1
                    }}
                >
                    {roleLoading
                        ? "Updating..."
                        : "Update Role"}
                </button>

            </div>

        </div>

    </div>

)}

                    {/* CONFIRMATION DIALOG */}

            <ConfirmDialog
                open={confirmDialog.open}
                message={confirmDialog.message}
                onConfirm={handleConfirmAction}
                onCancel={closeConfirmDialog}
            />

            {/* SUCCESS / ERROR DIALOG */}

            <StatusDialog
                type={error ? "error" : "success"}
                message={error || success}
                onClose={() => {
                    setError("");
                    setSuccess("");
                }}
            />

        </div>

    );
}


// =====================================================
// TABLE STYLES
// =====================================================

const headerStyle = {

    padding: "13px 12px",

    textAlign: "left",

    backgroundColor: "#eef3f7",

    color: "#23415f",

    fontSize: "13px",

    fontWeight: "700",

    borderBottom:
        "2px solid #d5e0e8",

    whiteSpace: "nowrap"
};


const cellStyle = {

    padding: "13px 12px",

    color: "#23415f",

    fontSize: "14px",

    verticalAlign: "middle"
};


export default AdminAccountManagement;