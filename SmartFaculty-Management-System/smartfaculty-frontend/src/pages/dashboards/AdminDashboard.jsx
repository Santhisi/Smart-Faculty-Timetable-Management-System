import { useEffect, useState } from "react";
import axios from "axios";
import AdminTimetableManagement from "./AdminTimetableManagement";
import ConfirmDialog from "../../components/ConfirmDialog";
import StatusDialog from "../../components/StatusDialog";
import "./RequestResponseDialog.css";

function AdminDashboard() {

    // =====================================================
    // STATES
    // =====================================================

    const [students, setStudents] = useState([]);
    const [lecturers, setLecturers] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [allocations, setAllocations] = useState([]);
    const [registrations, setRegistrations] = useState([]);

    const [departments, setDepartments] = useState([]);
    const [specializations, setSpecializations] = useState([]);

    const [loading, setLoading] = useState(true);
    const [formLoading, setFormLoading] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [requestResponseDialog, setRequestResponseDialog] = useState({
    open: false,
    id: null,
    action: "",
    responseText: ""
});

    const [activeSection, setActiveSection] = useState("dashboard");
    const [showForm, setShowForm] = useState(false);
    const [formType, setFormType] = useState("");
    const [editingId, setEditingId] = useState(null);
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


    // =====================================================
    // FILTERS
    // =====================================================

    const [studentRegistrationFilter, setStudentRegistrationFilter] = useState("");
    const [studentYearFilter, setStudentYearFilter] = useState("");
    const [studentSemesterFilter, setStudentSemesterFilter] = useState("");

    const [lecturerCodeFilter, setLecturerCodeFilter] = useState("");
    const [lecturerDepartmentFilter, setLecturerDepartmentFilter] = useState("");

    const [subjectDepartmentFilter, setSubjectDepartmentFilter] = useState("");
    const [subjectYearFilter, setSubjectYearFilter] = useState("");
    const [subjectSemesterFilter, setSubjectSemesterFilter] = useState("");

    const [allocationYearFilter, setAllocationYearFilter] = useState("");
    const [allocationSemesterFilter, setAllocationSemesterFilter] = useState("");

    const [registrationNumberFilter, setRegistrationNumberFilter] = useState("");
    const [registrationYearFilter, setRegistrationYearFilter] = useState("");
    const [registrationSemesterFilter, setRegistrationSemesterFilter] = useState("");

    // =====================================================
    // CHANGE REQUESTS
    // =====================================================

    const [changeRequests, setChangeRequests] = useState([]);
    const [requestLoading, setRequestLoading] = useState(false);
    const [processingRequest, setProcessingRequest] = useState(false);

    // =====================================================
    // ADMIN NOTIFICATIONS
    // =====================================================

    const [showNotifications, setShowNotifications] = useState(false);

    // =====================================================
    // ACCOUNT MANAGEMENT
    // =====================================================

    const [userAccounts, setUserAccounts] = useState([]);
    const [accountLoading, setAccountLoading] = useState(false);
    const [accountActionLoading, setAccountActionLoading] = useState(null);

    // =====================================================
    // ADMIN PASSWORD RESET
    // =====================================================

    const [resetUserId, setResetUserId] = useState(null);
    const [resetUsername, setResetUsername] = useState("");
    const [resetNewPassword, setResetNewPassword] = useState("");
    const [resetConfirmNewPassword, setResetConfirmNewPassword] = useState("");
    const [resetPasswordLoading, setResetPasswordLoading] = useState(false);

    // =====================================================
    // ROLE EDIT
    // =====================================================

   const [roleEditAccount, setRoleEditAccount] = useState(null);
   const [selectedRole, setSelectedRole] = useState("");
   const [roleUpdateLoading, setRoleUpdateLoading] = useState(false);


    // =====================================================
    // STUDENT FORM
    // =====================================================

    const [studentRegistrationNumber, setStudentRegistrationNumber] =
        useState("");

    const [studentName, setStudentName] =
        useState("");

    const [studentEmail, setStudentEmail] =
        useState("");

    const [studentPassword, setStudentPassword] =
        useState("");

    const [studentYear, setStudentYear] =
        useState("");

    const [studentSemester, setStudentSemester] =
        useState("");

    const [studentDegreeType, setStudentDegreeType] =
    useState("");

    const [studentMajor, setStudentMajor] =
        useState("");

    const [studentMinor, setStudentMinor] =
        useState("");


    // =====================================================
    // LECTURER FORM
    // =====================================================

    const [lecturerCode, setLecturerCode] =
        useState("");

    const [lecturerName, setLecturerName] =
        useState("");

    const [lecturerEmail, setLecturerEmail] =
        useState("");

    const [lecturerPassword, setLecturerPassword] =
        useState("");

    const [lecturerDepartment, setLecturerDepartment] =
        useState("");


    // =====================================================
    // SUBJECT FORM
    // =====================================================

    const [subjectCode, setSubjectCode] =
        useState("");

    const [subjectName, setSubjectName] =
        useState("");

    const [subjectDepartment, setSubjectDepartment] =
        useState("");

    const [subjectSpecialization, setSubjectSpecialization] =
        useState("");

    const [subjectYear, setSubjectYear] =
        useState("");

    const [subjectSemester, setSubjectSemester] =
        useState("");

    const [subjectCredits, setSubjectCredits] =
        useState("");

    const [subjectCompulsory, setSubjectCompulsory] =
        useState(false);


    // =====================================================
    // REGISTRATION EDIT FORM
    // =====================================================

    const [registrationStudent, setRegistrationStudent] = useState("");
    const [registrationSubject, setRegistrationSubject] = useState("");
    const [registrationYear, setRegistrationYear] = useState("");
    const [registrationSemester, setRegistrationSemester] = useState("");


    // =====================================================
    // LOGIN
    // =====================================================

    const token =
        localStorage.getItem("token");

    const username =
        localStorage.getItem("username") ||
        "Admin";


    // =====================================================
    // AXIOS CONFIG
    // =====================================================

    const getConfig = () => {

        return token
            ? {
                headers: {
                    Authorization:
                        `Bearer ${token}`
                }
            }
            : {};
    };


    // =====================================================
    // INITIAL LOAD
    // =====================================================

    useEffect(() => {

        loadDashboard();

    }, []);


    // =====================================================
    // LOAD DASHBOARD
    // =====================================================

    const loadDashboard = async () => {

        setLoading(true);
        setError("");

        try {

            const responses =
                await Promise.all([

                    axios.get(
                        "http://localhost:8080/api/students",
                        getConfig()
                    ),

                    axios.get(
                        "http://localhost:8080/api/lecturers",
                        getConfig()
                    ),

                    axios.get(
                        "http://localhost:8080/api/subjects",
                        getConfig()
                    ),

                    axios.get(
                        "http://localhost:8080/api/lecturer-allocations",
                        getConfig()
                    ),

                    axios.get(
                        "http://localhost:8080/api/student-subject-registrations",
                        getConfig()
                    ),

                    axios.get(
                        "http://localhost:8080/api/departments",
                        getConfig()
                    ),

                    axios.get(
                        "http://localhost:8080/api/specializations",
                        getConfig()
                    ),

                    axios.get(
                        "http://localhost:8080/api/timetable-change-requests",
                        getConfig()
                    )

                ]);


            setStudents(
                Array.isArray(responses[0].data)
                    ? responses[0].data
                    : []
            );

            setLecturers(
                Array.isArray(responses[1].data)
                    ? responses[1].data
                    : []
            );

            setSubjects(
                Array.isArray(responses[2].data)
                    ? responses[2].data
                    : []
            );

            setAllocations(
                Array.isArray(responses[3].data)
                    ? responses[3].data
                    : []
            );

            setRegistrations(
                Array.isArray(responses[4].data)
                    ? responses[4].data
                    : []
            );

            setDepartments(
                Array.isArray(responses[5].data)
                    ? responses[5].data
                    : []
            );

            setSpecializations(
                Array.isArray(responses[6].data)
                    ? responses[6].data
                    : []
            );

            setChangeRequests(
                Array.isArray(responses[7].data)
                    ? responses[7].data
                    : []
            );

        } catch (err) {

            console.error(
                "Admin dashboard loading error:",
                err
            );

            console.error(
                "Server response:",
                err.response?.data
            );

            setError(
                err.response?.data?.message ||
                "Unable to load admin dashboard."
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // LOAD CHANGE REQUESTS
    // =====================================================

    const loadChangeRequests = async () => {

    console.log("Refresh Requests button clicked");
    setRequestLoading(true);
    setError("");

    try {
        const response = await axios.get(
            "http://localhost:8080/api/timetable-change-requests",
            getConfig()
        );

        console.log("Change requests refreshed:", response.data);

        setChangeRequests(
            Array.isArray(response.data) ? response.data : []
        );

    } catch (err) {
        console.error("Error refreshing change requests:", err);

        setError(
            err.response?.data?.message ||
            err.response?.data ||
            "Unable to refresh timetable change requests."
        );

    } finally {
        // IMPORTANT: Reset requestLoading, not processingRequest
        setRequestLoading(false);
    }
};


    // =====================================================
    // ADMIN NOTIFICATION HELPERS
    // =====================================================

    const pendingChangeRequests = changeRequests.filter(
        (request) =>
            String(request?.status || "").toUpperCase() === "PENDING"
    );

    const handleAdminNotifications = async () => {
        const nextState = !showNotifications;
        setShowNotifications(nextState);

        if (nextState) {
            await loadChangeRequests();
        }
    };

    const openPendingChangeRequests = async () => {
        setShowNotifications(false);
        setActiveSection("requests");
        await loadChangeRequests();
    };


    // =====================================================
    // APPROVE / REJECT CHANGE REQUEST
    // =====================================================

    const openRequestResponseDialog = (id, action) => {
    setError("");
    setSuccess("");

    setRequestResponseDialog({
        open: true,
        id,
        action,
        responseText: action === "approve" ? "Request approved." : ""
    });
};

const closeRequestResponseDialog = () => {
    setRequestResponseDialog({
        open: false,
        id: null,
        action: "",
        responseText: ""
    });
};

const processChangeRequest = async () => {
    const { id, action, responseText } = requestResponseDialog;

    if (!id || !action) {
        setError("Invalid change request.");
        return;
    }

    if (action === "reject" && !responseText.trim()) {
        setError("Please enter a reason for rejecting this request.");
        return;
    }

    const adminResponse =
        responseText.trim() ||
        (action === "approve" ? "Request approved." : "Request rejected.");

    setError("");
    setSuccess("");
    setProcessingRequest(true);

    try {
        await axios.put(
            `http://localhost:8080/api/timetable-change-requests/${id}/${action}`,
            null,
            {
                ...getConfig(),
                params: { adminResponse }
            }
        );

        closeRequestResponseDialog();

        setSuccess(
            action === "approve"
                ? "Change request approved successfully."
                : "Change request rejected successfully."
        );

        await loadChangeRequests();
        await loadDashboard();

    } catch (err) {
        console.error("Error processing change request:", err);

        setError(
            err.response?.data?.message ||
            err.response?.data ||
            `Unable to ${action} change request.`
        );
    } finally {
        setProcessingRequest(false);
    }
};


    
const deleteChangeRequest = (id) => {

    showConfirmDialog(
        "Are you sure you want to delete this change request?",
        async () => {

            setError("");
            setSuccess("");

            try {

                await axios.delete(
                    `http://localhost:8080/api/timetable-change-requests/${id}`,
                    getConfig()
                );

                setSuccess(
                    "Change request deleted successfully."
                );

                await loadChangeRequests();

            } catch (err) {

                console.error("Change request delete error:", err);

                setError(
                    err.response?.data?.message ||
                    err.response?.data ||
                    "Unable to delete change request."
                );
            }
        }
    );
};



    // =====================================================
    // LOAD USER ACCOUNTS
    // =====================================================

    const loadUserAccounts = async () => {

        setAccountLoading(true);
        setError("");

        try {

            const response = await axios.get(
                "http://localhost:8080/api/user-accounts",
                getConfig()
            );

            console.log("User accounts:", response.data);

            setUserAccounts(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error("User account loading error:", err);

            setError(
                err.response?.data?.message ||
                err.response?.data ||
                "Unable to load user accounts."
            );

        } finally {

            setAccountLoading(false);
        }
    };


    // =====================================================
    // ENABLE / DISABLE USER ACCOUNT
    // =====================================================

    // =====================================================
    // OPEN ADMIN PASSWORD RESET
    // =====================================================

    const openPasswordReset = (account) => {

        setResetUserId(account?.userId || null);
        setResetUsername(account?.username || "");
        setResetNewPassword("");
        setResetConfirmNewPassword("");

        setError("");
        setSuccess("");
    };


    // =====================================================
    // CANCEL ADMIN PASSWORD RESET
    // =====================================================

    const cancelPasswordReset = () => {

        setResetUserId(null);
        setResetUsername("");
        setResetNewPassword("");
        setResetConfirmNewPassword("");
    };


    // =====================================================
    // ADMIN RESET PASSWORD
    // =====================================================

    
const resetAccountPassword = async () => {

    if (!resetUserId) {
        setError("Please select a user account.");
        return;
    }

    if (!resetNewPassword.trim()) {
        setError("New password is required.");
        return;
    }

    if (resetNewPassword.length < 6) {
        setError("New password must be at least 6 characters.");
        return;
    }

    if (!resetConfirmNewPassword.trim()) {
        setError("Please confirm the new password.");
        return;
    }

    if (resetNewPassword !== resetConfirmNewPassword) {
        setError("New password and confirmation password do not match.");
        return;
    }

    showConfirmDialog(
        `Are you sure you want to reset the password for the account "${resetUsername}"?`,
        async () => {

            setError("");
            setSuccess("");
            setResetPasswordLoading(true);

            try {

                await axios.put(
                    `http://localhost:8080/api/user-accounts/${resetUserId}/reset-password`,
                    {
                        newPassword: resetNewPassword,
                        confirmNewPassword: resetConfirmNewPassword
                    },
                    getConfig()
                );

                setSuccess("User account password reset successfully.");

                cancelPasswordReset();

            } catch (err) {

                console.error("Admin password reset error:", err);

                setError(
                    err.response?.data?.message ||
                    err.response?.data ||
                    "Unable to reset user account password."
                );

            } finally {

                setResetPasswordLoading(false);
            }
        }
    );
};



    
const changeAccountStatus = (account, action) => {

    const userId = account?.userId;

    if (!userId) {
        setError("User account ID could not be found.");
        return;
    }

    const actionText = action === "enable" ? "enable" : "disable";

    showConfirmDialog(
        `Are you sure you want to ${actionText} the account "${account.username}"?`,
        async () => {

            setError("");
            setSuccess("");
            setAccountActionLoading(`${action}-${userId}`);

            try {

                const response = await axios.put(
                    `http://localhost:8080/api/user-accounts/${userId}/${action}`,
                    {},
                    getConfig()
                );

                const updatedAccount = response.data;

                setUserAccounts((previousAccounts) =>
                    previousAccounts.map((item) =>
                        item.userId === userId
                            ? updatedAccount
                            : item
                    )
                );

                setSuccess(
                    action === "enable"
                        ? "User account enabled successfully."
                        : "User account disabled successfully."
                );

            } catch (err) {

                console.error("User account status update error:", err);

                setError(
                    err.response?.data?.message ||
                    err.response?.data ||
                    `Unable to ${actionText} user account.`
                );

            } finally {

                setAccountActionLoading(null);
            }
        }
    );
};

    // =====================================================
    // OPEN ROLE EDIT
    // =====================================================

const openRoleEdit = (account) => {

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
// CLOSE ROLE EDIT
// =====================================================

const closeRoleEdit = () => {

    if (roleUpdateLoading) {
        return;
    }

    setRoleEditAccount(null);
    setSelectedRole("");
};


// =====================================================
// UPDATE USER ROLE
// =====================================================

// =====================================================
// UPDATE USER ROLE
// =====================================================

const updateUserRole = () => {

    if (!roleEditAccount?.userId) {
        setError("User account ID could not be found.");
        return;
    }

    if (
        selectedRole !== "LECTURER" &&
        selectedRole !== "HOD"
    ) {
        setError("Please select a valid role.");
        return;
    }

    const currentRole = roleEditAccount.role;
    const userId = roleEditAccount.userId;
    const username = roleEditAccount.username;
    const newRole = selectedRole;

    if (currentRole === newRole) {
        setError("The selected role is already assigned to this account.");
        return;
    }

    showConfirmDialog(
        `Are you sure you want to change the role of "${username}" from ${currentRole} to ${newRole}?`,

        async () => {
            setError("");
            setSuccess("");
            setRoleUpdateLoading(true);

            try {
                const response = await axios.put(
                    `http://localhost:8080/api/user-accounts/${userId}/role`,
                    {},
                    {
                        ...getConfig(),
                        params: {
                            role: newRole
                        }
                    }
                );

                const updatedAccount = response.data;

                // IMPORTANT: AdminDashboard uses setUserAccounts
                setUserAccounts(previousAccounts =>
    previousAccounts.map(account =>
        account.userId === userId
            ? updatedAccount
            : account
    )
);
                setSuccess(
                    `Role changed successfully to ${newRole}.`
                );

                setRoleEditAccount(null);
                setSelectedRole("");

            } catch (err) {
                console.error("User role update error:", err);
                console.error("Server response:", err.response?.data);

                const serverMessage = err.response?.data;

                setError(
                    typeof serverMessage === "string"
                        ? serverMessage
                        : serverMessage?.message ||
                          serverMessage?.error ||
                          "Unable to update user role."
                );

            } finally {
                setRoleUpdateLoading(false);
            }
        }
    );
};


    // =====================================================
    // CLEAR FORM
    // =====================================================

    const clearForm = () => {

        setEditingId(null);
        setFormType("");

        setStudentRegistrationNumber("");
        setStudentName("");
        setStudentEmail("");
        setStudentPassword("");
        setStudentYear("");
        setStudentSemester("");
        setStudentDegreeType("");
        setStudentMajor("");
        setStudentMinor("");

        setLecturerCode("");
        setLecturerName("");
        setLecturerEmail("");
        setLecturerPassword("");
        setLecturerDepartment("");

        setSubjectCode("");
        setSubjectName("");
        setSubjectDepartment("");
        setSubjectSpecialization("");
        setSubjectYear("");
        setSubjectSemester("");
        setSubjectCredits("");
        setSubjectCompulsory(false);

        setRegistrationStudent("");
        setRegistrationSubject("");
        setRegistrationYear("");
        setRegistrationSemester("");

        setResetUserId(null);
        setResetUsername("");
        setResetNewPassword("");
        setResetConfirmNewPassword("");
    };


    // =====================================================
    // CSV IMPORT HELPERS
    // =====================================================

    const parseCSV = (text) => {

        const rows = [];
        let row = [];
        let value = "";
        let insideQuotes = false;

        for (let i = 0; i < text.length; i++) {

            const character = text[i];
            const nextCharacter = text[i + 1];

            if (character === '"') {

                if (insideQuotes && nextCharacter === '"') {
                    value += '"';
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (character === "," && !insideQuotes) {

                row.push(value.trim());
                value = "";

            } else if (
                (character === "\n" || character === "\r") &&
                !insideQuotes
            ) {

                if (character === "\r" && nextCharacter === "\n") {
                    i++;
                }

                row.push(value.trim());
                value = "";

                if (row.some((item) => item !== "")) {
                    rows.push(row);
                }

                row = [];

            } else {
                value += character;
            }
        }

        if (value !== "" || row.length > 0) {
            row.push(value.trim());

            if (row.some((item) => item !== "")) {
                rows.push(row);
            }
        }

        if (rows.length < 2) {
            return [];
        }

        const headers = rows[0].map((header) =>
            String(header || "")
                .replace(/^\uFEFF/, "")
                .trim()
                .toLowerCase()
        );

        return rows.slice(1).map((values) => {

            const item = {};

            headers.forEach((header, index) => {
                item[header] = values[index] ?? "";
            });

            return item;
        });
    };


    const getCSVValue = (row, ...names) => {

        for (const name of names) {

            const key = name.toLowerCase();

            if (
                Object.prototype.hasOwnProperty.call(row, key) &&
                String(row[key] ?? "").trim() !== ""
            ) {
                return String(row[key]).trim();
            }
        }

        return "";
    };


    const getCSVErrorMessage = (err) => {

        return (
            err.response?.data?.message ||
            err.response?.data ||
            err.message ||
            "Unknown error"
        );
    };


    const importStudentsCSV = async (event) => {

        const file = event.target.files?.[0];

        event.target.value = "";

        if (!file) {
            return;
        }

        if (!file.name.toLowerCase().endsWith(".csv")) {
            setError("Please select a CSV file for students.");
            return;
        }

        try {

            setError("");
            setSuccess("");
            setFormLoading(true);

            const text = await file.text();
            const rows = parseCSV(text);

            if (rows.length === 0) {
                throw new Error("The student CSV file contains no data rows.");
            }

            let imported = 0;
            const failed = [];

            for (let index = 0; index < rows.length; index++) {

                const row = rows[index];
                const rowNumber = index + 2;

                const data = {
                    registrationNumber: getCSVValue(
                        row,
                        "registrationnumber",
                        "registration_number"
                    ),
                    studentName: getCSVValue(
                        row,
                        "studentname",
                        "student_name",
                        "name"
                    ),
                    email: getCSVValue(row, "email"),
                    password: getCSVValue(row, "password"),
                    yearLevel: Number(
                        getCSVValue(row, "yearlevel", "year_level", "year")
                    ),
                    semester: Number(
                        getCSVValue(row, "semester")
                    ),
                    majorSpecializationId: getCSVValue(
                        row,
                        "majorspecializationid",
                        "major_specialization_id",
                        "majorid"
                    )
                        ? Number(
                            getCSVValue(
                                row,
                                "majorspecializationid",
                                "major_specialization_id",
                                "majorid"
                            )
                        )
                        : null,
                    minorSpecializationId: getCSVValue(
                        row,
                        "minorspecializationid",
                        "minor_specialization_id",
                        "minorid"
                    )
                        ? Number(
                            getCSVValue(
                                row,
                                "minorspecializationid",
                                "minor_specialization_id",
                                "minorid"
                            )
                        )
                        : null
                };

                if (
                    !data.registrationNumber ||
                    !data.studentName ||
                    !data.email ||
                    !data.password ||
                    !data.yearLevel ||
                    !data.semester
                ) {
                    failed.push(
                        `Row ${rowNumber}: required student fields are missing.`
                    );
                    continue;
                }

                try {

                    await axios.post(
                        "http://localhost:8080/api/students/register",
                        data,
                        getConfig()
                    );

                    imported++;

                } catch (err) {

                    failed.push(
                        `Row ${rowNumber}: ${getCSVErrorMessage(err)}`
                    );
                }
            }

            await loadDashboard();

            if (failed.length === 0) {
                setSuccess(
                    `${imported} student(s) imported successfully.`
                );
            } else {
                setError(
                    `Student import completed with ${failed.length} failed row(s): ${failed.slice(0, 5).join(" | ")}`
                );

                setSuccess(
                    `${imported} student(s) imported successfully.`
                );
            }

        } catch (err) {

            console.error("Student CSV import error:", err);
            setError(getCSVErrorMessage(err));

        } finally {
            setFormLoading(false);
        }
    };


    const importLecturersCSV = async (event) => {

        const file = event.target.files?.[0];

        event.target.value = "";

        if (!file) {
            return;
        }

        if (!file.name.toLowerCase().endsWith(".csv")) {
            setError("Please select a CSV file for lecturers.");
            return;
        }

        try {

            setError("");
            setSuccess("");
            setFormLoading(true);

            const text = await file.text();
            const rows = parseCSV(text);

            if (rows.length === 0) {
                throw new Error("The lecturer CSV file contains no data rows.");
            }

            let imported = 0;
            const failed = [];

            for (let index = 0; index < rows.length; index++) {

                const row = rows[index];
                const rowNumber = index + 2;

                const data = {
                    lecturerCode: getCSVValue(
                        row,
                        "lecturercode",
                        "lecturer_code",
                        "code"
                    ),
                    lecturerName: getCSVValue(
                        row,
                        "lecturername",
                        "lecturer_name",
                        "name"
                    ),
                    email: getCSVValue(row, "email"),
                    department: {
                        departmentId: Number(
                            getCSVValue(
                                row,
                                "departmentid",
                                "department_id"
                            )
                        )
                    },
                    user: {
                        password: getCSVValue(row, "password")
                    }
                };

                if (
                    !data.lecturerCode ||
                    !data.lecturerName ||
                    !data.email ||
                    !data.department.departmentId ||
                    !data.user.password
                ) {
                    failed.push(
                        `Row ${rowNumber}: required lecturer fields are missing.`
                    );
                    continue;
                }

                try {

                    await axios.post(
                        "http://localhost:8080/api/lecturers",
                        data,
                        getConfig()
                    );

                    imported++;

                } catch (err) {

                    failed.push(
                        `Row ${rowNumber}: ${getCSVErrorMessage(err)}`
                    );
                }
            }

            await loadDashboard();

            if (failed.length === 0) {
                setSuccess(
                    `${imported} lecturer(s) imported successfully.`
                );
            } else {
                setError(
                    `Lecturer import completed with ${failed.length} failed row(s): ${failed.slice(0, 5).join(" | ")}`
                );

                setSuccess(
                    `${imported} lecturer(s) imported successfully.`
                );
            }

        } catch (err) {

            console.error("Lecturer CSV import error:", err);
            setError(getCSVErrorMessage(err));

        } finally {
            setFormLoading(false);
        }
    };


    const importSubjectsCSV = async (event) => {

        const file = event.target.files?.[0];

        event.target.value = "";

        if (!file) {
            return;
        }

        if (!file.name.toLowerCase().endsWith(".csv")) {
            setError("Please select a CSV file for subjects.");
            return;
        }

        try {

            setError("");
            setSuccess("");
            setFormLoading(true);

            const text = await file.text();
            const rows = parseCSV(text);

            if (rows.length === 0) {
                throw new Error("The subject CSV file contains no data rows.");
            }

            let imported = 0;
            const failed = [];

            for (let index = 0; index < rows.length; index++) {

                const row = rows[index];
                const rowNumber = index + 2;

                const data = {
                    subjectCode: getCSVValue(
                        row,
                        "subjectcode",
                        "subject_code",
                        "code"
                    ),
                    subjectName: getCSVValue(
                        row,
                        "subjectname",
                        "subject_name",
                        "name"
                    ),
                    department: {
                        departmentId: Number(
                            getCSVValue(
                                row,
                                "departmentid",
                                "department_id"
                            )
                        )
                    },
                    specialization: {
                        specializationId: Number(
                            getCSVValue(
                                row,
                                "specializationid",
                                "specialization_id"
                            )
                        )
                    },
                    academicYear: Number(
                        getCSVValue(
                            row,
                            "academicyear",
                            "academic_year",
                            "year"
                        )
                    ),
                    semester: Number(
                        getCSVValue(row, "semester")
                    ),
                    credits: Number(
                        getCSVValue(row, "credits")
                    ),
                    compulsory:
                        getCSVValue(
                            row,
                            "compulsory"
                        ).toLowerCase() === "true"
                };

                if (
                    !data.subjectCode ||
                    !data.subjectName ||
                    !data.department.departmentId ||
                    !data.specialization.specializationId ||
                    !data.academicYear ||
                    !data.semester ||
                    !data.credits
                ) {
                    failed.push(
                        `Row ${rowNumber}: required subject fields are missing.`
                    );
                    continue;
                }

                if (
                    data.academicYear === 4 &&
                    data.semester !== 1
                ) {
                    failed.push(
                        `Row ${rowNumber}: Year 4 subjects can only be Semester 1.`
                    );
                    continue;
                }

                try {

                    await axios.post(
                        "http://localhost:8080/api/subjects",
                        data,
                        getConfig()
                    );

                    imported++;

                } catch (err) {

                    failed.push(
                        `Row ${rowNumber}: ${getCSVErrorMessage(err)}`
                    );
                }
            }

            await loadDashboard();

            if (failed.length === 0) {
                setSuccess(
                    `${imported} subject(s) imported successfully.`
                );
            } else {
                setError(
                    `Subject import completed with ${failed.length} failed row(s): ${failed.slice(0, 5).join(" | ")}`
                );

                setSuccess(
                    `${imported} subject(s) imported successfully.`
                );
            }

        } catch (err) {

            console.error("Subject CSV import error:", err);
            setError(getCSVErrorMessage(err));

        } finally {
            setFormLoading(false);
        }
    };


    // =====================================================
    // OPEN ADD STUDENT
    // =====================================================

    const openAddStudent = () => {
    setEditingId(null);
    clearForm();
    setFormType("student");
    setError("");
    setSuccess("");
    setShowForm(true);
};


    // =====================================================
    // OPEN EDIT STUDENT
    // =====================================================

const openEditStudent = (student) => {

    clearForm();

    setFormType("student");

    setEditingId(
        student.studentId
    );

    setStudentRegistrationNumber(
        student.registrationNumber || ""
    );

    setStudentName(
        student.studentName || ""
    );

    setStudentEmail(
        student.email || ""
    );

    setStudentYear(
        student.yearLevel
            ? String(student.yearLevel)
            : ""
    );

    setStudentSemester(
        student.semester
            ? String(student.semester)
            : ""
    );

    setStudentDegreeType(
        student.degreeType || ""
    );

    setStudentMajor(
        student.majorSpecializationId
            ? String(student.majorSpecializationId)
            : ""
    );

    setStudentMinor(
        student.minorSpecializationId
            ? String(student.minorSpecializationId)
            : ""
    );

    setError("");
    setSuccess("");

    setShowForm(true);
};


    // =====================================================
    // OPEN ADD LECTURER
    // =====================================================

    const openAddLecturer = () => {

        clearForm();

        setFormType("lecturer");

        setError("");
        setSuccess("");

        setShowForm(true);
    };


    // =====================================================
    // OPEN EDIT LECTURER
    // =====================================================

    const openEditLecturer = (lecturer) => {

        clearForm();

        setFormType("lecturer");

        setEditingId(
            lecturer.lecturerId
        );

        setLecturerCode(
            lecturer.lecturerCode || ""
        );

        setLecturerName(
            lecturer.lecturerName || ""
        );

        setLecturerEmail(
            lecturer.email || ""
        );

        setLecturerDepartment(
            lecturer.department?.departmentId
                ? String(
                    lecturer.department.departmentId
                )
                : ""
        );

        setError("");
        setSuccess("");

        setShowForm(true);
    };


    // =====================================================
    // OPEN ADD SUBJECT
    // =====================================================

    const openAddSubject = () => {

        clearForm();

        setFormType("subject");

        setError("");
        setSuccess("");

        setShowForm(true);
    };


    // =====================================================
    // OPEN EDIT SUBJECT
    // =====================================================

    const openEditSubject = (subject) => {

        clearForm();

        setFormType("subject");

        setEditingId(
            subject.subjectId
        );

        setSubjectCode(
            subject.subjectCode || ""
        );

        setSubjectName(
            subject.subjectName || ""
        );

        setSubjectDepartment(
            subject.department?.departmentId
                ? String(
                    subject.department.departmentId
                )
                : ""
        );

        setSubjectSpecialization(
            subject.specialization?.specializationId
                ? String(
                    subject.specialization.specializationId
                )
                : ""
        );

        setSubjectYear(
            subject.academicYear
                ? String(subject.academicYear)
                : ""
        );

        setSubjectSemester(
            subject.semester
                ? String(subject.semester)
                : ""
        );

        setSubjectCredits(
            subject.credits
                ? String(subject.credits)
                : ""
        );

        setSubjectCompulsory(
            Boolean(subject.compulsory)
        );

        setError("");
        setSuccess("");

        setShowForm(true);
    };


    // =====================================================
    // CLOSE FORM
    // =====================================================

    const closeForm = () => {

        setShowForm(false);

        clearForm();

        setError("");
    };


    // =====================================================
    // SUBMIT STUDENT
    // =====================================================

    const submitStudent = async () => {

    console.log("========== STUDENT SUBMIT START ==========");
    console.log("editingId:", editingId);
    console.log("formType:", formType);
    console.log("studentYear:", studentYear);
    console.log("studentSemester:", studentSemester);
    console.log("studentDegreeType:", studentDegreeType);
    console.log("studentMajor:", studentMajor);
    console.log("studentMinor:", studentMinor);

    setError("");
    setSuccess("");

    if (!studentRegistrationNumber.trim()) {
        setError("Registration number is required.");
        return;
    }

    if (!studentName.trim()) {
        setError("Student name is required.");
        return;
    }

    if (!studentEmail.trim()) {
        setError("Email is required.");
        return;
    }

    if (!studentYear) {
        setError("Please select a year.");
        return;
    }

    if (!studentSemester) {
        setError("Please select a semester.");
        return;
    }

    /*
     * Password is required only when creating
     * a new student.
     */
    if (!editingId && !studentPassword.trim()) {
        setError(
            "Password is required when adding a student."
        );
        return;
    }

    const year = Number(studentYear);

    /*
     * YEAR 1
     * No degree type, major or minor.
     */
    if (year === 1) {

        if (studentDegreeType) {
            setError(
                "Year 1 students do not require a degree type."
            );
            return;
        }

        if (studentMajor || studentMinor) {
            setError(
                "Year 1 students cannot have major or minor specializations."
            );
            return;
        }
    }

    /*
     * YEAR 2 / YEAR 3
     * Degree type, major and minor are required.
     */
    if (year === 2 || year === 3) {

        if (!studentDegreeType) {
            setError(
                "Please select a degree type."
            );
            return;
        }

        if (!studentMajor) {
            setError(
                `Year ${year} students must have a major specialization.`
            );
            return;
        }

        if (!studentMinor) {
            setError(
                `Year ${year} students must have a minor specialization.`
            );
            return;
        }

        if (studentMajor === studentMinor) {
            setError(
                "Major and minor specializations must be different."
            );
            return;
        }
    }

    /*
     * YEAR 4
     * Only Special Degree.
     */
    if (year === 4) {

        if (studentDegreeType !== "SPECIAL") {
            setError(
                "Year 4 students can only follow the Special Degree."
            );
            return;
        }

        if (!studentMajor) {
            setError(
                "Year 4 students must have a major specialization."
            );
            return;
        }

        if (studentMinor) {
            setError(
                "Year 4 students cannot have a minor specialization."
            );
            return;
        }

        if (Number(studentSemester) !== 1) {
            setError(
                "Year 4 students can only be registered for Semester 1."
            );
            return;
        }
    }

    /*
     * Build request data.
     */
    const data = {
        registrationNumber:
            studentRegistrationNumber.trim(),

        studentName:
            studentName.trim(),

        email:
            studentEmail.trim(),

        yearLevel:
            year,

        semester:
            Number(studentSemester),

        degreeType:
            year === 1
                ? null
                : studentDegreeType,

        majorSpecializationId:
            studentMajor
                ? Number(studentMajor)
                : null,

        minorSpecializationId:
            studentMinor
                ? Number(studentMinor)
                : null
    };

    console.log("Student update data:", data);

    try {

        setFormLoading(true);

        /*
         * ADD STUDENT
         */
        if (!editingId) {

            const createData = {
                ...data,
                password: studentPassword
            };

            console.log(
                "POST /api/students/register",
                createData
            );

            await axios.post(
                "http://localhost:8080/api/students/register",
                createData,
                getConfig()
            );

            setSuccess(
                "Student added successfully."
            );

        }

        /*
         * UPDATE STUDENT
         */
        else {

            console.log(
                "PUT URL:",
                `http://localhost:8080/api/students/${editingId}`
            );

            console.log(
                "PUT DATA:",
                data
            );

            const response = await axios.put(
                `http://localhost:8080/api/students/${editingId}`,
                data,
                getConfig()
            );

            console.log(
                "UPDATE RESPONSE:",
                response.data
            );

            setSuccess(
                "Student updated successfully."
            );
        }

        console.log(
            "Student save completed successfully."
        );

        await loadDashboard();

        setShowForm(false);

        clearForm();

    } catch (err) {

        console.error(
            "========== STUDENT SAVE ERROR =========="
        );

        console.error(
            "Full error:",
            err
        );

        console.error(
            "Response:",
            err.response
        );

        console.error(
            "Response data:",
            err.response?.data
        );

        console.error(
            "Response status:",
            err.response?.status
        );

        setError(
            err.response?.data?.message ||
            err.response?.data ||
            "Unable to save student."
        );

    } finally {

        setFormLoading(false);

        console.log(
            "========== STUDENT SUBMIT END =========="
        );
    }
};
    


    // =====================================================
    // DELETE STUDENT
    // =====================================================

    
const deleteStudent = (id) => {

    showConfirmDialog(
        "Are you sure you want to delete this student?",
        async () => {

            setError("");
            setSuccess("");

            try {

                await axios.delete(
                    `http://localhost:8080/api/students/${id}`,
                    getConfig()
                );

                setSuccess(
                    "Student deleted successfully."
                );

                await loadDashboard();

            } catch (err) {

                console.error("Student delete error:", err);

                setError(
                    err.response?.data?.message ||
                    err.response?.data ||
                    "Unable to delete student."
                );
            }
        }
    );
};



    // =====================================================
    // SUBMIT LECTURER
    // =====================================================

    const submitLecturer = async () => {

        if (!lecturerCode.trim()) {

            setError(
                "Lecturer code is required."
            );

            return;
        }

        if (!lecturerName.trim()) {

            setError(
                "Lecturer name is required."
            );

            return;
        }

        if (!lecturerEmail.trim()) {

            setError(
                "Lecturer email is required."
            );

            return;
        }

        if (!lecturerDepartment) {

            setError(
                "Please select a department."
            );

            return;
        }

        if (!editingId && !lecturerPassword.trim()) {

            setError(
                "Initial password is required when adding a lecturer."
            );

            return;
        }

        if (!editingId && lecturerPassword.trim().length < 6) {

            setError(
                "Initial password must be at least 6 characters."
            );

            return;
        }


        const data = {

            lecturerCode:
                lecturerCode.trim(),

            lecturerName:
                lecturerName.trim(),

            email:
                lecturerEmail.trim(),

            department: {

                departmentId:
                    Number(lecturerDepartment)
            }
        };

        if (!editingId || lecturerPassword.trim()) {
            data.user = {
                password: lecturerPassword
            };
        }


        try {

            setFormLoading(true);

            if (!editingId) {

                await axios.post(
                    "http://localhost:8080/api/lecturers",
                    data,
                    getConfig()
                );

                setSuccess(
                    "Lecturer added successfully."
                );

            } else {

                await axios.put(
                    `http://localhost:8080/api/lecturers/${editingId}`,
                    data,
                    getConfig()
                );

                setSuccess(
                    "Lecturer updated successfully."
                );
            }


            await loadDashboard();

            setShowForm(false);

            clearForm();

        } catch (err) {

            console.error(
                "Lecturer save error:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data ||
                "Unable to save lecturer."
            );

        } finally {

            setFormLoading(false);
        }
    };


    // =====================================================
    // DELETE LECTURER
    // =====================================================

    
const deleteLecturer = (id) => {

    showConfirmDialog(
        "Are you sure you want to delete this lecturer?",
        async () => {

            setError("");
            setSuccess("");

            try {

                await axios.delete(
                    `http://localhost:8080/api/lecturers/${id}`,
                    getConfig()
                );

                setSuccess(
                    "Lecturer deleted successfully."
                );

                await loadDashboard();

            } catch (err) {

                console.error("Lecturer delete error:", err);

                setError(
                    err.response?.data?.message ||
                    err.response?.data ||
                    "Unable to delete lecturer."
                );
            }
        }
    );
};



    // =====================================================
    // SUBMIT SUBJECT
    // =====================================================

    const submitSubject = async () => {

        if (!subjectCode.trim()) {

            setError(
                "Subject code is required."
            );

            return;
        }

        if (!subjectName.trim()) {

            setError(
                "Subject name is required."
            );

            return;
        }

        if (!subjectDepartment) {

            setError(
                "Please select a department."
            );

            return;
        }

        if (!subjectSpecialization) {

            setError(
                "Please select a specialization."
            );

            return;
        }

        if (!subjectYear) {

            setError(
                "Please select an academic year."
            );

            return;
        }

        if (!subjectSemester) {

            setError(
                "Please select a semester."
            );

            return;
        }

        if (!subjectCredits) {

            setError(
                "Please enter subject credits."
            );

            return;
        }


        if (
            Number(subjectYear) === 4 &&
            Number(subjectSemester) !== 1
        ) {

            setError(
                "Year 4 subjects can only be Semester 1."
            );

            return;
        }


        const data = {

            subjectCode:
                subjectCode.trim(),

            subjectName:
                subjectName.trim(),

            department: {

                departmentId:
                    Number(subjectDepartment)
            },

            specialization: {

                specializationId:
                    Number(subjectSpecialization)
            },

            academicYear:
                Number(subjectYear),

            semester:
                Number(subjectSemester),

            credits:
                Number(subjectCredits),

            compulsory:
                subjectCompulsory
        };


        try {

            setFormLoading(true);

            if (!editingId) {

                await axios.post(
                    "http://localhost:8080/api/subjects",
                    data,
                    getConfig()
                );

                setSuccess(
                    "Subject added successfully."
                );

            } else {

                await axios.put(
                    `http://localhost:8080/api/subjects/${editingId}`,
                    data,
                    getConfig()
                );

                setSuccess(
                    "Subject updated successfully."
                );
            }


            await loadDashboard();

            setShowForm(false);

            clearForm();

        } catch (err) {

            console.error(
                "Subject save error:",
                err
            );

            setError(
                err.response?.data?.message ||
                err.response?.data ||
                "Unable to save subject."
            );

        } finally {

            setFormLoading(false);
        }
    };


   // =====================================================
// DELETE SUBJECT
// =====================================================

const deleteSubject = (id) => {
    showConfirmDialog(
        "Are you sure you want to remove this subject? If students have registered or the subject has other linked academic records, it will be deactivated instead of permanently deleted.",
        async () => {
            setError("");
            setSuccess("");

            try {
                const response = await axios.delete(
                    `http://localhost:8080/api/subjects/${id}`,
                    getConfig()
                );

                const resultMessage =
                    typeof response.data === "string"
                        ? response.data
                        : response.data?.message;

                setSuccess(
                    resultMessage ||
                    "Subject removal completed successfully."
                );

                await loadDashboard();

            } catch (err) {
                console.error("Subject removal error:", err);

                const errorMessage =
                    err.response?.data?.message ||
                    (typeof err.response?.data === "string"
                        ? err.response.data
                        : "Unable to remove subject.");

                setError(errorMessage);
            }
        }
    );
};



    // =====================================================
    // OPEN EDIT REGISTRATION
    // =====================================================

    const openEditRegistration = (registration) => {
        clearForm();
        setFormType("registration");
        setEditingId(registration.registrationId);
        setRegistrationStudent(String(registration.student?.studentId || ""));
        setRegistrationSubject(String(registration.subject?.subjectId || ""));
        setRegistrationYear(String(registration.academicYear || ""));
        setRegistrationSemester(String(registration.semester || ""));
        setError("");
        setSuccess("");
        setShowForm(true);
    };


    
const deleteRegistration = (id) => {

    showConfirmDialog(
        "Are you sure you want to delete this student subject registration?",
        async () => {

            setError("");
            setSuccess("");

            try {

                await axios.delete(
                    `http://localhost:8080/api/student-subject-registrations/${id}`,
                    getConfig()
                );

                setSuccess(
                    "Student subject registration deleted successfully."
                );

                await loadDashboard();

            } catch (err) {

                console.error("Registration delete error:", err);

                setError(
                    err.response?.data?.message ||
                    err.response?.data ||
                    "Unable to delete registration."
                );
            }
        }
    );
};



    const submitRegistrationEdit = async () => {
        if (!registrationStudent || !registrationSubject || !registrationYear || !registrationSemester) {
            setError("Student, subject, academic year and semester are required.");
            return;
        }

        try {
            setFormLoading(true);
            await axios.put(
                `http://localhost:8080/api/student-subject-registrations/${editingId}`,
                {
                    student: { studentId: Number(registrationStudent) },
                    subject: { subjectId: Number(registrationSubject) },
                    academicYear: Number(registrationYear),
                    semester: Number(registrationSemester)
                },
                getConfig()
            );
            setSuccess("Student subject registration updated successfully.");
            setShowForm(false);
            clearForm();
            await loadDashboard();
        } catch (err) {
            console.error("Registration update error:", err);
            setError(err.response?.data?.message || err.response?.data || "Unable to update registration.");
        } finally {
            setFormLoading(false);
        }
    };


    // =====================================================
    // SUBMIT FORM
    // =====================================================

    const submitForm = async (event) => {

        event.preventDefault();

        setError("");
        setSuccess("");

        if (formType === "student") {
    await submitStudent();
    return;
}

        if (formType === "lecturer") {

            await submitLecturer();

            return;
        }

        if (formType === "subject") {

            await submitSubject();

            return;
        }

        if (formType === "registration") {
            await submitRegistrationEdit();
            return;
        }
    };


    // =====================================================
    // LOGOUT
    // =====================================================

    const handleLogout = () => {

        localStorage.removeItem("token");
        localStorage.removeItem("username");

        window.location.href = "/";
    };


    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    const getLecturerName = (allocation) => {

        return (
            allocation?.lecturer?.lecturerName ||
            allocation?.lecturer?.lecturer_name ||
            "-"
        );
    };


    const getSubjectCode = (allocation) => {

        return (
            allocation?.subject?.subjectCode ||
            allocation?.subject?.subject_code ||
            "-"
        );
    };


    const getSubjectName = (allocation) => {

        return (
            allocation?.subject?.subjectName ||
            allocation?.subject?.subject_name ||
            "-"
        );
    };


    const getDepartmentName = (departmentId) => {

        const department =
            departments.find(
                (item) =>
                    String(item.departmentId) ===
                    String(departmentId)
            );

        return (
            department?.departmentName ||
            department?.name ||
            "-"
        );
    };


    const getSpecializationName = (specializationId) => {

        const specialization =
            specializations.find(
                (item) =>
                    String(item.specializationId) ===
                    String(specializationId)
            );

        if (!specialization) {
            return "-";
        }

        return (
            specialization.specializationCode ||
            specialization.specializationName ||
            specialization.name ||
            "-"
        );
    };


    // =====================================================
    // FILTERED TABLE DATA
    // =====================================================

    const filteredStudents = students.filter((student) => {
        const registrationMatches = !studentRegistrationFilter ||
            String(student.registrationNumber || "")
                .toLowerCase()
                .includes(studentRegistrationFilter.trim().toLowerCase());

        const yearMatches = !studentYearFilter ||
            String(student.yearLevel) === String(studentYearFilter);
        const semesterMatches = !studentSemesterFilter ||
            String(student.semester) === String(studentSemesterFilter);
        return registrationMatches && yearMatches && semesterMatches;
    });

    const filteredLecturers = lecturers.filter((lecturer) => {
        const codeMatches = !lecturerCodeFilter ||
            String(lecturer.lecturerCode || "")
                .toLowerCase()
                .includes(lecturerCodeFilter.trim().toLowerCase());

        const departmentMatches = !lecturerDepartmentFilter ||
            String(lecturer.department?.departmentId || lecturer.departmentId || "") ===
            String(lecturerDepartmentFilter);

        return codeMatches && departmentMatches;
    });

    const filteredSubjects = subjects.filter((subject) => {
        const departmentName =
            subject.department?.departmentName ||
            subject.department?.name ||
            getDepartmentName(subject.department?.departmentId || subject.departmentId);

        const departmentMatches = !subjectDepartmentFilter ||
            String(departmentName || "")
                .toLowerCase()
                .includes(subjectDepartmentFilter.trim().toLowerCase());

        const yearMatches = !subjectYearFilter ||
            String(subject.academicYear) === String(subjectYearFilter);
        const semesterMatches = !subjectSemesterFilter ||
            String(subject.semester) === String(subjectSemesterFilter);

        return departmentMatches && yearMatches && semesterMatches;
    });

    const filteredAllocations = allocations.filter((allocation) => {
        const yearMatches = !allocationYearFilter ||
            String(allocation.academicYear) === String(allocationYearFilter);
        const semesterMatches = !allocationSemesterFilter ||
            String(allocation.semester) === String(allocationSemesterFilter);
        return yearMatches && semesterMatches;
    });

    const filteredRegistrations = registrations.filter((registration) => {
        const registrationNumber =
            registration.student?.registrationNumber ||
            registration.student?.registration_number ||
            "";

        const registrationMatches = !registrationNumberFilter ||
            String(registrationNumber)
                .toLowerCase()
                .includes(registrationNumberFilter.trim().toLowerCase());
        const yearMatches = !registrationYearFilter ||
            String(registration.academicYear) === String(registrationYearFilter);
        const semesterMatches = !registrationSemesterFilter ||
            String(registration.semester) === String(registrationSemesterFilter);

        return registrationMatches && yearMatches && semesterMatches;
    });


    // =====================================================
    // LOADING
    // =====================================================

    if (loading) {

        return (

            <div
                style={{
                    minHeight: "100vh",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    backgroundColor: "#f4f7fb"
                }}
            >

                <div
                    style={{
                        textAlign: "center"
                    }}
                >

                    <h2
                        style={{
                            margin: 0,
                            color: "#1e3a5f"
                        }}
                    >
                        Admin Dashboard
                    </h2>

                    <p
                        style={{
                            color: "#475569"
                        }}
                    >
                        Loading...
                    </p>

                </div>

            </div>
        );
    }


    // =====================================================
    // MAIN
    // =====================================================

    return (

        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#f4f7fb",
                display: "flex",
                fontFamily: "Arial, sans-serif"
            }}
        >

            {/* =================================================
                SIDEBAR
            ================================================= */}

            <aside
                style={{
                    width: "240px",
                    minHeight: "100vh",
                    backgroundColor: "#1f2937",
                    color: "#ffffff",
                    padding: "25px 15px 85px",
                    boxSizing: "border-box",
                    position: "fixed",
                    overflowY: "auto",
                    left: 0,
                    top: 0,
                    bottom: 0
                }}
            >

                <div
                    style={{
                        padding: "0 10px",
                        marginBottom: "30px"
                    }}
                >

                    <h2
                        style={{
                            margin: 0,
                            fontSize: "22px",
                            color: "#ffffff"
                        }}
                    >
                        Smart Faculty
                    </h2>

                    <p
                        style={{
                            marginTop: "7px",
                            marginBottom: 0,
                            color: "#dbe4f0",
                            fontSize: "13px"
                        }}
                    >
                        Admin Panel
                    </p>

                </div>


                <SidebarButton
                    active={
                        activeSection === "dashboard"
                    }
                    onClick={() =>
                        setActiveSection("dashboard")
                    }
                >
                    Dashboard
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "students"
                    }
                    onClick={() =>
                        setActiveSection("students")
                    }
                >
                    Students
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "lecturers"
                    }
                    onClick={() =>
                        setActiveSection("lecturers")
                    }
                >
                    Lecturers
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "subjects"
                    }
                    onClick={() =>
                        setActiveSection("subjects")
                    }
                >
                    Subjects
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "allocations"
                    }
                    onClick={() =>
                        setActiveSection("allocations")
                    }
                >
                    Teaching Allocations
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "registrations"
                    }
                    onClick={() =>
                        setActiveSection("registrations")
                    }
                >
                    Registrations
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "timetable"
                    }
                    onClick={() =>
                        setActiveSection("timetable")
                    }
                >
                    Timetable
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "requests"
                    }
                    onClick={() =>
                        setActiveSection("requests")
                    }
                >
                    Change Requests
                </SidebarButton>


                <SidebarButton
                    active={
                        activeSection === "accounts"
                    }
                    onClick={() => {
                        setActiveSection("accounts");
                        loadUserAccounts();
                    }}
                >
                    Account Management
                </SidebarButton>


                <button
                    type="button"
                    onClick={handleLogout}
                    style={{
                        position: "absolute",
                        left: "15px",
                        right: "15px",
                        bottom: "25px",
                        padding: "11px",
                        border: "1px solid #64748b",
                        borderRadius: "7px",
                        backgroundColor: "transparent",
                        color: "#ffffff",
                        cursor: "pointer",
                        fontWeight: "600"
                    }}
                >
                    Logout
                </button>

            </aside>


            {/* =================================================
                MAIN CONTENT
            ================================================= */}

            <main
                style={{
                    marginLeft: "240px",
                    width: "calc(100% - 240px)",
                    padding: "30px",
                    boxSizing: "border-box"
                }}
            >

                {/* HEADER */}

                <div
                    style={{
                        backgroundColor: "#ffffff",
                        padding: "22px 25px",
                        borderRadius: "12px",
                        marginBottom: "25px",
                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.08)",
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center"
                    }}
                >

                    <div>

                        <h1
                            style={{
                                margin: 0,
                                color: "#1e3a5f"
                            }}
                        >
                            Admin Dashboard
                        </h1>

                        <p
                            style={{
                                margin: "6px 0 0 0",
                                color: "#334e68"
                            }}
                        >
                            Welcome, {username}
                        </p>

                    </div>


                    <div
                        style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "10px",
                            position: "relative"
                        }}
                    >

                        <button
                            type="button"
                            onClick={handleAdminNotifications}
                            title={`${pendingChangeRequests.length} pending timetable change request${pendingChangeRequests.length === 1 ? "" : "s"}`}
                            style={{
                                position: "relative",
                                width: "44px",
                                height: "44px",
                                border: "1px solid #cbd5e1",
                                borderRadius: "50%",
                                backgroundColor: "#ffffff",
                                color: "#1e3a5f",
                                cursor: "pointer",
                                fontSize: "20px",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center"
                            }}
                        >
                            🔔

                            {pendingChangeRequests.length > 0 && (
                                <span
                                    style={{
                                        position: "absolute",
                                        top: "-5px",
                                        right: "-5px",
                                        minWidth: "20px",
                                        height: "20px",
                                        padding: "0 5px",
                                        borderRadius: "10px",
                                        backgroundColor: "#dc2626",
                                        color: "#ffffff",
                                        fontSize: "11px",
                                        fontWeight: "700",
                                        display: "flex",
                                        alignItems: "center",
                                        justifyContent: "center",
                                        boxSizing: "border-box"
                                    }}
                                >
                                    {pendingChangeRequests.length > 99
                                        ? "99+"
                                        : pendingChangeRequests.length}
                                </span>
                            )}
                        </button>

                        {showNotifications && (
                            <div
                                style={{
                                    position: "absolute",
                                    top: "52px",
                                    right: "0",
                                    width: "340px",
                                    backgroundColor: "#ffffff",
                                    border: "1px solid #e2e8f0",
                                    borderRadius: "10px",
                                    boxShadow: "0 8px 24px rgba(0,0,0,0.12)",
                                    zIndex: 1000,
                                    padding: "15px",
                                    boxSizing: "border-box"
                                }}
                            >
                                <div
                                    style={{
                                        display: "flex",
                                        justifyContent: "space-between",
                                        alignItems: "center",
                                        marginBottom: "10px"
                                    }}
                                >
                                    <strong style={{ color: "#1e3a5f" }}>
                                        Notifications
                                    </strong>

                                    <span
                                        style={{
                                            fontSize: "12px",
                                            color: "#64748b"
                                        }}
                                    >
                                        {pendingChangeRequests.length} pending
                                    </span>
                                </div>

                                {pendingChangeRequests.length === 0 ? (
                                    <div
                                        style={{
                                            padding: "15px 5px",
                                            textAlign: "center",
                                            color: "#64748b",
                                            fontSize: "13px"
                                        }}
                                    >
                                        No pending timetable change requests.
                                    </div>
                                ) : (
                                    <div
                                        style={{
                                            maxHeight: "280px",
                                            overflowY: "auto"
                                        }}
                                    >
                                        {pendingChangeRequests.map((request) => (
                                            <button
                                                key={request?.requestId || request?.id}
                                                type="button"
                                                onClick={openPendingChangeRequests}
                                                style={{
                                                    width: "100%",
                                                    textAlign: "left",
                                                    border: "none",
                                                    borderBottom: "1px solid #e2e8f0",
                                                    backgroundColor: "#ffffff",
                                                    padding: "11px 5px",
                                                    cursor: "pointer"
                                                }}
                                            >
                                                <div
                                                    style={{
                                                        fontWeight: "700",
                                                        color: "#1e3a5f",
                                                        fontSize: "13px"
                                                    }}
                                                >
                                                    Pending Timetable Change Request
                                                </div>

                                                <div
                                                    style={{
                                                        color: "#475569",
                                                        fontSize: "12px",
                                                        marginTop: "4px"
                                                    }}
                                                >
                                                    {request?.lecturer?.lecturerName ||
                                                        request?.lecturerName ||
                                                        "Lecturer request"}
                                                    {" • "}
                                                    {request?.subject?.subjectCode ||
                                                        request?.subjectCode ||
                                                        "Subject"}
                                                </div>
                                            </button>
                                        ))}
                                    </div>
                                )}

                                {pendingChangeRequests.length > 0 && (
                                    <button
                                        type="button"
                                        onClick={openPendingChangeRequests}
                                        style={{
                                            width: "100%",
                                            marginTop: "10px",
                                            padding: "9px",
                                            border: "none",
                                            borderRadius: "7px",
                                            backgroundColor: "#1e3a5f",
                                            color: "#ffffff",
                                            cursor: "pointer",
                                            fontWeight: "600"
                                        }}
                                    >
                                        Review Change Requests
                                    </button>
                                )}
                            </div>
                        )}

                        <button
                            type="button"
                            onClick={loadDashboard}
                            style={primaryButtonStyle}
                        >
                            Refresh
                        </button>

                    </div>
                    <ConfirmDialog
    open={confirmDialog.open}
    message={confirmDialog.message}
    onConfirm={handleConfirmAction}
    onCancel={closeConfirmDialog}
/>

                </div>





                {/* =================================================
                    DASHBOARD
                ================================================= */}

                {activeSection === "dashboard" && (

                    <>

                        <h2 style={sectionTitleStyle}>
                            Overview
                        </h2>


                        <div
                            style={{
                                display: "grid",
                                gridTemplateColumns:
                                    "repeat(auto-fit, minmax(200px, 1fr))",
                                gap: "18px",
                                marginBottom: "25px"
                            }}
                        >

                            <DashboardCard
                                title="Total Students"
                                value={students.length}
                                onClick={() =>
                                    setActiveSection("students")
                                }
                            />

                            <DashboardCard
                                title="Total Lecturers"
                                value={lecturers.length}
                                onClick={() =>
                                    setActiveSection("lecturers")
                                }
                            />

                            <DashboardCard
                                title="Total Subjects"
                                value={subjects.length}
                                onClick={() =>
                                    setActiveSection("subjects")
                                }
                            />

                            <DashboardCard
                                title="Teaching Allocations"
                                value={allocations.length}
                                onClick={() =>
                                    setActiveSection("allocations")
                                }
                            />

                            <DashboardCard
                                title="Registrations"
                                value={registrations.length}
                                onClick={() =>
                                    setActiveSection("registrations")
                                }
                            />

                        </div>


                        <SectionCard title="Administration">

                            <div
                                style={{
                                    display: "grid",
                                    gridTemplateColumns:
                                        "repeat(auto-fit, minmax(220px, 1fr))",
                                    gap: "15px"
                                }}
                            >

                                <AdminAction
                                    title="Manage Students"
                                    onClick={() =>
                                        setActiveSection("students")
                                    }
                                />

                                <AdminAction
                                    title="Manage Lecturers"
                                    onClick={() =>
                                        setActiveSection("lecturers")
                                    }
                                />

                                <AdminAction
                                    title="Manage Subjects"
                                    onClick={() =>
                                        setActiveSection("subjects")
                                    }
                                />

                                <AdminAction
                                    title="Manage Timetable"
                                    onClick={() =>
                                        setActiveSection("timetable")
                                    }
                                />

                                <AdminAction
                                    title="Teaching Allocations"
                                    onClick={() =>
                                        setActiveSection("allocations")
                                    }
                                />

                                <AdminAction
                                    title="Change Requests"
                                    onClick={() => {
                                        setActiveSection("requests");
                                        loadChangeRequests();
                                    }}
                                />

                                <AdminAction
                                    title="Account Management"
                                    onClick={() => {
                                        setActiveSection("accounts");
                                        loadUserAccounts();
                                    }}
                                />

                            </div>

                        </SectionCard>

                    </>
                )}


                {/* =================================================
                    STUDENTS
                ================================================= */}

                {activeSection === "students" && (

                    <SectionCard title="Student Management">

                        <div style={sectionToolbarStyle}>
                            <div>
                                <h3 style={{ margin: 0 }}>Registered Students</h3>
                            </div>

                            <div style={toolbarActionsStyle}>
                                <label style={csvImportButtonStyle}>
                                    Import Students CSV
                                    <input
                                        type="file"
                                        accept=".csv,text/csv"
                                        onChange={importStudentsCSV}
                                        style={hiddenFileInputStyle}
                                        disabled={formLoading}
                                    />
                                </label>

                                <button type="button" onClick={openAddStudent} style={primaryButtonStyle}>
                                    + Add Student
                                </button>
                            </div>
                        </div>

                        <div style={filterBarStyle}>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Registration Number</label>
                                <input
                                    type="text"
                                    value={studentRegistrationFilter}
                                    onChange={(e) => setStudentRegistrationFilter(e.target.value)}
                                    placeholder="Search registration number"
                                    style={filterSearchInputStyle}
                                />
                            </div>

                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Academic Year</label>
                                <select value={studentYearFilter} onChange={(e) => setStudentYearFilter(e.target.value)} style={dropdownStyle}>
                                    <option value="">All Years</option>
                                    <option value="1">Year 1</option>
                                    <option value="2">Year 2</option>
                                    <option value="3">Year 3</option>
                                    <option value="4">Year 4</option>
                                </select>
                            </div>

                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Semester</label>
                                <select value={studentSemesterFilter} onChange={(e) => setStudentSemesterFilter(e.target.value)} style={dropdownStyle}>
                                    <option value="">All Semesters</option>
                                    <option value="1">Semester 1</option>
                                    <option value="2">Semester 2</option>
                                </select>
                            </div>

                            <button type="button" onClick={() => { setStudentRegistrationFilter(""); setStudentYearFilter(""); setStudentSemesterFilter(""); }} style={secondaryButtonStyle}>
                                Clear Filters
                            </button>
                        </div>

                        <p style={filterCountStyle}>Showing {filteredStudents.length} of {students.length} students</p>

                        {filteredStudents.length === 0 ? (
                            <EmptyMessage message="No students match the selected filters." />
                        ) : (
                            <div style={scrollTableStyle}>
                                <table style={tableStyle}>
                                    <thead><tr>
                                        <th style={headerStyle}>#</th>
                                        <th style={headerStyle}>Registration Number</th>
                                        <th style={headerStyle}>Name</th>
                                        <th style={headerStyle}>Email</th>
                                        <th style={headerStyle}>Year</th>
                                        <th style={headerStyle}>Semester</th>
                                        <th style={headerStyle}>Degree Type</th>
                                        <th style={headerStyle}>Major</th>
                                        <th style={headerStyle}>Minor</th>
                                        <th style={headerStyle}>Actions</th>
                                    </tr></thead>
                                    <tbody>
                                        {filteredStudents.map((student, index) => (
                                            <tr key={student.studentId}>
                                                <td style={cellStyle}>{index + 1}</td>
                                                <td style={cellStyle}><strong>{student.registrationNumber}</strong></td>
                                                <td style={cellStyle}>{student.studentName}</td>
                                                <td style={cellStyle}>{student.email}</td>
                                                <td style={cellStyle}>Year {student.yearLevel}</td>
                                                <td style={cellStyle}>Semester {student.semester}</td>
                                                <td style={cellStyle}>{student.degreeType === "SPECIAL"
                                                       ? "Special Degree"
                                                     : student.degreeType === "GENERAL"
                                                         ? "General Degree"
                                                     : "-"}
                                                </td>
                                                <td style={cellStyle}>{student.majorSpecializationCode || getSpecializationName(student.majorSpecializationId) || "-"}</td>
                                                <td style={cellStyle}>{student.minorSpecializationCode || getSpecializationName(student.minorSpecializationId) || "-"}</td>
                                                <td style={cellStyle}>
                                                    <ActionButtons
                                                        onEdit={() => openEditStudent(student)}
                                                        onDelete={() => deleteStudent(student.studentId)}
                                                    />
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </SectionCard>
                )}


                {/* =================================================
                    LECTURERS
                ================================================= */}

                {activeSection === "lecturers" && (
                    <SectionCard title="Lecturer Management">
                        <div style={sectionToolbarStyle}>
                            <div>
                                <h3 style={{ margin: 0 }}>Registered Lecturers</h3>
                            </div>
                            <div style={toolbarActionsStyle}>
                                <label style={csvImportButtonStyle}>
                                    Import Lecturers CSV
                                    <input
                                        type="file"
                                        accept=".csv,text/csv"
                                        onChange={importLecturersCSV}
                                        style={hiddenFileInputStyle}
                                        disabled={formLoading}
                                    />
                                </label>

                                <button type="button" onClick={openAddLecturer} style={primaryButtonStyle}>
                                    + Add Lecturer
                                </button>
                            </div>
                        </div>

                        <div style={filterBarStyle}>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Lecturer Code</label>
                                <input
                                    type="text"
                                    value={lecturerCodeFilter}
                                    onChange={(e) => setLecturerCodeFilter(e.target.value)}
                                    placeholder="Search lecturer code"
                                    style={filterSearchInputStyle}
                                />
                            </div>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Department</label>
                                <select value={lecturerDepartmentFilter} onChange={(e) => setLecturerDepartmentFilter(e.target.value)} style={dropdownStyle}>
                                    <option value="">All Departments</option>
                                    {departments.map((department) => (
                                        <option key={department.departmentId} value={department.departmentId}>
                                            {department.departmentName || department.name || `Department ${department.departmentId}`}
                                        </option>
                                    ))}
                                </select>
                            </div>
                            <button type="button" onClick={() => { setLecturerCodeFilter(""); setLecturerDepartmentFilter(""); }} style={secondaryButtonStyle}>
                                Clear Filters
                            </button>
                        </div>

                        <p style={filterCountStyle}>Showing {filteredLecturers.length} of {lecturers.length} lecturers</p>

                        {filteredLecturers.length === 0 ? (
                            <EmptyMessage message="No lecturers match the selected department." />
                        ) : (
                            <div style={scrollTableStyle}>
                                <table style={tableStyle}>
                                    <thead><tr>
                                        <th style={headerStyle}>#</th>
                                        <th style={headerStyle}>Lecturer Code</th>
                                        <th style={headerStyle}>Lecturer Name</th>
                                        <th style={headerStyle}>Email</th>
                                        <th style={headerStyle}>Department</th>
                                        <th style={headerStyle}>Actions</th>
                                    </tr></thead>
                                    <tbody>
                                        {filteredLecturers.map((lecturer, index) => (
                                            <tr key={lecturer.lecturerId}>
                                                <td style={cellStyle}>{index + 1}</td>
                                                <td style={cellStyle}><strong>{lecturer.lecturerCode}</strong></td>
                                                <td style={cellStyle}>{lecturer.lecturerName}</td>
                                                <td style={cellStyle}>{lecturer.email || "-"}</td>
                                                <td style={cellStyle}>{lecturer.department?.departmentName || lecturer.department?.name || getDepartmentName(lecturer.departmentId) || "-"}</td>
                                                <td style={cellStyle}>
                                                    <ActionButtons
                                                        onEdit={() => openEditLecturer(lecturer)}
                                                        onDelete={() => deleteLecturer(lecturer.lecturerId)}
                                                    />
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </SectionCard>
                )}


                {/* =================================================
                    SUBJECTS
                ================================================= */}

                {activeSection === "subjects" && (

                    <SectionCard title="Subject Management">

                        <div style={sectionToolbarStyle}>

                            <div>

                                <h3 style={{ margin: 0 }}>
                                    Registered Subjects
                                </h3>

                                

                            </div>

                            <div style={toolbarActionsStyle}>

                                <label style={csvImportButtonStyle}>
                                    Import Subjects CSV
                                    <input
                                        type="file"
                                        accept=".csv,text/csv"
                                        onChange={importSubjectsCSV}
                                        style={hiddenFileInputStyle}
                                        disabled={formLoading}
                                    />
                                </label>

                                <button
                                    type="button"
                                    onClick={openAddSubject}
                                    style={primaryButtonStyle}
                                >
                                    + Add Subject
                                </button>

                            </div>

                        </div>

                        <div style={filterBarStyle}>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Department</label>
                                <input
                                    type="text"
                                    value={subjectDepartmentFilter}
                                    onChange={(e) => setSubjectDepartmentFilter(e.target.value)}
                                    placeholder="Search department"
                                    style={filterSearchInputStyle}
                                />
                            </div>

                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Year</label>
                                <select value={subjectYearFilter} onChange={(e) => setSubjectYearFilter(e.target.value)} style={filterInputStyle}>
                                    <option value="">All Years</option>
                                    <option value="1">Year 1</option>
                                    <option value="2">Year 2</option>
                                    <option value="3">Year 3</option>
                                    <option value="4">Year 4</option>
                                </select>
                            </div>

                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Semester</label>
                                <select value={subjectSemesterFilter} onChange={(e) => setSubjectSemesterFilter(e.target.value)} style={filterInputStyle}>
                                    <option value="">All Semesters</option>
                                    <option value="1">Semester 1</option>
                                    <option value="2">Semester 2</option>
                                </select>
                            </div>

                            {(subjectDepartmentFilter || subjectYearFilter || subjectSemesterFilter) && (
                                <button type="button" onClick={() => { setSubjectDepartmentFilter(""); setSubjectYearFilter(""); setSubjectSemesterFilter(""); }} style={secondaryButtonStyle}>
                                    Clear Filter
                                </button>
                            )}
                        </div>

                        <p style={filterCountStyle}>
                            Showing {filteredSubjects.length} of {subjects.length} subjects
                        </p>


                        {subjects.length === 0 ? (

                            <EmptyMessage
                                message="No subjects found."
                            />

                        ) : filteredSubjects.length === 0 ? (

                            <EmptyMessage
                                message="No subjects match the selected Year and Semester filters."
                            />

                        ) : (

                            <div style={scrollTableStyle}>

                                <table style={tableStyle}>

                                    <thead>

                                        <tr>

                                            <th style={headerStyle}>
                                                #
                                            </th>

                                            <th style={headerStyle}>
                                                Code
                                            </th>

                                            <th style={headerStyle}>
                                                Subject Name
                                            </th>

                                            <th style={headerStyle}>
                                                Department
                                            </th>

                                            <th style={headerStyle}>
                                                Specialization
                                            </th>

                                            <th style={headerStyle}>
                                                Year
                                            </th>

                                            <th style={headerStyle}>
                                                Semester
                                            </th>

                                            <th style={headerStyle}>
                                                Credits
                                            </th>

                                            <th style={headerStyle}>
                                                Type
                                            </th>

                                            <th style={headerStyle}>
                                                Actions
                                            </th>

                                        </tr>

                                    </thead>


                                    <tbody>

                                        {filteredSubjects.map(
                                            (subject, index) => (

                                                <tr
                                                    key={
                                                        subject.subjectId
                                                    }
                                                >

                                                    <td style={cellStyle}>
                                                        {index + 1}
                                                    </td>

                                                    <td style={cellStyle}>
                                                        <strong>
                                                            {
                                                                subject.subjectCode
                                                            }
                                                        </strong>
                                                    </td>

                                                    <td style={cellStyle}>
                                                        {
                                                            subject.subjectName
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>
                                                        {
                                                            subject.department?.departmentName ||
                                                            subject.department?.name ||
                                                            "-"
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>
                                                        {
                                                            subject.specialization?.specializationCode ||
                                                            subject.specialization?.specializationName ||
                                                            "-"
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>
                                                        Year {
                                                            subject.academicYear
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>
                                                        Semester {
                                                            subject.semester
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>
                                                        {
                                                            subject.credits
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>

                                                        {subject.compulsory
                                                            ? "Compulsory"
                                                            : "Optional"}

                                                    </td>

                                                    <td style={cellStyle}>

                                                        <ActionButtons
                                                            onEdit={() =>
                                                                openEditSubject(
                                                                    subject
                                                                )
                                                            }
                                                            onDelete={() =>
                                                                deleteSubject(
                                                                    subject.subjectId
                                                                )
                                                            }
                                                        />

                                                    </td>

                                                </tr>

                                            )
                                        )}

                                    </tbody>

                                </table>

                            </div>

                        )}

                    </SectionCard>
                )}


                {/* =================================================
                    ALLOCATIONS
                ================================================= */}

                {activeSection === "allocations" && (

                    <SectionCard title="Teaching Allocations">

                        <div style={filterBarStyle}>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Academic Year</label>
                                <select value={allocationYearFilter} onChange={(e) => setAllocationYearFilter(e.target.value)} style={dropdownStyle}>
                                    <option value="">All Years</option>
                                    <option value="1">Year 1</option>
                                    <option value="2">Year 2</option>
                                    <option value="3">Year 3</option>
                                    <option value="4">Year 4</option>
                                </select>
                            </div>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Semester</label>
                                <select value={allocationSemesterFilter} onChange={(e) => setAllocationSemesterFilter(e.target.value)} style={dropdownStyle}>
                                    <option value="">All Semesters</option>
                                    <option value="1">Semester 1</option>
                                    <option value="2">Semester 2</option>
                                </select>
                            </div>
                            <button type="button" onClick={() => { setAllocationYearFilter(""); setAllocationSemesterFilter(""); }} style={secondaryButtonStyle}>
                                Clear Filters
                            </button>
                        </div>

                        <p style={filterCountStyle}>Showing {filteredAllocations.length} of {allocations.length} teaching allocations</p>


                        {allocations.length === 0 ? (

                            <EmptyMessage
                                message="No teaching allocations found."
                            />

                        ) : filteredAllocations.length === 0 ? (

                            <EmptyMessage
                                message="No teaching allocations match the selected filters."
                            />

                        ) : (

                            <div style={scrollTableStyle}>

                                <table style={tableStyle}>

                                    <thead>

                                        <tr>

                                            <th style={headerStyle}>
                                                #
                                            </th>

                                            <th style={headerStyle}>
                                                Lecturer
                                            </th>

                                            <th style={headerStyle}>
                                                Subject
                                            </th>

                                            <th style={headerStyle}>
                                                Year
                                            </th>

                                            <th style={headerStyle}>
                                                Semester
                                            </th>

                                        </tr>

                                    </thead>


                                    <tbody>

                                        {filteredAllocations.map(
                                            (allocation, index) => (

                                                <tr
                                                    key={
                                                        allocation.allocationId
                                                    }
                                                >

                                                    <td style={cellStyle}>
                                                        {index + 1}
                                                    </td>

                                                    <td style={cellStyle}>
                                                        {
                                                            getLecturerName(
                                                                allocation
                                                            )
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>

                                                        <strong>
                                                            {
                                                                getSubjectCode(
                                                                    allocation
                                                                )
                                                            }
                                                        </strong>

                                                        <br />

                                                        <small>
                                                            {
                                                                getSubjectName(
                                                                    allocation
                                                                )
                                                            }
                                                        </small>

                                                    </td>

                                                    <td style={cellStyle}>
                                                        Year {
                                                            allocation.academicYear
                                                        }
                                                    </td>

                                                    <td style={cellStyle}>
                                                        Semester {
                                                            allocation.semester
                                                        }
                                                    </td>

                                                </tr>

                                            )
                                        )}

                                    </tbody>

                                </table>

                            </div>

                        )}

                    </SectionCard>
                )}


                {/* =================================================
                    REGISTRATIONS
                ================================================= */}

                {activeSection === "registrations" && (
                    <SectionCard title="Student Subject Registrations">
                        <div style={sectionToolbarStyle}>
                            <div>
                                <h3 style={{ margin: 0 }}>Registered Subjects</h3>
                                
                            </div>
                            <button type="button" onClick={loadDashboard} style={primaryButtonStyle}>Refresh Registrations</button>
                        
                        </div>

                        <div style={filterBarStyle}>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Registration Number</label>
                                <input type="text" value={registrationNumberFilter} onChange={(e) => setRegistrationNumberFilter(e.target.value)} placeholder="Search registration number" style={filterSearchInputStyle} />
                            </div>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Academic Year</label>
                                <select value={registrationYearFilter} onChange={(e) => setRegistrationYearFilter(e.target.value)} style={dropdownStyle}>
                                    <option value="">All Years</option>
                                    <option value="1">Year 1</option>
                                    <option value="2">Year 2</option>
                                    <option value="3">Year 3</option>
                                    <option value="4">Year 4</option>
                                </select>
                            </div>
                            <div style={filterFieldStyle}>
                                <label style={filterLabelStyle}>Semester</label>
                                <select value={registrationSemesterFilter} onChange={(e) => setRegistrationSemesterFilter(e.target.value)} style={dropdownStyle}>
                                    <option value="">All Semesters</option>
                                    <option value="1">Semester 1</option>
                                    <option value="2">Semester 2</option>
                                </select>
                            </div>
                            <button type="button" onClick={() => { setRegistrationNumberFilter(""); setRegistrationYearFilter(""); setRegistrationSemesterFilter(""); }} style={secondaryButtonStyle}>
                                Clear Filters
                            </button>
                        </div>

                        <p style={filterCountStyle}>Showing {filteredRegistrations.length} of {registrations.length} registrations</p>

                        {registrations.length === 0 ? (
                            <EmptyMessage message="No subject registrations found." />
                        ) : filteredRegistrations.length === 0 ? (
                            <EmptyMessage message="No registrations match the selected filters." />
                        ) : (
                            <div style={scrollTableStyle}>
                                <table style={tableStyle}>
                                    <thead><tr>
                                        <th style={headerStyle}>#</th>
                                        <th style={headerStyle}>Student</th>
                                        <th style={headerStyle}>Subject</th>
                                        <th style={headerStyle}>Year</th>
                                        <th style={headerStyle}>Semester</th>
                                        <th style={headerStyle}>Actions</th>
                                    </tr></thead>
                                    <tbody>
                                        {filteredRegistrations.map((registration, index) => (
                                            <tr key={registration.registrationId}>
                                                <td style={cellStyle}>{index + 1}</td>
                                                <td style={cellStyle}>
                                                    <strong>{registration.student?.studentName || "-"}</strong><br />
                                                    <small>{registration.student?.registrationNumber || "-"}</small>
                                                </td>
                                                <td style={cellStyle}>
                                                    <strong>{registration.subject?.subjectCode || "-"}</strong><br />
                                                    <small>{registration.subject?.subjectName || "-"}</small>
                                                </td>
                                                <td style={cellStyle}>Year {registration.academicYear}</td>
                                                <td style={cellStyle}>Semester {registration.semester}</td>
                                                <td style={cellStyle}>
                                                    <ActionButtons
                                                        onEdit={() => openEditRegistration(registration)}
                                                        onDelete={() => deleteRegistration(registration.registrationId)}
                                                    />
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </SectionCard>
                )}


                {/* =================================================
                    TIMETABLE
                ================================================= */}

                {activeSection === "timetable" && (

                    <AdminTimetableManagement />

                )}


                {/* =================================================
                    CHANGE REQUESTS
                ================================================= */}

                {activeSection === "requests" && (
                    <SectionCard title="Timetable Change Requests">
                        <div style={sectionToolbarStyle}>
                            <div>
                                <h3 style={{ margin: 0 }}>Lecturer Change Requests</h3>
                                
                                
                            </div>
                            <button
                                type="button"
                                onClick={loadChangeRequests}
                                style={primaryButtonStyle}
                                disabled={requestLoading}
                            >
                                {requestLoading ? "Refreshing..." : "Refresh Requests"}
                            </button>
                        </div>

                        {changeRequests.length === 0 ? (
                            <EmptyMessage message="No timetable change requests found." />
                        ) : (
                            <div style={scrollTableStyle}>
                                <table style={tableStyle}>
                                    <thead><tr>
                                        <th style={headerStyle}>#</th>
                                        <th style={headerStyle}>Lecturer</th>
                                        <th style={headerStyle}>Subject</th>
                                        <th style={headerStyle}>Requested Change</th>
                                        <th style={headerStyle}>Reason</th>
                                        <th style={headerStyle}>Status</th>
                                        <th style={headerStyle}>Actions</th>
                                    </tr></thead>
                                    <tbody>
                                        {changeRequests.map((request, index) => (
                                            <tr key={request.requestId}>
                                                <td style={cellStyle}>{index + 1}</td>
                                                <td style={cellStyle}>
                                                    <strong>{request.lecturer?.lecturerName || "-"}</strong><br />
                                                    <small>{request.lecturer?.lecturerCode || "-"}</small>
                                                </td>
                                                <td style={cellStyle}>
                                                    <strong>{request.timetable?.lecturerAllocation?.subject?.subjectCode || "-"}</strong><br />
                                                    <small>{request.timetable?.lecturerAllocation?.subject?.subjectName || "-"}</small>
                                                </td>
                                                <td style={cellStyle}>
                                                    {request.requestedDay || "Current day"}<br />
                                                    {(request.requestedStartTime || request.requestedEndTime) && (
                                                        <small>{request.requestedStartTime || "-"} - {request.requestedEndTime || "-"}</small>
                                                    )}
                                                    {request.requestedClassroom && <><br /><small>Room: {request.requestedClassroom}</small></>}
                                                </td>
                                                <td style={cellStyle}>{request.reason || "-"}</td>
                                                <td style={cellStyle}>
                                                    <span style={statusBadgeStyle(request.status)}>{request.status || "PENDING"}</span>
                                                    {request.adminResponse && <><br /><small>{request.adminResponse}</small></>}
                                                </td>
                                                <td style={cellStyle}>
                                                    <div style={actionGroupStyle}>
                                                        {String(request.status).toUpperCase() === "PENDING" && (
                                                            <>
                                                                <button
    type="button"
    onClick={() =>
        openRequestResponseDialog(request.requestId, "approve")
    }
    style={{
        backgroundColor: "#16a34a",
        color: "#ffffff",
        border: "none",
        padding: "8px 16px",
        borderRadius: "6px",
        fontWeight: "600",
        cursor: "pointer"
    }}
>
    Approve
</button>

<button
    type="button"
    onClick={() =>
        openRequestResponseDialog(request.requestId, "reject")
    }
    style={{
        backgroundColor: "#dc2626",
        color: "#ffffff",
        border: "none",
        padding: "8px 16px",
        borderRadius: "6px",
        fontWeight: "600",
        cursor: "pointer"
    }}
>
    Reject
</button>
                                                            </>
                                                        )}
                                                        <button type="button" onClick={() => deleteChangeRequest(request.requestId)} style={deleteButtonStyle}>Delete</button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </SectionCard>
                )}

                {/* =================================================
    ACCOUNT MANAGEMENT
================================================= */}

{activeSection === "accounts" && (

    <SectionCard title="Account Management">

        <div style={sectionToolbarStyle}>

            <div>

                <h3 style={{ margin: 0 }}>
                    User Accounts
                </h3>

            </div>


            <button
                type="button"
                onClick={loadUserAccounts}
                style={primaryButtonStyle}
                disabled={accountLoading}
            >
                {
                    accountLoading
                        ? "Refreshing..."
                        : "Refresh Accounts"
                }
            </button>

        </div>


        {/* =================================================
            ACCOUNT TABLE
        ================================================= */}

        {accountLoading ? (

            <div
                style={{
                    padding: "30px",
                    textAlign: "center",
                    color: "#64748b"
                }}
            >
                Loading user accounts...
            </div>

        ) : userAccounts.length === 0 ? (

            <EmptyMessage
                message="No user accounts found."
            />

        ) : (

            <div style={scrollTableStyle}>

                <table style={tableStyle}>

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

                        {userAccounts.map(
                            (account, index) => {

                                const enabled =
                                    account.enabled === true ||
                                    String(
                                        account.enabled
                                    ).toLowerCase() === "true";

                                const actionKey =
                                    `${enabled ? "disable" : "enable"}-${account.userId}`;

                                const canEditRole =
                                    account.role === "LECTURER" ||
                                    account.role === "HOD";


                                return (

                                    <tr
                                        key={
                                            account.userId
                                        }
                                    >

                                        {/* NUMBER */}

                                        <td style={cellStyle}>
                                            {index + 1}
                                        </td>


                                        {/* USERNAME */}

                                        <td style={cellStyle}>

                                            <strong>
                                                {
                                                    account.username ||
                                                    "-"
                                                }
                                            </strong>

                                        </td>


                                        {/* ROLE */}

                                        <td style={cellStyle}>

                                            {
                                                account.role ||
                                                "-"
                                            }

                                        </td>


                                        {/* STATUS */}

                                        <td style={cellStyle}>

                                            <span
                                                style={
                                                    enabled
                                                        ? accountEnabledBadgeStyle
                                                        : accountDisabledBadgeStyle
                                                }
                                            >
                                                {
                                                    enabled
                                                        ? "Enabled"
                                                        : "Disabled"
                                                }
                                            </span>

                                        </td>


                                        {/* ACTIONS */}

                                        <td style={cellStyle}>

                                            <div
                                                style={{
                                                    display: "flex",
                                                    justifyContent: "center",
                                                    alignItems: "center",
                                                    gap: "10px",
                                                    flexWrap: "nowrap"
                                                }}
                                            >

                                                {/* EDIT ROLE */}

                                                {canEditRole ? (

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            openRoleEdit(
                                                                account
                                                            )
                                                        }
                                                        style={{
                                                            minWidth: "82px",
                                                            padding: "7px 10px",
                                                            border: "none",
                                                            borderRadius: "6px",
                                                            backgroundColor: "#334e68",
                                                            color: "#ffffff",
                                                            cursor: "pointer",
                                                            fontWeight: "600",
                                                            whiteSpace: "nowrap"
                                                        }}
                                                    >
                                                        Edit Role
                                                    </button>

                                                ) : (

                                                    <div
                                                        style={{
                                                            width: "82px"
                                                        }}
                                                    />

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
                                                        accountActionLoading ===
                                                        actionKey
                                                    }
                                                    style={{
                                                        ...(enabled
                                                            ? disableAccountButtonStyle
                                                            : enableAccountButtonStyle),
                                                        minWidth: "82px",
                                                        whiteSpace: "nowrap"
                                                    }}
                                                >
                                                    {
                                                        accountActionLoading ===
                                                        actionKey
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
                                                        openPasswordReset(
                                                            account
                                                        )
                                                    }
                                                    style={{
                                                        ...resetPasswordButtonStyle,
                                                        minWidth: "125px",
                                                        whiteSpace: "nowrap"
                                                    }}
                                                >
                                                    Reset Password
                                                </button>

                                            </div>

                                        </td>

                                    </tr>

                                );

                            }
                        )}

                    </tbody>

                </table>

            </div>

        )}


        {/* =================================================
            ROLE EDIT PANEL
        ================================================= */}

        {roleEditAccount && (

            <div
                style={{
                    marginTop: "25px",
                    padding: "20px",
                    border: "1px solid #d7e0ea",
                    borderRadius: "8px",
                    backgroundColor: "#f8fafc"
                }}
            >

                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        marginBottom: "18px"
                    }}
                >

                    <h3 style={{ margin: 0 }}>
                        Edit User Role
                    </h3>


                    <button
                        type="button"
                        onClick={closeRoleEdit}
                        disabled={roleUpdateLoading}
                        style={closeButtonStyle}
                    >
                        ×
                    </button>

                </div>


                <FormField label="Username">

                    <input
                        value={
                            roleEditAccount.username ||
                            ""
                        }
                        readOnly
                        style={inputStyle}
                    />

                </FormField>


                <FormField label="Current Role">

                    <input
                        value={
                            roleEditAccount.role ||
                            ""
                        }
                        readOnly
                        style={inputStyle}
                    />

                </FormField>


                <FormField label="New Role">

                    <select
                        value={selectedRole}
                        onChange={(e) =>
                            setSelectedRole(
                                e.target.value
                            )
                        }
                        disabled={roleUpdateLoading}
                        style={inputStyle}
                    >

                        <option value="LECTURER">
                            Lecturer
                        </option>

                        <option value="HOD">
                            HOD
                        </option>

                    </select>

                </FormField>


                <div
                    style={{
                        display: "flex",
                        gap: "10px",
                        marginTop: "20px"
                    }}
                >

                    <button
                        type="button"
                        onClick={updateUserRole}
                        disabled={roleUpdateLoading}
                        style={primaryButtonStyle}
                    >
                        {
                            roleUpdateLoading
                                ? "Updating..."
                                : "Update Role"
                        }
                    </button>


                    <button
                        type="button"
                        onClick={closeRoleEdit}
                        disabled={roleUpdateLoading}
                        style={secondaryButtonStyle}
                    >
                        Cancel
                    </button>

                </div>

            </div>

        )}


        {/* =================================================
            PASSWORD RESET PANEL
        ================================================= */}

        {resetUserId && (

            <div
                style={{
                    marginTop: "25px",
                    padding: "20px",
                    border: "1px solid #d7e0ea",
                    borderRadius: "8px",
                    backgroundColor: "#f8fafc"
                }}
            >

                <div
                    style={{
                        display: "flex",
                        justifyContent: "space-between",
                        alignItems: "center",
                        marginBottom: "18px"
                    }}
                >

                    <h3 style={{ margin: 0 }}>
                        Reset Password
                    </h3>


                    <button
                        type="button"
                        onClick={cancelPasswordReset}
                        style={closeButtonStyle}
                    >
                        ×
                    </button>

                </div>


                <FormField label="Username">

                    <input
                        value={resetUsername}
                        readOnly
                        style={inputStyle}
                    />

                </FormField>


                <FormField label="New Password">

                    <input
                        type="password"
                        value={resetNewPassword}
                        onChange={(e) =>
                            setResetNewPassword(
                                e.target.value
                            )
                        }
                        style={inputStyle}
                        autoComplete="new-password"
                    />

                </FormField>


                <FormField label="Confirm New Password">

                    <input
                        type="password"
                        value={
                            resetConfirmNewPassword
                        }
                        onChange={(e) =>
                            setResetConfirmNewPassword(
                                e.target.value
                            )
                        }
                        style={inputStyle}
                        autoComplete="new-password"
                    />

                </FormField>


                <div
                    style={{
                        display: "flex",
                        gap: "10px",
                        marginTop: "20px"
                    }}
                >

                    <button
                        type="button"
                        onClick={
                            resetAccountPassword
                        }
                        disabled={
                            resetPasswordLoading
                        }
                        style={primaryButtonStyle}
                    >
                        {
                            resetPasswordLoading
                                ? "Resetting..."
                                : "Reset Password"
                        }
                    </button>


                    <button
                        type="button"
                        onClick={
                            cancelPasswordReset
                        }
                        disabled={
                            resetPasswordLoading
                        }
                        style={secondaryButtonStyle}
                    >
                        Cancel
                    </button>

                </div>

            </div>

        )}

    </SectionCard>

)}


                {/* =================================================
                    ADD / EDIT FORM
                ================================================= */}

                {showForm && (

                    <div style={modalOverlayStyle}>

                        <div style={modalStyle}>

                            <div
                                style={{
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "center",
                                    marginBottom: "20px"
                                }}
                            >

                                <h2
                                    style={{
                                        margin: 0,
                                        color: "#1e3a5f"
                                    }}
                                >
                                    {editingId
                                        ? `Edit ${formType}`
                                        : `Add ${formType}`}
                                </h2>


                                <button
                                    type="button"
                                    onClick={closeForm}
                                    style={closeButtonStyle}
                                >
                                    ×
                                </button>

                            </div>


                            <form onSubmit={submitForm}>

                                {/* =================================================
                                    STUDENT FORM
                                ================================================= */}

                                {formType === "student" && (

                                    <>

                                        <FormField
                                            label="Registration Number"
                                        >

                                            <input
                                                value={
                                                    studentRegistrationNumber
                                                }
                                                onChange={(e) =>
                                                    setStudentRegistrationNumber(
                                                        e.target.value
                                                    )
                                                }
                                                style={dropdownStyle}
                                                disabled={
                                                    Boolean(editingId)
                                                }
                                            />

                                        </FormField>


                                        <FormField
                                            label="Student Name"
                                        >

                                            <input
                                                value={studentName}
                                                onChange={(e) =>
                                                    setStudentName(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                            />

                                        </FormField>


                                        <FormField
                                            label="Email"
                                        >

                                            <input
                                                type="email"
                                                value={studentEmail}
                                                onChange={(e) =>
                                                    setStudentEmail(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                            />

                                        </FormField>


                                        {!editingId && (

                                            <FormField
                                                label="Password"
                                            >

                                                <input
                                                    type="password"
                                                    value={
                                                        studentPassword
                                                    }
                                                    onChange={(e) =>
                                                        setStudentPassword(
                                                            e.target.value
                                                        )
                                                    }
                                                    style={inputStyle}
                                                />

                                            </FormField>

                                        )}


                                        <FormField
                                            label="Academic Year"
                                        >

                                            <select
                                                value={studentYear}
                                                onChange={(e) => {

                                                    const value =
                                                        e.target.value;

                                                    setStudentYear(
                                                        value
                                                    );

                                                   if (value === "1") {
                                                       setStudentDegreeType("");
                                                       setStudentMajor("");
                                                       setStudentMinor("");
                                                     }

                                                    if (
                                                        value === "4"
                                                    ) {

                                                        setStudentMinor("");

                                                        if (
                                                            studentSemester ===
                                                            "2"
                                                        ) {

                                                            setStudentSemester(
                                                                "1"
                                                            );
                                                        }
                                                    }

                                                }}
                                                style={dropdownStyle}
                                            >

                                                <option value="">
                                                    Select Year
                                                </option>

                                                <option value="1">
                                                    Year 1
                                                </option>

                                                <option value="2">
                                                    Year 2
                                                </option>

                                                <option value="3">
                                                    Year 3
                                                </option>

                                                <option value="4">
                                                    Year 4
                                                </option>

                                            </select>

                                        </FormField>


                                        <FormField
                                            label="Semester"
                                        >

                                            <select
                                                value={studentSemester}
                                                onChange={(e) =>
                                                    setStudentSemester(
                                                        e.target.value
                                                    )
                                                }
                                                style={dropdownStyle}
                                            >

                                                <option value="">
                                                    Select Semester
                                                </option>

                                                <option value="1">
                                                    Semester 1
                                                </option>

                                                {studentYear !== "4" && (

                                                    <option value="2">
                                                        Semester 2
                                                    </option>

                                                )}

                                            </select>

                                        </FormField>

                                        {studentYear !== "1" && (
    <FormField
        label=""
    >
        <select
            value={studentDegreeType}
            onChange={(e) => {
                setStudentDegreeType(e.target.value);
            }}
            style={dropdownStyle}
        >
            <option value="">
                Select Degree Type
            </option>

            <option value="SPECIAL">
                Special Degree
            </option>

            <option value="GENERAL">
                General Degree
            </option>
        </select>
    </FormField>
)}




                                        {studentYear !== "1" && (

                                            <FormField
                                                label="Major Specialization"
                                            >

                                                <select
                                                    value={
                                                        studentMajor
                                                    }
                                                    onChange={(e) =>
                                                        setStudentMajor(
                                                            e.target.value
                                                        )
                                                    }
                                                    style={dropdownStyle}
                                                >

                                                    <option value="">
                                                        Select Major
                                                    </option>

                                                    {specializations.map(
                                                        (specialization) => (

                                                            <option
                                                                key={
                                                                    specialization.specializationId
                                                                }
                                                                value={
                                                                    specialization.specializationId
                                                                }
                                                            >
                                                                {
                                                                    specialization.specializationCode
                                                                }

                                                                {" - "}

                                                                {
                                                                    specialization.specializationName
                                                                }
                                                            </option>

                                                        )
                                                    )}

                                                </select>

                                            </FormField>

                                        )}


                                        {(studentYear === "2" ||
                                            studentYear === "3") && (

                                            <FormField
                                                label="Minor Specialization"
                                            >

                                                <select
                                                    value={
                                                        studentMinor
                                                    }
                                                    onChange={(e) =>
                                                        setStudentMinor(
                                                            e.target.value
                                                        )
                                                    }
                                                    style={dropdownStyle}
                                                >

                                                    <option value="">
                                                        Select Minor
                                                    </option>

                                                    {specializations
                                                        .filter(
                                                            (specialization) =>
                                                                String(
                                                                    specialization.specializationId
                                                                ) !==
                                                                String(
                                                                    studentMajor
                                                                )
                                                        )
                                                        .map(
                                                            (specialization) => (

                                                                <option
                                                                    key={
                                                                        specialization.specializationId
                                                                    }
                                                                    value={
                                                                        specialization.specializationId
                                                                    }
                                                                >
                                                                    {
                                                                        specialization.specializationCode
                                                                    }

                                                                    {" - "}

                                                                    {
                                                                        specialization.specializationName
                                                                    }
                                                                </option>

                                                            )
                                                        )}

                                                </select>

                                            </FormField>

                                        )}

                                    </>

                                )}


                                {/* =================================================
                                    LECTURER FORM
                                ================================================= */}

                                {formType === "lecturer" && (

                                    <>

                                        <FormField
                                            label="Lecturer Code"
                                        >

                                            <input
                                                value={lecturerCode}
                                                onChange={(e) =>
                                                    setLecturerCode(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                            />

                                        </FormField>


                                        <FormField
                                            label="Lecturer Name"
                                        >

                                            <input
                                                value={lecturerName}
                                                onChange={(e) =>
                                                    setLecturerName(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                            />

                                        </FormField>


                                        <FormField
                                            label="Email"
                                        >

                                            <input
                                                type="email"
                                                value={lecturerEmail}
                                                onChange={(e) =>
                                                    setLecturerEmail(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                            />

                                        </FormField>


                                        <FormField
                                            label="Department"
                                        >

                                            <select
                                                value={
                                                    lecturerDepartment
                                                }
                                                onChange={(e) =>
                                                    setLecturerDepartment(
                                                        e.target.value
                                                    )
                                                }
                                                style={dropdownStyle}
                                            >

                                                <option value="">
                                                    Select Department
                                                </option>

                                                {departments.map(
                                                    (department) => (

                                                        <option
                                                            key={
                                                                department.departmentId
                                                            }
                                                            value={
                                                                department.departmentId
                                                            }
                                                        >
                                                            {
                                                                department.departmentName ||
                                                                department.name
                                                            }
                                                        </option>

                                                    )
                                                )}

                                            </select>

                                        </FormField>

                                        <FormField
                                            label={
                                                editingId
                                                    ? "New Password (optional)"
                                                    : "Initial Password"
                                            }
                                        >
                                            <input
                                                type="password"
                                                value={lecturerPassword}
                                                onChange={(e) =>
                                                    setLecturerPassword(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                                placeholder={
                                                    editingId
                                                        ? "Leave blank to keep current password"
                                                        : "Enter initial login password"
                                                }
                                                autoComplete="new-password"
                                            />
                                            <small
                                                style={{
                                                    display: "block",
                                                    marginTop: "6px",
                                                    color: "#64748b"
                                                }}
                                            >
                                                {editingId
                                                    ? "Enter a password only if you want to reset this lecturer's password."
                                                    : "The lecturer will use this password for their first login."}
                                            </small>
                                        </FormField>

                                    </>

                                )}


                                {/* =================================================
                                    SUBJECT FORM
                                ================================================= */}

                                {formType === "registration" && (
                                <>
                                    <FormField label="Student">
                                        <select value={registrationStudent} onChange={(e) => setRegistrationStudent(e.target.value)} style={dropdownStyle}>
                                            <option value="">Select Student</option>
                                            {students.map((student) => (
                                                <option key={student.studentId} value={student.studentId}>
                                                    {student.registrationNumber} - {student.studentName}
                                                </option>
                                            ))}
                                        </select>
                                    </FormField>

                                    <FormField label="Subject">
                                        <select value={registrationSubject} onChange={(e) => setRegistrationSubject(e.target.value)} style={dropdownStyle}>
                                            <option value="">Select Subject</option>
                                            {subjects.map((subject) => (
                                                <option key={subject.subjectId} value={subject.subjectId}>
                                                    {subject.subjectCode} - {subject.subjectName}
                                                </option>
                                            ))}
                                        </select>
                                    </FormField>

                                    <FormField label="Academic Year">
                                        <select value={registrationYear} onChange={(e) => setRegistrationYear(e.target.value)} style={dropdownStyle}>
                                            <option value="">Select Academic Year</option>
                                            <option value="1">Year 1</option>
                                            <option value="2">Year 2</option>
                                            <option value="3">Year 3</option>
                                            <option value="4">Year 4</option>
                                        </select>
                                    </FormField>

                                    <FormField label="Semester">
                                        <select value={registrationSemester} onChange={(e) => setRegistrationSemester(e.target.value)} style={dropdownStyle}>
                                            <option value="">Select Semester</option>
                                            <option value="1">Semester 1</option>
                                            <option value="2">Semester 2</option>
                                        </select>
                                    </FormField>
                                </>
                            )}

                            {formType === "subject" && (

                                    <>

                                        <FormField
                                            label="Subject Code"
                                        >

                                            <input
                                                value={subjectCode}
                                                onChange={(e) =>
                                                    setSubjectCode(
                                                        e.target.value
                                                    )
                                                }
                                                style={dropdownStyle}
                                            />

                                        </FormField>


                                        <FormField
                                            label="Subject Name"
                                        >

                                            <input
                                                value={subjectName}
                                                onChange={(e) =>
                                                    setSubjectName(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                            />

                                        </FormField>


                                        <FormField
                                            label="Department"
                                        >

                                            <select
                                                value={
                                                    subjectDepartment
                                                }
                                                onChange={(e) =>
                                                    setSubjectDepartment(
                                                        e.target.value
                                                    )
                                                }
                                                style={dropdownStyle}
                                            >

                                                <option value="">
                                                    Select Department
                                                </option>

                                                {departments.map(
                                                    (department) => (

                                                        <option
                                                            key={
                                                                department.departmentId
                                                            }
                                                            value={
                                                                department.departmentId
                                                            }
                                                        >
                                                            {
                                                                department.departmentName ||
                                                                department.name
                                                            }
                                                        </option>

                                                    )
                                                )}

                                            </select>

                                        </FormField>


                                        <FormField
                                            label="Specialization"
                                        >

                                            <select
                                                value={
                                                    subjectSpecialization
                                                }
                                                onChange={(e) =>
                                                    setSubjectSpecialization(
                                                        e.target.value
                                                    )
                                                }
                                                style={dropdownStyle}
                                            >

                                                <option value="">
                                                    Select Specialization
                                                </option>

                                                {specializations.map(
                                                    (specialization) => (

                                                        <option
                                                            key={
                                                                specialization.specializationId
                                                            }
                                                            value={
                                                                specialization.specializationId
                                                            }
                                                        >
                                                            {
                                                                specialization.specializationCode
                                                            }

                                                            {" - "}

                                                            {
                                                                specialization.specializationName
                                                            }
                                                        </option>

                                                    )
                                                )}

                                            </select>

                                        </FormField>


                                        <FormField
                                            label="Academic Year"
                                        >

                                            <select
                                                value={subjectYear}
                                                onChange={(e) => {

                                                    const value =
                                                        e.target.value;

                                                    setSubjectYear(
                                                        value
                                                    );

                                                    if (
                                                        value === "4" &&
                                                        subjectSemester ===
                                                        "2"
                                                    ) {

                                                        setSubjectSemester(
                                                            "1"
                                                        );
                                                    }

                                                }}
                                                style={dropdownStyle}
                                            >

                                                <option value="">
                                                    Select Year
                                                </option>

                                                <option value="1">
                                                    Year 1
                                                </option>

                                                <option value="2">
                                                    Year 2
                                                </option>

                                                <option value="3">
                                                    Year 3
                                                </option>

                                                <option value="4">
                                                    Year 4
                                                </option>

                                            </select>

                                        </FormField>


                                        <FormField
                                            label="Semester"
                                        >

                                            <select
                                                value={subjectSemester}
                                                onChange={(e) =>
                                                    setSubjectSemester(
                                                        e.target.value
                                                    )
                                                }
                                                style={dropdownStyle}
                                            >

                                                <option value="">
                                                    Select Semester
                                                </option>

                                                <option value="1">
                                                    Semester 1
                                                </option>

                                                {subjectYear !== "4" && (

                                                    <option value="2">
                                                        Semester 2
                                                    </option>

                                                )}

                                            </select>

                                        </FormField>


                                        <FormField
                                            label="Credits"
                                        >

                                            <input
                                                type="number"
                                                min="1"
                                                value={
                                                    subjectCredits
                                                }
                                                onChange={(e) =>
                                                    setSubjectCredits(
                                                        e.target.value
                                                    )
                                                }
                                                style={inputStyle}
                                            />

                                        </FormField>


                                        <label
                                            style={{
                                                display: "flex",
                                                alignItems: "center",
                                                gap: "8px",
                                                marginBottom: "20px",
                                                fontWeight: "600"
                                            }}
                                        >

                                            <input
                                                type="checkbox"
                                                checked={
                                                    subjectCompulsory
                                                }
                                                onChange={(e) =>
                                                    setSubjectCompulsory(
                                                        e.target.checked
                                                    )
                                                }
                                            />

                                            Compulsory Subject

                                        </label>

                                    </>

                                )}


                                {/* BUTTONS */}

                                <div
                                    style={{
                                        display: "flex",
                                        gap: "10px",
                                        marginTop: "25px"
                                    }}
                                >

                                    <button
                                        type="submit"
                                        disabled={
                                            formLoading
                                        }
                                        style={primaryButtonStyle}
                                    >

                                        {formLoading
                                            ? "Saving..."
                                            : editingId
                                                ? "Update"
                                                : "Save"}

                                    </button>


                                    <button
                                        type="button"
                                        onClick={closeForm}
                                        style={secondaryButtonStyle}
                                    >
                                        Cancel
                                    </button>

                                </div>

                            </form>

                        </div>

                    </div>

                )}

                       </main>

            <ConfirmDialog
    open={confirmDialog.open}
    message={confirmDialog.message}
    onConfirm={handleConfirmAction}
    onCancel={closeConfirmDialog}
/>

            <StatusDialog
                type={success ? "success" : "error"}
                message={success || error}
                onClose={() => {
                    setSuccess("");
                    setError("");
                }}
            />
            {requestResponseDialog.open && (
    <div className="request-dialog-overlay">
        <div className="request-dialog">

            <div className="request-dialog-icon">
                {requestResponseDialog.action === "approve" ? "✓" : "!"}
            </div>

            <h2>
                {requestResponseDialog.action === "approve"
                    ? "Approve Change Request"
                    : "Reject Change Request"}
            </h2>

            <p>
                {requestResponseDialog.action === "approve"
                    ? "You can enter an optional response to the lecturer."
                    : "Please enter the reason for rejecting this request."}
            </p>

            <textarea
                className="request-response-textarea"
                value={requestResponseDialog.responseText}
                onChange={(e) =>
                    setRequestResponseDialog((prev) => ({
                        ...prev,
                        responseText: e.target.value
                    }))
                }
                placeholder="Enter your response here..."
                rows={4}
            />

            <div className="request-dialog-buttons">

                <button
                    type="button"
                    className="request-dialog-cancel"
                    onClick={closeRequestResponseDialog}
                    disabled={processingRequest}
                >
                    Cancel
                </button>

                <button
                    type="button"
                    className={
                        requestResponseDialog.action === "approve"
                            ? "request-dialog-approve"
                            : "request-dialog-reject"
                    }
                    onClick={processChangeRequest}
                    disabled={processingRequest}
                >
                    {processingRequest
                        ? "Processing..."
                        : requestResponseDialog.action === "approve"
                            ? "Confirm Approval"
                            : "Confirm Rejection"}
                </button>

            </div>
        </div>
    </div>
)}

        </div>
    );
}


// =========================================================
// FORM FIELD
// =========================================================

function FormField({
    label,
    children
}) {

    return (

        <div
            style={{
                marginBottom: "18px"
            }}
        >

            <label style={labelStyle}>
                {label}
            </label>

            {children}

        </div>
    );
}


// =========================================================
// ACTION BUTTONS
// =========================================================

function ActionButtons({
    onEdit,
    onDelete
}) {

    return (

        <div
            style={{
                display: "flex",
                gap: "7px",
                flexWrap: "wrap"
            }}
        >

            <button
                type="button"
                onClick={onEdit}
                style={editButtonStyle}
            >
                Edit
            </button>


            <button
                type="button"
                onClick={onDelete}
                style={deleteButtonStyle}
            >
                Delete
            </button>


        </div>
    );
}


// =========================================================
// SIDEBAR BUTTON
// =========================================================

function SidebarButton({
    children,
    active,
    onClick
}) {

    return (

        <button
            type="button"
            onClick={onClick}
            style={{
                display: "block",
                width: "100%",
                padding: "12px 13px",
                marginBottom: "6px",
                border: "none",
                borderRadius: "7px",
                textAlign: "left",
                cursor: "pointer",
                backgroundColor:
                    active
                        ? "#2563eb"
                        : "transparent",
                color: "#ffffff",
                fontWeight:
                    active
                        ? "600"
                        : "500",
                fontSize: "14px"
            }}
        >
            {children}
        </button>
    );
}


// =========================================================
// DASHBOARD CARD
// =========================================================

function DashboardCard({
    title,
    value,
    onClick
}) {

    return (

        <button
            type="button"
            onClick={onClick}
            style={{
                backgroundColor: "#ffffff",
                border: "none",
                borderRadius: "12px",
                padding: "22px",
                textAlign: "left",
                cursor: "pointer",
                boxShadow:
                    "0 2px 8px rgba(0,0,0,0.08)"
            }}
        >

            <p
                style={{
                    margin: 0,
                    color: "#334e68",
                    fontSize: "14px",
                    fontWeight: "600"
                }}
            >
                {title}
            </p>

            <h2
                style={{
                    margin: "10px 0 0 0",
                    color: "#1e3a5f",
                    fontSize: "30px"
                }}
            >
                {value}
            </h2>

        </button>
    );
}


// =========================================================
// ADMIN ACTION
// =========================================================

function AdminAction({
    title,
    onClick
}) {

    return (

        <button
            type="button"
            onClick={onClick}
            style={{
                padding: "18px",
                border: "1px solid #d7e0ea",
                borderRadius: "8px",
                backgroundColor: "#ffffff",
                textAlign: "left",
                cursor: "pointer",
                color: "#1e3a5f",
                fontSize: "14px",
                fontWeight: "600"
            }}
        >
            {title}
        </button>
    );
}


// =========================================================
// SECTION CARD
// =========================================================

function SectionCard({
    title,
    children
}) {

    return (

        <div
            style={{
                backgroundColor: "#ffffff",
                borderRadius: "12px",
                padding: "25px",
                boxShadow:
                    "0 2px 8px rgba(0,0,0,0.08)"
            }}
        >

            <h2
                style={sectionTitleStyle}
            >
                {title}
            </h2>

            {children}

        </div>
    );
}


// =========================================================
// EMPTY MESSAGE
// =========================================================

function EmptyMessage({
    message
}) {

    return (

        <div
            style={{
                padding: "35px",
                textAlign: "center",
                color: "#334e68",
                backgroundColor: "#f8fafc",
                border: "1px solid #d7e0ea",
                borderRadius: "8px"
            }}
        >
            {message}

            
        </div>
    );
}


// =========================================================
// STYLES
// =========================================================

const sectionTitleStyle = {

    marginTop: 0,
    marginBottom: "22px",
    color: "#1e3a5f",
    fontSize: "21px",
    fontWeight: "700"
};


const toolbarActionsStyle = {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    flexWrap: "wrap"
};


const csvImportButtonStyle = {
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "11px 18px",
    border: "1px solid #2563eb",
    borderRadius: "7px",
    backgroundColor: "#eff6ff",
    color: "#1d4ed8",
    cursor: "pointer",
    fontWeight: "600",
    fontSize: "14px",
    boxSizing: "border-box"
};


const hiddenFileInputStyle = {
    display: "none"
};


const sectionToolbarStyle = {

    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "15px",
    marginBottom: "22px",
    flexWrap: "wrap"
};


const helpTextStyle = {

    margin:
        "5px 0 0 0",

    color:
        "#64748b",

    fontSize:
        "13px"
};


const scrollTableStyle = {

    overflowX: "auto"
};


const tableStyle = {

    width: "100%",
    borderCollapse: "collapse",
    fontFamily: "Arial, sans-serif",
    fontSize: "14px"
};


const headerStyle = {

    padding: "14px 16px",
    textAlign: "center",
    borderBottom: "2px solid #b8c7d9",
    backgroundColor: "#eaf0f6",
    color: "#1e3a5f",
    fontSize: "14px",
    fontWeight: "700",
    whiteSpace: "nowrap"
};


const cellStyle = {

    padding: "14px 16px",
    textAlign: "center",
    borderBottom: "1px solid #d7e0ea",
    fontSize: "14px",
    fontFamily: "Arial, sans-serif",
    color: "#243b53",
    verticalAlign: "middle"
};


const primaryButtonStyle = {

    padding: "11px 18px",
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#2563eb",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600",
    fontSize: "14px"
};


const secondaryButtonStyle = {

    padding: "11px 20px",
    border: "1px solid #cbd5e1",
    borderRadius: "7px",
    backgroundColor: "#ffffff",
    color: "#334155",
    cursor: "pointer",
    fontWeight: "600"
};


const editButtonStyle = {

    padding: "7px 12px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#f59e0b",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600"
};


const deleteButtonStyle = {

    padding: "7px 12px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#dc2626",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600"
};


const errorStyle = {

    backgroundColor: "#fee2e2",
    color: "#991b1b",
    padding: "14px",
    borderRadius: "8px",
    marginBottom: "20px",
    border: "1px solid #fecaca",
    fontSize: "14px",
    fontWeight: "600"
};


const successStyle = {

    backgroundColor: "#dcfce7",
    color: "#166534",
    padding: "14px",
    borderRadius: "8px",
    marginBottom: "20px",
    border: "1px solid #bbf7d0",
    fontSize: "14px",
    fontWeight: "600"
};


const labelStyle = {

    display: "block",
    marginBottom: "7px",
    fontWeight: "600",
    fontSize: "14px",
    color: "#1e293b"
};


const dropdownStyle = {

    width: "100%",
    boxSizing: "border-box",
    padding: "11px 12px",
    border: "1px solid #cbd5e1",
    borderRadius: "7px",
    fontSize: "14px",
    backgroundColor: "#ffffff",
    color: "#111827",
    appearance: "auto",
    WebkitAppearance: "menulist"
};


const inputStyle = {

    width: "100%",
    boxSizing: "border-box",
    padding: "11px 12px",
    border: "1px solid #cbd5e1",
    borderRadius: "7px",
    fontSize: "14px",
    backgroundColor: "#ffffff",
    color: "#111827"
};


const modalOverlayStyle = {

    position: "fixed",
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: "rgba(15,23,42,0.45)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    padding: "20px",
    zIndex: 1000,
    overflowY: "auto"
};


const modalStyle = {

    width: "100%",
    maxWidth: "650px",
    maxHeight: "90vh",
    overflowY: "auto",
    backgroundColor: "#ffffff",
    borderRadius: "12px",
    padding: "28px",
    boxSizing: "border-box",
    boxShadow:
        "0 10px 30px rgba(0,0,0,0.2)"
};


const closeButtonStyle = {

    border: "none",
    backgroundColor: "transparent",
    fontSize: "28px",
    cursor: "pointer",
    color: "#475569"
};


const filterBarStyle = {
    display: "flex",
    alignItems: "flex-end",
    gap: "12px",
    flexWrap: "wrap",
    padding: "15px",
    marginBottom: "8px",
    backgroundColor: "#eef6ff",
    border: "1px solid #93c5fd",
    borderRadius: "8px"
};

const filterFieldStyle = {
    minWidth: "190px"
};

const filterLabelStyle = {
    display: "block",
    marginBottom: "6px",
    fontSize: "13px",
    fontWeight: "600",
    color: "#334155"
};

const filterSearchInputStyle = {
    width: "100%",
    minWidth: "190px",
    boxSizing: "border-box",
    padding: "10px 12px",
    border: "2px solid #60a5fa",
    borderRadius: "7px",
    backgroundColor: "#eff6ff",
    color: "#1e3a5f",
    fontSize: "14px",
    outline: "none"
};

const filterInputStyle = {
    width: "100%",
    minWidth: "190px",
    boxSizing: "border-box",
    padding: "10px 12px",
    border: "1px solid #cbd5e1",
    borderRadius: "7px",
    backgroundColor: "#ffffff",
    color: "#243b53",
    fontSize: "14px",
    outline: "none"
};

const filterCountStyle = {
    margin: "8px 0 16px",
    color: "#64748b",
    fontSize: "13px"
};

const actionGroupStyle = {
    display: "flex",
    gap: "7px",
    flexWrap: "wrap"
};

const approveButtonStyle = {
    padding: "7px 11px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#16a34a",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600"
};

const rejectButtonStyle = {
    padding: "7px 11px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#f59e0b",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600"
};

const accountEnabledBadgeStyle = {
    display: "inline-block",
    padding: "5px 9px",
    borderRadius: "999px",
    fontSize: "12px",
    fontWeight: "700",
    backgroundColor: "#dcfce7",
    color: "#166534"
};


const accountDisabledBadgeStyle = {
    display: "inline-block",
    padding: "5px 9px",
    borderRadius: "999px",
    fontSize: "12px",
    fontWeight: "700",
    backgroundColor: "#fee2e2",
    color: "#991b1b"
};

const editRoleButtonStyle = {
    minWidth: "78px",
    padding: "7px 10px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#334e68",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600",
    whiteSpace: "nowrap"
};


const disableAccountButtonStyle = {
    padding: "7px 12px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#dc2626",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600"
};


const enableAccountButtonStyle = {
    padding: "7px 12px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#16a34a",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600"
};


const resetPasswordButtonStyle = {
    padding: "7px 12px",
    border: "none",
    borderRadius: "6px",
    backgroundColor: "#2563eb",
    color: "#ffffff",
    cursor: "pointer",
    fontWeight: "600"
};


const statusBadgeStyle = (status) => ({
    display: "inline-block",
    padding: "5px 9px",
    borderRadius: "999px",
    fontSize: "12px",
    fontWeight: "700",
    backgroundColor: String(status).toUpperCase() === "APPROVED"
        ? "#dcfce7"
        : String(status).toUpperCase() === "REJECTED"
            ? "#fee2e2"
            : "#fef3c7",
    color: String(status).toUpperCase() === "APPROVED"
        ? "#166534"
        : String(status).toUpperCase() === "REJECTED"
            ? "#991b1b"
            : "#92400e"
});

export default AdminDashboard;