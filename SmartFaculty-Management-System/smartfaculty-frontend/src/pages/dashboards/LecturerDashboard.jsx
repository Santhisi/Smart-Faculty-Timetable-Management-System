import { useEffect, useState } from "react";
import axios from "axios";
import LecturerChangePassword from "../LecturerChangePassword";

import StatusDialog from "../../components/StatusDialog";

function LecturerDashboard() {

    // =====================================================
    // STATES
    // =====================================================

    const [lecturer, setLecturer] = useState(null);
    const [timetables, setTimetables] = useState([]);
    const [allocations, setAllocations] = useState([]);
    const [changeRequests, setChangeRequests] = useState([]);
    const [lecturerNotifications, setLecturerNotifications] = useState([]);
    const [showNotifications, setShowNotifications] = useState(false);
    const [notificationLoading, setNotificationLoading] = useState(false);
    const [notificationError, setNotificationError] = useState("");

    const [loading, setLoading] = useState(true);
    const [allocationLoading, setAllocationLoading] = useState(false);
    const [changeRequestLoading, setChangeRequestLoading] = useState(false);

    const [error, setError] = useState("");
    const [allocationError, setAllocationError] = useState("");
    const [changeRequestError, setChangeRequestError] = useState("");

    // Request form
    const [showRequestForm, setShowRequestForm] = useState(false);
    const [selectedTimetable, setSelectedTimetable] = useState(null);

    const [reason, setReason] = useState("");
    const [requestedDay, setRequestedDay] = useState("");
    const [requestedStartTime, setRequestedStartTime] = useState("");
    const [requestedEndTime, setRequestedEndTime] = useState("");
    const [requestedClassroom, setRequestedClassroom] = useState("");

    const [submitting, setSubmitting] = useState(false);
    const [success, setSuccess] = useState("");

    const [showEditRequestForm, setShowEditRequestForm] = useState(false);
const [editingRequestId, setEditingRequestId] = useState(null);

const [editRequestReason, setEditRequestReason] = useState("");
const [editRequestedDay, setEditRequestedDay] = useState("");
const [editRequestedStartTime, setEditRequestedStartTime] = useState("");
const [editRequestedEndTime, setEditRequestedEndTime] = useState("");
const [editRequestedClassroom, setEditRequestedClassroom] = useState("");

const [editRequestSaving, setEditRequestSaving] = useState(false);
const [requestActionMessage, setRequestActionMessage] = useState("");

const [showDeleteDialog, setShowDeleteDialog] = useState(false);
const [selectedRequestId, setSelectedRequestId] = useState(null);


    // =====================================================
    // LOGIN USERNAME
    // =====================================================

    const username =
        localStorage.getItem("username") || "Lecturer";

    const token =
        localStorage.getItem("token");


    // =====================================================
    // LOAD DASHBOARD
    // =====================================================

    useEffect(() => {
        loadDashboard();
    }, []);


    const loadDashboard = async () => {

        setLoading(true);
        setError("");

        try {

            // ---------------------------------------------
            // 1. GET LOGGED-IN LECTURER PROFILE
            // ---------------------------------------------
            // IMPORTANT:
            // The backend security rules no longer allow a normal
            // lecturer to request the full /api/lecturers list.
            // Use the protected /api/lecturers/me endpoint instead.

            const lecturerResponse = await axios.get(
                "http://localhost:8080/api/lecturers/me",
                {
                    headers: token
                        ? {
                            Authorization: `Bearer ${token}`
                        }
                        : {}
                }
            );

            const currentLecturer =
                lecturerResponse.data || null;

            console.log(
                "Logged-in Lecturer:",
                currentLecturer
            );


            if (!currentLecturer) {

                setError(
                    "Lecturer information could not be found."
                );

                setLoading(false);
                return;
            }


            console.log(
                "Logged-in Lecturer:",
                currentLecturer
            );


            setLecturer(currentLecturer);


            // ---------------------------------------------
            // 3. GET LECTURER ID
            // ---------------------------------------------

            const lecturerId =
                currentLecturer.lecturerId ||
                currentLecturer.lecturer_id;


            if (!lecturerId) {

                setError(
                    "Lecturer ID could not be found."
                );

                setLoading(false);
                return;
            }


            console.log(
                "Lecturer ID:",
                lecturerId
            );


            // ---------------------------------------------
            // 4. LOAD ALL DATA
            // ---------------------------------------------

            await Promise.all([
                loadTimetable(lecturerId),
                loadAllocations(lecturerId),
                loadChangeRequests(lecturerId),
                loadLecturerNotifications(lecturerId)
            ]);


        } catch (err) {

            console.error(
                "Dashboard loading error:",
                err
            );

            console.error(
                "Server response:",
                err.response?.data
            );

            setError(
                "Unable to load lecturer information."
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // LOAD LECTURER TIMETABLE
    // =====================================================

    const loadTimetable = async (lecturerId) => {

        try {

            const response = await axios.get(
                `http://localhost:8080/api/timetables/lecturer/${lecturerId}`,
                {
                    headers: token
                        ? {
                            Authorization: `Bearer ${token}`
                        }
                        : {}
                }
            );

            console.log(
                "Lecturer timetable:",
                response.data
            );

            setTimetables(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Timetable loading error:",
                err
            );

            console.error(
                "Timetable response:",
                err.response?.data
            );

            setTimetables([]);
        }
    };


    // =====================================================
    // LOAD TEACHING ALLOCATIONS
    // =====================================================

    const loadAllocations = async (lecturerId) => {

        try {

            setAllocationLoading(true);
            setAllocationError("");

            const response = await axios.get(
                `http://localhost:8080/api/lecturer-allocations/lecturer/${lecturerId}`,
                {
                    headers: token
                        ? {
                            Authorization: `Bearer ${token}`
                        }
                        : {}
                }
            );

            console.log(
                "Teaching allocations:",
                response.data
            );

            setAllocations(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Allocation loading error:",
                err
            );

            setAllocations([]);

            setAllocationError(
                "Unable to load teaching allocations."
            );

        } finally {

            setAllocationLoading(false);
        }
    };


    // =====================================================
    // LOAD CHANGE REQUESTS
    // =====================================================

    const loadChangeRequests = async (lecturerId) => {

        try {

            setChangeRequestLoading(true);
            setChangeRequestError("");

            const response = await axios.get(
                `http://localhost:8080/api/timetable-change-requests/lecturer/${lecturerId}`,
                {
                    headers: token
                        ? {
                            Authorization: `Bearer ${token}`
                        }
                        : {}
                }
            );

            console.log(
                "My change requests:",
                response.data
            );
            console.log("First request:", response.data?.[0]);
            console.log("Request status:", response.data?.[0]?.status);
            console.log("Request ID:", response.data?.[0]?.requestId);

            setChangeRequests(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Change request loading error:",
                err
            );

            setChangeRequests([]);

            setChangeRequestError(
                "Unable to load timetable change requests."
            );

        } finally {

            setChangeRequestLoading(false);
        }
    };

    // =====================================================
// FORMAT TIME FOR EDIT FORM
// =====================================================

const getTimeInputValue = (time) => {
    if (!time) return "";

    // Handles values such as 09:30:00 or 09:30
    return String(time).substring(0, 5);
};


// =====================================================
// OPEN EDIT REQUEST FORM
// =====================================================

const openEditRequestForm = (request) => {

    if (String(request?.status || "PENDING").toUpperCase() !== "PENDING") {
        return;
    }

    setEditingRequestId(request.requestId);
    setEditRequestReason(request.reason || "");
    setEditRequestedDay(request.requestedDay || "");
    setEditRequestedStartTime(
        getTimeInputValue(request.requestedStartTime)
    );
    setEditRequestedEndTime(
        getTimeInputValue(request.requestedEndTime)
    );
    setEditRequestedClassroom(request.requestedClassroom || "");

    setRequestActionMessage("");
    setShowEditRequestForm(true);
};


// =====================================================
// CLOSE EDIT REQUEST FORM
// =====================================================

const closeEditRequestForm = () => {
    setShowEditRequestForm(false);
    setEditingRequestId(null);
};


// =====================================================
// UPDATE CHANGE REQUEST
// =====================================================

const updateChangeRequest = async () => {
    if (!editRequestReason.trim()) {
        setRequestActionMessage("Please enter a reason.");
        return;
    }

    if (!editingRequestId) {
        setRequestActionMessage("Change request ID could not be found.");
        return;
    }

    if (
        editRequestedStartTime &&
        editRequestedEndTime &&
        editRequestedStartTime >= editRequestedEndTime
    ) {
        setRequestActionMessage("End time must be later than start time.");
        return;
    }

    const lecturerId =
        lecturer?.lecturerId ||
        lecturer?.lecturer_id;

    if (!lecturerId) {
        setRequestActionMessage("Lecturer ID could not be found.");
        return;
    }

    try {
        setEditRequestSaving(true);
        setRequestActionMessage("");

        const updatedRequest = {
            reason: editRequestReason.trim(),
            requestedDay: editRequestedDay || null,
            requestedStartTime: editRequestedStartTime || null,
            requestedEndTime: editRequestedEndTime || null,
            requestedClassroom: editRequestedClassroom.trim() || null
        };

        await axios.put(
            `http://localhost:8080/api/timetable-change-requests/${editingRequestId}`,
            updatedRequest,
            {
                headers: token
                    ? { Authorization: `Bearer ${token}` }
                    : {}
            }
        );

        closeEditRequestForm();
        await loadChangeRequests(lecturerId);
        setRequestActionMessage("Request updated successfully.");

    } catch (err) {
        console.error("Update request error:", err);
        console.error("Server response:", err.response?.data);

        setRequestActionMessage(
            err.response?.data?.message ||
            "Unable to update the change request."
        );
    } finally {
        setEditRequestSaving(false);
    }
};


// Open delete confirmation dialog
// =====================================================
// DELETE CHANGE REQUEST
// =====================================================

// Open confirmation dialog
const handleDeleteClick = (requestId) => {
    setSelectedRequestId(requestId);
    setShowDeleteDialog(true);
};

// Close confirmation dialog
const closeDeleteDialog = () => {
    setShowDeleteDialog(false);
    setSelectedRequestId(null);
};

// Confirm and delete request
const confirmDeleteRequest = async () => {

    if (!selectedRequestId) {
        return;
    }

    const lecturerId =
        lecturer?.lecturerId ||
        lecturer?.lecturer_id;

    try {

        await axios.delete(
            `http://localhost:8080/api/timetable-change-requests/${selectedRequestId}`,
            {
                headers: token
                    ? {
                        Authorization: `Bearer ${token}`
                    }
                    : {}
            }
        );

        closeDeleteDialog();

        setRequestActionMessage(
            "Timetable change request deleted successfully."
        );

        await loadChangeRequests(lecturerId);

    } catch (error) {

        console.error(
            "Error deleting request:",
            error
        );

        console.error(
            "Server response:",
            error.response?.data
        );

        setRequestActionMessage(
            error.response?.data?.message ||
            error.response?.data ||
            "Failed to delete timetable change request."
        );
    }
};

    // =====================================================
    // LOAD LECTURER NOTIFICATIONS
    // =====================================================

    const loadLecturerNotifications = async (lecturerId) => {

        try {

            setNotificationLoading(true);
            setNotificationError("");

            const response = await axios.get(
                `http://localhost:8080/api/lecturer-notifications/lecturer/${lecturerId}`,
                {
                    headers: token
                        ? {
                            Authorization: `Bearer ${token}`
                        }
                        : {}
                }
            );

            console.log(
                "Lecturer notifications:",
                response.data
            );

            setLecturerNotifications(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Lecturer notification loading error:",
                err
            );

            setLecturerNotifications([]);

            setNotificationError(
                "Unable to load notifications."
            );

        } finally {

            setNotificationLoading(false);
        }
    };


    // =====================================================
    // GET NOTIFICATION ID
    // =====================================================

    const getNotificationId = (notification) => {

        return (
            notification?.notificationId ||
            notification?.lecturerNotificationId ||
            notification?.lecturer_notification_id ||
            notification?.id
        );
    };


    // =====================================================
    // FORMAT NOTIFICATION DATE/TIME
    // =====================================================

    const formatNotificationDateTime = (dateTime) => {

        if (!dateTime) {
            return "-";
        }

        const date = new Date(dateTime);

        if (Number.isNaN(date.getTime())) {
            return String(dateTime);
        }

        return date.toLocaleString();
    };


    // =====================================================
    // MARK NOTIFICATION AS READ
    // =====================================================

    const markNotificationAsRead = async (notification) => {

        const notificationId =
            getNotificationId(notification);

        if (!notificationId || notification?.isRead) {
            return;
        }

        try {

            await axios.put(
                `http://localhost:8080/api/lecturer-notifications/${notificationId}/read`,
                {},
                {
                    headers: token
                        ? {
                            Authorization: `Bearer ${token}`
                        }
                        : {}
                }
            );

            setLecturerNotifications((previous) =>
                previous.map((item) =>
                    getNotificationId(item) === notificationId
                        ? { ...item, isRead: true }
                        : item
                )
            );

        } catch (err) {

            console.error(
                "Mark notification as read error:",
                err
            );

            alert(
                "Unable to mark the notification as read."
            );
        }
    };


    // =====================================================
    // UNREAD NOTIFICATION COUNT
    // =====================================================

    const unreadNotificationCount =
        lecturerNotifications.filter(
            (notification) =>
                notification?.isRead !== true
        ).length;


    // =====================================================
    // GET SUBJECT CODE
    // =====================================================

    const getSubjectCode = (item) => {

        return (
            item?.lecturerAllocation?.subject?.subjectCode ||
            item?.lecturerAllocation?.subject?.subject_code ||
            item?.subject?.subjectCode ||
            item?.subject?.subject_code ||
            item?.subjectCode ||
            "-"
        );
    };


    // =====================================================
    // GET YEAR
    // =====================================================

    const getYear = (item) => {

        const value =
            item?.yearLevel ??
            item?.academicYear ??
            item?.year ??
            item?.lecturerAllocation?.academicYear ??
            item?.lecturerAllocation?.yearLevel;

        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {
            return "-";
        }

        if (typeof value === "number") {
            return `Year ${value}`;
        }

        const text = String(value);

        if (text.includes("YEAR_")) {
            return text.replace("YEAR_", "Year ");
        }

        if (
            text.toLowerCase().startsWith("year")
        ) {
            return text;
        }

        return text;
    };


    // =====================================================
    // GET SEMESTER
    // =====================================================

    const getSemester = (item) => {

        const value =
            item?.semester ??
            item?.lecturerAllocation?.semester;

        if (
            value === null ||
            value === undefined ||
            value === ""
        ) {
            return "-";
        }

        if (typeof value === "number") {
            return `Semester ${value}`;
        }

        const text = String(value);

        if (text.includes("SEMESTER_")) {
            return text.replace(
                "SEMESTER_",
                "Semester "
            );
        }

        if (
            text.toLowerCase().startsWith("semester")
        ) {
            return text;
        }

        return text;
    };


    // =====================================================
    // GET DAY
    // =====================================================

    const getDay = (timetable) => {

        return (
            timetable?.timeSlot?.day ||
            timetable?.day ||
            "-"
        );
    };


    // =====================================================
    // FORMAT TIME
    // =====================================================

    const formatTime = (time) => {

        if (!time) {
            return "-";
        }

        const text = String(time);

        return text.length >= 5
            ? text.substring(0, 5)
            : text;
    };


    // =====================================================
    // GET START TIME
    // =====================================================

    const getStartTime = (timetable) => {

        return formatTime(
            timetable?.timeSlot?.startTime ||
            timetable?.startTime
        );
    };


    // =====================================================
    // GET END TIME
    // =====================================================

    const getEndTime = (timetable) => {

        return formatTime(
            timetable?.timeSlot?.endTime ||
            timetable?.endTime
        );
    };


    // =====================================================
    // GET CLASSROOM
    // =====================================================

    const getClassroom = (timetable) => {

        return (
            timetable?.classroom?.roomCode ||
            timetable?.classroom?.room_code ||
            timetable?.roomCode ||
            timetable?.room_code ||
            "-"
        );
    };


    // =====================================================
    // GET REQUEST SUBJECT
    // =====================================================

    const getRequestSubject = (request) => {

        return (
            request?.timetable
                ?.lecturerAllocation
                ?.subject
                ?.subjectCode ||

            request?.timetable
                ?.lecturerAllocation
                ?.subject
                ?.subject_code ||

            request?.timetable
                ?.subject
                ?.subjectCode ||

            request?.timetable
                ?.subject
                ?.subject_code ||

            request?.subject
                ?.subjectCode ||

            request?.subjectCode ||

            "-"
        );
    };


    // =====================================================
    // FORMAT REQUEST DATE/TIME
    // =====================================================

    const formatRequestDateTime = (dateTime) => {

        if (!dateTime) {
            return "-";
        }

        const date = new Date(dateTime);

        if (Number.isNaN(date.getTime())) {
            return String(dateTime);
        }

        return date.toLocaleString();
    };


    // =====================================================
    // GET DEPARTMENT NAME
    // =====================================================

    const getDepartmentName = () => {

        return (
            lecturer?.department?.departmentName ||
            lecturer?.department?.department_name ||
            lecturer?.departmentName ||
            lecturer?.department_name ||
            "-"
        );
    };


    // =====================================================
    // OPEN REQUEST FORM
    // =====================================================

    const openRequestForm = (timetable) => {

        setSelectedTimetable(timetable);

        setReason("");
        setRequestedDay("");
        setRequestedStartTime("");
        setRequestedEndTime("");

        const currentClassroom =
            getClassroom(timetable);

        setRequestedClassroom(
            currentClassroom === "-"
                ? ""
                : currentClassroom
        );

        setShowRequestForm(true);

        setTimeout(() => {

            window.scrollTo({
                top: document.body.scrollHeight,
                behavior: "smooth"
            });

        }, 100);
    };


    // =====================================================
    // CLOSE REQUEST FORM
    // =====================================================

    const closeRequestForm = () => {

        setShowRequestForm(false);
        setSelectedTimetable(null);

        setReason("");
        setRequestedDay("");
        setRequestedStartTime("");
        setRequestedEndTime("");
        setRequestedClassroom("");
    };


    // =====================================================
    // SUBMIT REQUEST
    // =====================================================

    const submitRequest = async () => {

        if (!selectedTimetable) {
            alert("Please select a timetable.");
            return;
        }

        if (!reason.trim()) {
            alert("Please enter a reason.");
            return;
        }

        const lecturerId =
            lecturer?.lecturerId ||
            lecturer?.lecturer_id;

        const timetableId =
            selectedTimetable?.timetableId ||
            selectedTimetable?.timetable_id;

        if (!lecturerId) {
            alert("Lecturer ID could not be found.");
            return;
        }

        if (!timetableId) {
            alert("Timetable ID could not be found.");
            return;
        }

        try {

            setSubmitting(true);

            const requestData = {

                lecturer: {
                    lecturerId: lecturerId
                },

                timetable: {
                    timetableId: timetableId
                },

                reason: reason.trim(),

                requestedDay:
                    requestedDay || null,

                requestedStartTime:
                    requestedStartTime || null,

                requestedEndTime:
                    requestedEndTime || null,

                requestedClassroom:
                    requestedClassroom || null,

                status: "PENDING"
            };


            console.log(
                "Submitting request:",
                requestData
            );


            await axios.post(
                "http://localhost:8080/api/timetable-change-requests",
                requestData,
                {
                    headers: {
                        ...(token
                            ? {
                                Authorization:
                                    `Bearer ${token}`
                            }
                            : {}),
                        "Content-Type":
                            "application/json"
                    }
                }
            );


            setSuccess("Timetable change request submitted successfully.");


            closeRequestForm();


            // Reload requests so the new PENDING
            // request immediately appears.
            await loadChangeRequests(
                lecturerId
            );


        } catch (err) {

            console.error(
                "Request submission error:",
                err
            );

            console.error(
                "Server response:",
                err.response?.data
            );

            alert(
                err.response?.data?.message ||
                "Unable to submit timetable change request."
            );

        } finally {

            setSubmitting(false);
        }
    };


    // =====================================================
    // LOGOUT
    // =====================================================

    const handleLogout = () => {

        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("role");

        window.location.href = "/";
    };


    // =====================================================
    // COMMON STYLES
    // =====================================================

    const cardStyle = {
        backgroundColor: "#ffffff",
        border: "1px solid #d5e0e8",
        borderRadius: "10px",
        padding: "25px 30px",
        marginBottom: "25px",
        boxShadow: "0 2px 8px rgba(0,0,0,0.05)"
    };


    const headerStyle = {
        padding: "14px 16px",
        textAlign: "center",
        backgroundColor: "#eef3f7",
        color: "#23415f",
        fontWeight: "700",
        fontSize: "14px",
        borderBottom: "2px solid #d5e0e8",
        whiteSpace: "nowrap"
    };


    const cellStyle = {
        padding: "14px 16px",
        textAlign: "center",
        borderBottom: "1px solid #e4e9ed",
        color: "#23415f",
        fontSize: "14px",
        whiteSpace: "nowrap",
        verticalAlign: "middle"
    };


    // =====================================================
    // LOADING
    // =====================================================

    if (loading) {

        return (
            <div
                style={{
                    minHeight: "100vh",
                    backgroundColor: "#f4f7fa",
                    fontFamily: "Arial, sans-serif",
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    color: "#23415f"
                }}
            >
                <div style={{ textAlign: "center" }}>
                    <h2>Smart Faculty</h2>
                    <p>Loading lecturer dashboard...</p>
                </div>
            </div>
        );
    }


    // =====================================================
    // MAIN DASHBOARD
    // =====================================================

    return (

        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#f4f7fa",
                fontFamily: "Arial, sans-serif",
                color: "#23415f"
            }}
        >

            {/* =================================================
                HEADER
            ================================================= */}

            <header
                style={{
                    width: "100%",
                    backgroundColor: "#23415f",
                    color: "#ffffff",
                    padding: "18px 40px",
                    boxSizing: "border-box",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.12)"
                }}
            >

                <div>

                    <h1
                        style={{
                            margin: "0",
                            fontSize: "22px",
                            fontWeight: "600"
                        }}
                    >
                        Smart Faculty
                    </h1>

                    <p
                        style={{
                            margin: "4px 0 0",
                            fontSize: "12px",
                            opacity: "0.85"
                        }}
                    >
                        Timetable Management System
                    </p>

                </div>


                <div
                    style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "12px"
                    }}
                >

                    <button
                        onClick={() => {
                            setShowNotifications(
                                (previous) => !previous
                            );
                        }}
                        style={{
                            position: "relative",
                            backgroundColor: "rgba(255,255,255,0.12)",
                            color: "#ffffff",
                            border: "1px solid rgba(255,255,255,0.35)",
                            borderRadius: "6px",
                            padding: "9px 14px",
                            cursor: "pointer",
                            fontSize: "18px",
                            lineHeight: "1",
                            minWidth: "48px"
                        }}
                        title="Notifications"
                    >
                        🔔

                        {unreadNotificationCount > 0 && (
                            <span
                                style={{
                                    position: "absolute",
                                    top: "-7px",
                                    right: "-7px",
                                    minWidth: "19px",
                                    height: "19px",
                                    padding: "0 5px",
                                    borderRadius: "10px",
                                    backgroundColor: "#e74c3c",
                                    color: "#ffffff",
                                    fontSize: "11px",
                                    fontWeight: "700",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    boxSizing: "border-box"
                                }}
                            >
                                {unreadNotificationCount > 99
                                    ? "99+"
                                    : unreadNotificationCount}
                            </span>
                        )}
                    </button>

                    <button
                        onClick={handleLogout}
                        style={{
                        backgroundColor: "#ffffff",
                        color: "#23415f",
                        border: "none",
                        borderRadius: "6px",
                        padding: "9px 18px",
                        cursor: "pointer",
                        fontSize: "14px",
                        fontWeight: "600"
                    }}
                >
                        Logout
                    </button>

                </div>

            </header>


            {/* =================================================
                NOTIFICATIONS PANEL
            ================================================= */}

            {showNotifications && (
                <div
                    style={{
                        width: "100%",
                        maxWidth: "1250px",
                        margin: "18px auto 0",
                        padding: "0 35px",
                        boxSizing: "border-box"
                    }}
                >

                    <div
                        style={{
                            ...cardStyle,
                            marginBottom: "0",
                            padding: "20px 24px"
                        }}
                    >

                        <div
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                gap: "15px",
                                marginBottom: "15px"
                            }}
                        >
                            <h3
                                style={{
                                    margin: "0",
                                    color: "#23415f",
                                    fontSize: "19px"
                                }}
                            >
                                Notifications
                            </h3>

                            <span
                                style={{
                                    fontSize: "13px",
                                    color: "#687b8d"
                                }}
                            >
                                {unreadNotificationCount} unread
                            </span>
                        </div>

                        {notificationLoading ? (

                            <p
                                style={{
                                    margin: "0",
                                    color: "#687b8d"
                                }}
                            >
                                Loading notifications...
                            </p>

                        ) : notificationError ? (

                            <p
                                style={{
                                    margin: "0",
                                    color: "#c0392b",
                                    textAlign: "center"
                                }}
                            >
                                {notificationError}
                            </p>

                        ) : lecturerNotifications.length === 0 ? (

                            <p
                                style={{
                                    margin: "0",
                                    color: "#8797a5",
                                    textAlign: "center",
                                    padding: "10px 0"
                                }}
                            >
                                No notifications yet.
                            </p>

                        ) : (

                            <div
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    gap: "10px",
                                    maxHeight: "360px",
                                    overflowY: "auto"
                                }}
                            >

                                {lecturerNotifications.map(
                                    (notification) => {

                                        const notificationId =
                                            getNotificationId(
                                                notification
                                            );

                                        const isUnread =
                                            notification?.isRead !== true;

                                        return (

                                            <div
                                                key={
                                                    notificationId ||
                                                    `${notification?.createdAt}-${notification?.title}`
                                                }
                                                style={{
                                                    border: isUnread
                                                        ? "1px solid #b9cfe0"
                                                        : "1px solid #e4e9ed",
                                                    borderRadius: "8px",
                                                    padding: "14px 16px",
                                                    backgroundColor: isUnread
                                                        ? "#f7fbfe"
                                                        : "#ffffff"
                                                }}
                                            >

                                                <div
                                                    style={{
                                                        display: "flex",
                                                        justifyContent: "space-between",
                                                        alignItems: "flex-start",
                                                        gap: "15px"
                                                    }}
                                                >

                                                    <div
                                                        style={{
                                                            flex: 1
                                                        }}
                                                    >
                                                        <div
                                                            style={{
                                                                display: "flex",
                                                                alignItems: "center",
                                                                gap: "8px",
                                                                flexWrap: "wrap",
                                                                marginBottom: "5px"
                                                            }}
                                                        >
                                                            <strong
                                                                style={{
                                                                    color: "#23415f",
                                                                    fontSize: "15px"
                                                                }}
                                                            >
                                                                {notification?.title ||
                                                                    "Notification"}
                                                            </strong>

                                                            {isUnread && (
                                                                <span
                                                                    style={{
                                                                        fontSize: "11px",
                                                                        fontWeight: "700",
                                                                        color: "#ffffff",
                                                                        backgroundColor: "#e74c3c",
                                                                        borderRadius: "10px",
                                                                        padding: "3px 8px"
                                                                    }}
                                                                >
                                                                    NEW
                                                                </span>
                                                            )}
                                                        </div>

                                                        <p
                                                            style={{
                                                                margin: "0 0 7px",
                                                                color: "#52677a",
                                                                fontSize: "14px",
                                                                lineHeight: "1.5"
                                                            }}
                                                        >
                                                            {notification?.message ||
                                                                "-"}
                                                        </p>

                                                        <div
                                                            style={{
                                                                color: "#8797a5",
                                                                fontSize: "12px"
                                                            }}
                                                        >
                                                            {formatNotificationDateTime(
                                                                notification?.createdAt
                                                            )}
                                                        </div>
                                                    </div>

                                                    {isUnread && (
                                                        <button
                                                            onClick={() =>
                                                                markNotificationAsRead(
                                                                    notification
                                                                )
                                                            }
                                                            style={{
                                                                flexShrink: 0,
                                                                backgroundColor: "#23415f",
                                                                color: "#ffffff",
                                                                border: "none",
                                                                borderRadius: "6px",
                                                                padding: "8px 12px",
                                                                cursor: "pointer",
                                                                fontSize: "12px",
                                                                fontWeight: "600"
                                                            }}
                                                        >
                                                            Mark as Read
                                                        </button>
                                                    )}

                                                </div>

                                            </div>
                                            

                                        );
                                    }
                                )}

                            </div>

                        )}

                    </div>

                </div>
            )}


            {/* =================================================
                MAIN
            ================================================= */}

            <main
                style={{
                    width: "100%",
                    maxWidth: "1250px",
                    margin: "0 auto",
                    padding: "30px 35px 50px",
                    boxSizing: "border-box"
                }}

                
            >

                {/* =================================================
                    WELCOME
                ================================================= */}

                <div style={cardStyle}>

                    <h2
                        style={{
                            margin: "0 0 8px",
                            fontSize: "26px",
                            color: "#23415f",
                            textAlign: "center"
                        }}
                    >
                        Lecturer Dashboard
                    </h2>

                    <p
                        style={{
                            margin: "0",
                            color: "#687b8d",
                            fontSize: "15px",
                            textAlign: "center"
                        }}
                    >
                        Welcome{" "}

                        <strong>
                            {lecturer?.lecturerName ||
                                lecturer?.lecturer_name ||
                                username}
                        </strong>

                    </p>

                </div>


                {/* =================================================
                    MY INFORMATION
                ================================================= */}

                <div style={cardStyle}>

                    <h3
                        style={{
                            margin: "0 0 22px",
                            color: "#23415f",
                            fontSize: "19px",
                            textAlign: "center"
                        }}
                    >
                        My Information
                    </h3>


                    <div
                        style={{
                            display: "grid",
                            gridTemplateColumns:
                                "repeat(4, 1fr)",
                            gap: "25px",
                            textAlign: "center"
                        }}
                    >

                        {/* Lecturer Code */}

                        <div>

                            <div
                                style={{
                                    fontSize: "12px",
                                    color: "#8797a5",
                                    marginBottom: "6px"
                                }}
                            >
                                Lecturer Code
                            </div>

                            <div
                                style={{
                                    fontWeight: "600",
                                    fontSize: "15px"
                                }}
                            >
                                {lecturer?.lecturerCode ||
                                    lecturer?.lecturer_code ||
                                    "-"}
                            </div>

                        </div>


                        {/* Lecturer Name */}

                        <div>

                            <div
                                style={{
                                    fontSize: "12px",
                                    color: "#8797a5",
                                    marginBottom: "6px"
                                }}
                            >
                                Lecturer Name
                            </div>

                            <div
                                style={{
                                    fontWeight: "600",
                                    fontSize: "15px"
                                }}
                            >
                                {lecturer?.lecturerName ||
                                    lecturer?.lecturer_name ||
                                    "-"}
                            </div>

                        </div>


                        {/* Email */}

                        <div>

                            <div
                                style={{
                                    fontSize: "12px",
                                    color: "#8797a5",
                                    marginBottom: "6px"
                                }}
                            >
                                Email
                            </div>

                            <div
                                style={{
                                    fontWeight: "600",
                                    fontSize: "15px",
                                    wordBreak: "break-word"
                                }}
                            >
                                {lecturer?.email || "-"}
                            </div>

                        </div>


                        {/* Department */}

                        <div>

                            <div
                                style={{
                                    fontSize: "12px",
                                    color: "#8797a5",
                                    marginBottom: "6px"
                                }}
                            >
                                Department
                            </div>

                            <div
                                style={{
                                    fontWeight: "600",
                                    fontSize: "15px",
                                    lineHeight: "1.5"
                                }}
                            >

                                {getDepartmentName() ===
                                "Department of Information and Communication Technology" ? (
                                    <>
                                        <div>
                                            Department of Information and
                                        </div>
                                        <div>
                                            Communication Technology
                                        </div>
                                    </>
                                ) : (
                                    getDepartmentName()
                                )}

                            </div>

                        </div>

                    </div>

                </div>


                {/* =================================================
                    MY TEACHING ALLOCATION
                ================================================= */}

                <div style={cardStyle}>

                    <h3
                        style={{
                            margin: "0 0 20px",
                            color: "#23415f",
                            fontSize: "19px",
                            textAlign: "center"
                        }}
                    >
                        My Teaching Allocation
                    </h3>


                    {allocationLoading ? (

                        <p>Loading teaching allocations...</p>

                    ) : allocationError ? (

                        <p
                            style={{
                                color: "#c0392b",
                                textAlign: "center"
                            }}
                        >
                            {allocationError}
                        </p>

                    ) : allocations.length === 0 ? (

                        <p
                            style={{
                                color: "#687b8d",
                                textAlign: "center"
                            }}
                        >
                            No teaching allocations found.
                        </p>

                    ) : (

                        <div
                            style={{
                                width: "100%",
                                overflowX: "auto"
                            }}
                        >

                            <table
                                style={{
                                    width: "100%",
                                    minWidth: "650px",
                                    borderCollapse: "collapse"
                                }}
                            >

                                <thead>

                                    <tr>

                                        <th style={headerStyle}>
                                            Subject
                                        </th>

                                        <th style={headerStyle}>
                                            Academic Year
                                        </th>

                                        <th style={headerStyle}>
                                            Semester
                                        </th>

                                    </tr>

                                </thead>


                                <tbody>

                                    {allocations.map(
                                        (allocation, index) => (

                                            <tr
                                                key={
                                                    allocation?.allocationId ||
                                                    index
                                                }
                                            >

                                                <td
                                                    style={{
                                                        ...cellStyle,
                                                        fontWeight: "700"
                                                    }}
                                                >
                                                    {getSubjectCode(
                                                        allocation
                                                    )}
                                                </td>

                                                <td style={cellStyle}>
                                                    {getYear(
                                                        allocation
                                                    )}
                                                </td>

                                                <td style={cellStyle}>
                                                    {getSemester(
                                                        allocation
                                                    )}
                                                </td>

                                            </tr>

                                        )
                                    )}

                                </tbody>

                            </table>

                        </div>

                    )}

                </div>


                {/* =================================================
                    MY TIMETABLE
                ================================================= */}

                <div style={cardStyle}>

                    <h3
                        style={{
                            margin: "0 0 20px",
                            color: "#23415f",
                            fontSize: "19px",
                            textAlign: "center"
                        }}
                    >
                        My Timetable
                    </h3>


                    {timetables.length === 0 ? (

                        <p
                            style={{
                                color: "#687b8d",
                                textAlign: "center"
                            }}
                        >
                            No timetable records found.
                        </p>

                    ) : (

                        <div
                            style={{
                                width: "100%",
                                overflowX: "auto"
                            }}
                        >

                            <table
                                style={{
                                    width: "100%",
                                    minWidth: "1100px",
                                    borderCollapse: "collapse"
                                }}
                            >

                                <thead>

                                    <tr>

                                        <th style={headerStyle}>
                                            Subject
                                        </th>

                                        <th style={headerStyle}>
                                            Year
                                        </th>

                                        <th style={headerStyle}>
                                            Semester
                                        </th>

                                        <th style={headerStyle}>
                                            Day
                                        </th>

                                        <th style={headerStyle}>
                                            Time
                                        </th>

                                        <th style={headerStyle}>
                                            Classroom
                                        </th>

                                        <th style={headerStyle}>
                                            Action
                                        </th>

                                    </tr>

                                </thead>


                                <tbody>

                                    {timetables.map(
                                        (timetable, index) => (

                                            <tr
                                                key={
                                                    timetable?.timetableId ||
                                                    timetable?.timetable_id ||
                                                    index
                                                }
                                            >

                                                <td
                                                    style={{
                                                        ...cellStyle,
                                                        fontWeight: "700"
                                                    }}
                                                >
                                                    {getSubjectCode(
                                                        timetable
                                                    )}
                                                </td>

                                                <td style={cellStyle}>
                                                    {getYear(
                                                        timetable
                                                    )}
                                                </td>

                                                <td style={cellStyle}>
                                                    {getSemester(
                                                        timetable
                                                    )}
                                                </td>

                                                <td
                                                    style={{
                                                        ...cellStyle,
                                                        fontWeight: "600"
                                                    }}
                                                >
                                                    {getDay(
                                                        timetable
                                                    )}
                                                </td>

                                                <td style={cellStyle}>

                                                    {getStartTime(
                                                        timetable
                                                    )}

                                                    {" - "}

                                                    {getEndTime(
                                                        timetable
                                                    )}

                                                </td>

                                                <td
                                                    style={{
                                                        ...cellStyle,
                                                        fontWeight: "700"
                                                    }}
                                                >
                                                    {getClassroom(
                                                        timetable
                                                    )}
                                                </td>

                                                <td style={cellStyle}>

                                                    <button
                                                        onClick={() =>
                                                            openRequestForm(
                                                                timetable
                                                            )
                                                        }
                                                        style={{
                                                            backgroundColor:
                                                                "#23415f",
                                                            color:
                                                                "#ffffff",
                                                            border: "none",
                                                            borderRadius:
                                                                "6px",
                                                            padding:
                                                                "9px 14px",
                                                            cursor:
                                                                "pointer",
                                                            fontWeight:
                                                                "600",
                                                            fontSize:
                                                                "12px"
                                                        }}
                                                    >
                                                        Request Change
                                                    </button>

                                                </td>

                                            </tr>

                                        )
                                    )}

                                </tbody>

                            </table>

                        </div>

                    )}

                </div>


                {/* =================================================
                    CHANGE REQUEST FORM
                ================================================= */}

                {showRequestForm && (

                    <div style={cardStyle}>

                        <h3
                            style={{
                                margin: "0 0 20px",
                                color: "#23415f",
                                fontSize: "19px",
                                textAlign: "center"
                            }}
                        >
                            Request Timetable Change
                        </h3>


                        {selectedTimetable && (

                            <div
                                style={{
                                    backgroundColor: "#f4f7fa",
                                    padding: "15px",
                                    borderRadius: "6px",
                                    marginBottom: "20px"
                                }}
                            >

                                <strong>
                                    Subject:
                                </strong>{" "}
                                {getSubjectCode(
                                    selectedTimetable
                                )}

                                <br />

                                <strong>
                                    Current Day:
                                </strong>{" "}
                                {getDay(
                                    selectedTimetable
                                )}

                                <br />

                                <strong>
                                    Current Time:
                                </strong>{" "}
                                {getStartTime(
                                    selectedTimetable
                                )}
                                {" - "}
                                {getEndTime(
                                    selectedTimetable
                                )}

                                <br />

                                <strong>
                                    Current Classroom:
                                </strong>{" "}
                                {getClassroom(
                                    selectedTimetable
                                )}

                            </div>

                        )}


                        <div
                            style={{
                                display: "grid",
                                gridTemplateColumns:
                                    "repeat(2, 1fr)",
                                gap: "18px"
                            }}
                        >

                            {/* Requested Day */}

                            <div>

                                <label
                                    style={{
                                        display: "block",
                                        marginBottom: "7px",
                                        fontSize: "13px",
                                        fontWeight: "600"
                                    }}
                                >
                                    Requested Day
                                </label>

                                <select
                                    value={requestedDay}
                                    onChange={(e) =>
                                        setRequestedDay(
                                            e.target.value
                                        )
                                    }
                                    style={{
                                        width: "100%",
                                        padding: "10px",
                                        border:
                                            "1px solid #ccd6df",
                                        borderRadius: "6px",
                                        boxSizing: "border-box"
                                    }}
                                >

                                    <option value="">
                                        Keep current day
                                    </option>

                                    <option value="MONDAY">
                                        Monday
                                    </option>

                                    <option value="TUESDAY">
                                        Tuesday
                                    </option>

                                    <option value="WEDNESDAY">
                                        Wednesday
                                    </option>

                                    <option value="THURSDAY">
                                        Thursday
                                    </option>

                                    <option value="FRIDAY">
                                        Friday
                                    </option>

                                </select>

                            </div>


                            {/* Requested Classroom */}

                            <div>

                                <label
                                    style={{
                                        display: "block",
                                        marginBottom: "7px",
                                        fontSize: "13px",
                                        fontWeight: "600"
                                    }}
                                >
                                    Requested Classroom
                                </label>

                                <input
                                    type="text"
                                    value={requestedClassroom}
                                    onChange={(e) =>
                                        setRequestedClassroom(
                                            e.target.value
                                        )
                                    }
                                    placeholder="Example: C1"
                                    style={{
                                        width: "100%",
                                        padding: "10px",
                                        border:
                                            "1px solid #ccd6df",
                                        borderRadius: "6px",
                                        boxSizing: "border-box"
                                    }}
                                />

                            </div>


                            {/* Start Time */}

                            <div>

                                <label
                                    style={{
                                        display: "block",
                                        marginBottom: "7px",
                                        fontSize: "13px",
                                        fontWeight: "600"
                                    }}
                                >
                                    Requested Start Time
                                </label>

                                <input
                                    type="time"
                                    value={requestedStartTime}
                                    onChange={(e) =>
                                        setRequestedStartTime(
                                            e.target.value
                                        )
                                    }
                                    style={{
                                        width: "100%",
                                        padding: "10px",
                                        border:
                                            "1px solid #ccd6df",
                                        borderRadius: "6px",
                                        boxSizing: "border-box"
                                    }}
                                />

                            </div>


                            {/* End Time */}

                            <div>

                                <label
                                    style={{
                                        display: "block",
                                        marginBottom: "7px",
                                        fontSize: "13px",
                                        fontWeight: "600"
                                    }}
                                >
                                    Requested End Time
                                </label>

                                <input
                                    type="time"
                                    value={requestedEndTime}
                                    onChange={(e) =>
                                        setRequestedEndTime(
                                            e.target.value
                                        )
                                    }
                                    style={{
                                        width: "100%",
                                        padding: "10px",
                                        border:
                                            "1px solid #ccd6df",
                                        borderRadius: "6px",
                                        boxSizing: "border-box"
                                    }}
                                />

                            </div>


                            {/* Reason */}

                            <div
                                style={{
                                    gridColumn: "1 / -1"
                                }}
                            >

                                <label
                                    style={{
                                        display: "block",
                                        marginBottom: "7px",
                                        fontSize: "13px",
                                        fontWeight: "600"
                                    }}
                                >
                                    Reason
                                </label>

                                <textarea
                                    value={reason}
                                    onChange={(e) =>
                                        setReason(
                                            e.target.value
                                        )
                                    }
                                    rows="4"
                                    placeholder="Enter the reason for the timetable change..."
                                    style={{
                                        width: "100%",
                                        padding: "10px",
                                        border:
                                            "1px solid #ccd6df",
                                        borderRadius: "6px",
                                        boxSizing: "border-box",
                                        resize: "vertical"
                                    }}
                                />

                            </div>

                        </div>


                        {/* Buttons */}

                        <div
                            style={{
                                marginTop: "20px",
                                display: "flex",
                                justifyContent: "flex-end",
                                gap: "10px"
                            }}
                        >

                            <button
                                onClick={closeRequestForm}
                                disabled={submitting}
                                style={{
                                    backgroundColor: "#eef3f7",
                                    color: "#23415f",
                                    border: "none",
                                    borderRadius: "6px",
                                    padding: "10px 18px",
                                    cursor: "pointer",
                                    fontWeight: "600"
                                }}
                            >
                                Cancel
                            </button>


                            <button
                                onClick={submitRequest}
                                disabled={submitting}
                                style={{
                                    backgroundColor: "#23415f",
                                    color: "#ffffff",
                                    border: "none",
                                    borderRadius: "6px",
                                    padding: "10px 18px",
                                    cursor: submitting
                                        ? "not-allowed"
                                        : "pointer",
                                    fontWeight: "600",
                                    opacity: submitting
                                        ? 0.7
                                        : 1
                                }}
                            >
                                {submitting
                                    ? "Submitting..."
                                    : "Submit Request"}
                            </button>

                        </div>

                    </div>

                )}
                

                {/* =================================================
                    MY CHANGE REQUESTS
                ================================================= */}

                <div style={cardStyle}>

                    <h3
                        style={{
                            margin: "0 0 20px",
                            color: "#23415f",
                            fontSize: "19px",
                            textAlign: "center"
                        }}
                    >
                        My Timetable Change Requests
                    </h3>

                    {requestActionMessage && (
    <div
        style={{
            padding: "10px",
            marginBottom: "15px",
            borderRadius: "6px",
            backgroundColor: requestActionMessage.toLowerCase().includes("successfully")
                ? "#d4edda"
                : "#f8d7da",
            color: requestActionMessage.toLowerCase().includes("successfully")
                ? "#155724"
                : "#721c24"
        }}
    >
        {requestActionMessage}
    </div>
)}


                    {changeRequestLoading ? (

                        <p
                            style={{
                                color: "#687b8d",
                                textAlign: "center"
                            }}
                        >
                            Loading change requests...
                        </p>

                    ) : changeRequestError ? (

                        <p
                            style={{
                                color: "#c0392b",
                                textAlign: "center"
                            }}
                        >
                            {changeRequestError}
                        </p>

                    ) : changeRequests.length === 0 ? (

                        <p
                            style={{
                                color: "#687b8d",
                                textAlign: "center"
                            }}
                        >
                            No timetable change requests found.
                        </p>

                    ) : (

                        <div
                            style={{
                                width: "100%",
                                overflowX: "auto"
                            }}
                        >

                            <table
                                style={{
                                    width: "100%",
                                    minWidth: "1050px",
                                    borderCollapse: "collapse"
                                }}
                            >

                                <thead>

                                    <tr>

                                        <th style={headerStyle}>
                                            Subject
                                        </th>

                                        <th style={headerStyle}>
                                            Requested Day
                                        </th>

                                        <th style={headerStyle}>
                                            Requested Time
                                        </th>

                                        <th style={headerStyle}>
                                            Requested Classroom
                                        </th>

                                        <th style={headerStyle}>
                                            Status
                                        </th>

                                        <th style={headerStyle}>
                                            Requested At
                                        </th>

                                        <th style={headerStyle}>
                                            Reason
                                        </th>

                                        <th style={headerStyle}>
                                             Actions
                                        </th>

                                    </tr>

                                </thead>


                                <tbody>

                                    {changeRequests.map(
                                        (request, index) => {

                                            const status =
                                                request?.status ||
                                                "PENDING";

                                            return (

                                                <tr
                                                    key={
                                                        request?.requestId ||
                                                        index
                                                    }
                                                >

                                                    <td
                                                        style={{
                                                            ...cellStyle,
                                                            fontWeight:
                                                                "700"
                                                        }}
                                                    >
                                                        {getRequestSubject(
                                                            request
                                                        )}
                                                    </td>


                                                    <td
                                                        style={
                                                            cellStyle
                                                        }
                                                    >
                                                        {
                                                            request?.requestedDay ||
                                                            "-"
                                                        }
                                                    </td>


                                                    <td
                                                        style={
                                                            cellStyle
                                                        }
                                                    >

                                                        {
                                                            request?.requestedStartTime
                                                                ? formatTime(
                                                                    request.requestedStartTime
                                                                )
                                                                : "-"
                                                        }

                                                        {" - "}

                                                        {
                                                            request?.requestedEndTime
                                                                ? formatTime(
                                                                    request.requestedEndTime
                                                                )
                                                                : "-"
                                                        }

                                                    </td>


                                                    <td
                                                        style={{
                                                            ...cellStyle,
                                                            fontWeight:
                                                                "700"
                                                        }}
                                                    >
                                                        {
                                                            request?.requestedClassroom ||
                                                            "-"
                                                        }
                                                    </td>


                                                    <td
                                                        style={
                                                            cellStyle
                                                        }
                                                    >

                                                        <span
                                                            style={{
                                                                display:
                                                                    "inline-block",
                                                                padding:
                                                                    "5px 10px",
                                                                borderRadius:
                                                                    "12px",
                                                                fontSize:
                                                                    "11px",
                                                                fontWeight:
                                                                    "700",

                                                                backgroundColor:
                                                                    status ===
                                                                    "APPROVED"
                                                                        ? "#e8f5e9"
                                                                        : status ===
                                                                          "REJECTED"
                                                                            ? "#fdecea"
                                                                            : "#fff4e5",

                                                                color:
                                                                    status ===
                                                                    "APPROVED"
                                                                        ? "#2e7d32"
                                                                        : status ===
                                                                          "REJECTED"
                                                                            ? "#c0392b"
                                                                            : "#b26a00"
                                                            }}
                                                        >
                                                            {status}
                                                        </span>

                                                    </td>


                                                    <td
                                                        style={{
                                                            ...cellStyle,
                                                            whiteSpace:
                                                                "normal"
                                                        }}
                                                    >
                                                        {formatRequestDateTime(
                                                            request?.createdAt
                                                        )}
                                                    </td>


                                                    <td
                                                        style={{
                                                            ...cellStyle,
                                                            whiteSpace:
                                                                "normal",
                                                            textAlign:
                                                                "left",
                                                            lineHeight:
                                                                "1.5"
                                                        }}
                                                    >
                                                        {
                                                            request?.reason ||
                                                            "-"
                                                        }
                                                    </td>
                                                    <td style={cellStyle}>
    <div
        style={{
            display: "flex",
            gap: "8px",
            justifyContent: "center",
            alignItems: "center"
        }}
    >
        <button
            type="button"
            onClick={() => openEditRequestForm(request)}
            style={{
                padding: "7px 13px",
                backgroundColor: "#2874a6",
                color: "#fff",
                border: "none",
                borderRadius: "5px",
                cursor: "pointer",
                fontWeight: "600"
            }}
        >
            Edit
        </button>

       <button
    type="button"
    onClick={() => handleDeleteClick(request.requestId)}
    style={{
        padding: "7px 13px",
        backgroundColor: "#c0392b",
        color: "#fff",
        border: "none",
        borderRadius: "5px",
        cursor: "pointer",
        fontWeight: "600"
    }}
>
    Delete
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

                </div>
                {/* EDIT CHANGE REQUEST FORM */}

{showEditRequestForm && (

    <div
        style={{
            marginTop: "25px",
            padding: "22px",
            backgroundColor: "#f7f9fc",
            border: "1px solid #dce4ec",
            borderRadius: "10px"
        }}
    >

        <h3
            style={{
                color: "#23415f",
                marginTop: 0,
                marginBottom: "20px",
                textAlign: "center"
            }}
        >
            Edit Timetable Change Request
        </h3>

        <div
            style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                gap: "16px"
            }}
        >

            <div>
                <label>Requested Day</label>

                <select
                    value={editRequestedDay}
                    onChange={e => setEditRequestedDay(e.target.value)}
                    style={{
                        width: "100%",
                        padding: "10px",
                        marginTop: "6px",
                        border: "1px solid #ccd5df",
                        borderRadius: "5px"
                    }}
                >
                    <option value="">Keep current day</option>
                    <option value="MONDAY">Monday</option>
                    <option value="TUESDAY">Tuesday</option>
                    <option value="WEDNESDAY">Wednesday</option>
                    <option value="THURSDAY">Thursday</option>
                    <option value="FRIDAY">Friday</option>
                </select>
            </div>

            <div>
                <label>Requested Classroom</label>

                <input
                    type="text"
                    value={editRequestedClassroom}
                    onChange={e => setEditRequestedClassroom(e.target.value)}
                    placeholder="Enter classroom"
                    style={{
                        width: "100%",
                        padding: "10px",
                        marginTop: "6px",
                        boxSizing: "border-box",
                        border: "1px solid #ccd5df",
                        borderRadius: "5px"
                    }}
                />
            </div>

            <div>
                <label>Requested Start Time</label>

                <input
                    type="time"
                    value={editRequestedStartTime}
                    onChange={e => setEditRequestedStartTime(e.target.value)}
                    style={{
                        width: "100%",
                        padding: "10px",
                        marginTop: "6px",
                        boxSizing: "border-box",
                        border: "1px solid #ccd5df",
                        borderRadius: "5px"
                    }}
                />
            </div>

            <div>
                <label>Requested End Time</label>

                <input
                    type="time"
                    value={editRequestedEndTime}
                    onChange={e => setEditRequestedEndTime(e.target.value)}
                    style={{
                        width: "100%",
                        padding: "10px",
                        marginTop: "6px",
                        boxSizing: "border-box",
                        border: "1px solid #ccd5df",
                        borderRadius: "5px"
                    }}
                />
            </div>

            <div style={{ gridColumn: "1 / -1" }}>
                <label>Reason</label>

                <textarea
                    rows="4"
                    value={editRequestReason}
                    onChange={e => setEditRequestReason(e.target.value)}
                    placeholder="Enter the reason for the timetable change..."
                    style={{
                        width: "100%",
                        padding: "10px",
                        marginTop: "6px",
                        boxSizing: "border-box",
                        border: "1px solid #ccd5df",
                        borderRadius: "5px",
                        resize: "vertical"
                    }}
                />
            </div>

        </div>

        <div
            style={{
                display: "flex",
                gap: "10px",
                justifyContent: "center",
                marginTop: "20px"
            }}
        >

            <button
                type="button"
                onClick={updateChangeRequest}
                disabled={editRequestSaving}
                style={{
                    padding: "10px 20px",
                    backgroundColor: "#2874a6",
                    color: "#fff",
                    border: "none",
                    borderRadius: "5px",
                    cursor: editRequestSaving ? "not-allowed" : "pointer"
                }}
            >
                {editRequestSaving ? "Updating..." : "Update Request"}
            </button>

            <button
                type="button"
                onClick={closeEditRequestForm}
                disabled={editRequestSaving}
                style={{
                    padding: "10px 20px",
                    backgroundColor: "#687b8d",
                    color: "#fff",
                    border: "none",
                    borderRadius: "5px",
                    cursor: "pointer"
                }}
            >
                Cancel
            </button>

        </div>

    </div>
)}
{/* =================================================
    DELETE CONFIRMATION DIALOG
================================================= */}

{showDeleteDialog && (

    <div
        style={{
            position: "fixed",
            top: 0,
            left: 0,
            width: "100%",
            height: "100%",
            backgroundColor: "rgba(0, 0, 0, 0.55)",
            display: "flex",
            justifyContent: "center",
            alignItems: "center",
            zIndex: 9999
        }}
    >

        <div
            style={{
                backgroundColor: "#ffffff",
                width: "90%",
                maxWidth: "420px",
                padding: "30px",
                borderRadius: "12px",
                textAlign: "center",
                boxShadow: "0 8px 30px rgba(0,0,0,0.2)"
            }}
        >

            {/* Warning Icon */}

            <div
                style={{
                    width: "55px",
                    height: "55px",
                    margin: "0 auto 15px",
                    borderRadius: "50%",
                    backgroundColor: "#fee2e2",
                    color: "#dc2626",
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    fontSize: "30px",
                    fontWeight: "bold"
                }}
            >
                !
            </div>


            {/* Dialog Title */}

            <h2
                style={{
                    marginBottom: "12px",
                    color: "#23415f",
                    fontSize: "22px"
                }}
            >
                Confirm Deletion
            </h2>


            {/* Dialog Message */}

            <p
                style={{
                    color: "#687b8d",
                    fontSize: "15px",
                    lineHeight: "1.6",
                    marginBottom: "25px"
                }}
            >
                Are you sure you want to delete this
                timetable change request?
            </p>


            {/* Dialog Buttons */}

            <div
                style={{
                    display: "flex",
                    justifyContent: "center",
                    gap: "12px"
                }}
            >

                {/* Cancel Button */}

                <button
                    type="button"
                    onClick={closeDeleteDialog}
                    style={{
                        padding: "10px 25px",
                        backgroundColor: "#e5e7eb",
                        color: "#374151",
                        border: "none",
                        borderRadius: "6px",
                        cursor: "pointer",
                        fontSize: "14px",
                        fontWeight: "600"
                    }}
                >
                    Cancel
                </button>


                {/* Confirm Delete Button */}

                <button
                    type="button"
                    onClick={confirmDeleteRequest}
                    style={{
                        padding: "10px 25px",
                        backgroundColor: "#dc2626",
                        color: "#ffffff",
                        border: "none",
                        borderRadius: "6px",
                        cursor: "pointer",
                        fontSize: "14px",
                        fontWeight: "600"
                    }}
                >
                    Delete
                </button>

            </div>

        </div>

    </div>
)}

                </main>
                            {/* =================================================
                CHANGE PASSWORD
            ================================================= */}

            <main
                style={{
                    width: "100%",
                    maxWidth: "1250px",
                    margin: "0 auto",
                    padding: "0 35px 30px",
                    boxSizing: "border-box"
                }}
            >

                <LecturerChangePassword />

            </main>

            


            {/* =================================================
                FOOTER
            ================================================= */}

            <footer
                style={{
                    textAlign: "center",
                    padding: "20px",
                    color: "#8797a5",
                    fontSize: "12px"
                }}
            >
                Smart Faculty Timetable Management System
            </footer>

            <StatusDialog
    type="success"
    message={success}
    onClose={() => setSuccess("")}
/>



        </div>
    );
}

export default LecturerDashboard;