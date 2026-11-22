import { useEffect, useState } from "react";
import axios from "axios";
import StudentRegisteredSubjects from "./StudentRegisteredSubjects";
import StudentTimetable from "./StudentTimetable";
import ChangePassword from "../ChangePassword";

function StudentDashboard() {

    // =====================================================
    // STUDENT STATES
    // =====================================================

    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    // =====================================================
    // NOTIFICATION STATES
    // =====================================================

    const [notifications, setNotifications] = useState([]);
    const [showNotifications, setShowNotifications] =
        useState(false);

    const [notificationLoading, setNotificationLoading] =
        useState(false);

    const [markingAllAsRead, setMarkingAllAsRead] =
        useState(false);

    // =====================================================
    // LOGIN USERNAME
    // =====================================================

    const username =
        localStorage.getItem("username") || "Student";


    // =====================================================
    // LOAD STUDENT INFORMATION
    // =====================================================

    useEffect(() => {
        loadStudentInformation();
    }, []);


    const loadStudentInformation = async () => {

        try {

            setLoading(true);
            setError("");

            const token =
                localStorage.getItem("token");

            const loggedInUsername =
                localStorage.getItem("username");


            // -------------------------------------------------
            // CHECK LOGIN
            // -------------------------------------------------

            if (!token || !loggedInUsername) {

                setError(
                    "You are not logged in."
                );

                setLoading(false);

                return;
            }


            // -------------------------------------------------
            // GET ALL STUDENTS
            // -------------------------------------------------

            const response = await axios.get(
                "http://localhost:8080/api/students",
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            const students =
                Array.isArray(response.data)
                    ? response.data
                    : [];


            console.log(
                "All Students:",
                students
            );


            // -------------------------------------------------
            // FIND LOGGED-IN STUDENT
            // -------------------------------------------------

            const currentStudent =
                students.find(
                    (item) =>
                        item.registrationNumber ===
                            loggedInUsername ||

                        item.user?.username ===
                            loggedInUsername ||

                        item.username ===
                            loggedInUsername ||

                        item.email ===
                            loggedInUsername
                );


            if (!currentStudent) {

                setError(
                    "Student information could not be found."
                );

                setLoading(false);

                return;
            }


            console.log(
                "Current Student:",
                currentStudent
            );


            setStudent(
                currentStudent
            );


            // -------------------------------------------------
            // GET STUDENT ID
            // -------------------------------------------------

            const studentId =
                currentStudent.studentId ||
                currentStudent.student_id;


            if (!studentId) {

                setError(
                    "Student ID could not be found."
                );

                setLoading(false);

                return;
            }


            // -------------------------------------------------
            // LOAD NOTIFICATIONS
            // -------------------------------------------------

            await loadNotifications(
                studentId,
                token
            );


        } catch (err) {

            console.error(
                "Error loading student information:",
                err
            );


            if (
                err.response?.status === 403
            ) {

                setError(
                    "Access denied. Please log in again."
                );

            } else {

                setError(
                    "Unable to load student information."
                );
            }

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // LOAD NOTIFICATIONS
    // =====================================================

    const loadNotifications = async (
        studentId,
        token = localStorage.getItem("token")
    ) => {

        if (!studentId || !token) {
            return;
        }


        try {

            setNotificationLoading(true);


            const response = await axios.get(
                `http://localhost:8080/api/notifications/student/${studentId}`,
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            console.log(
                "Student Notifications:",
                response.data
            );


            setNotifications(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );


        } catch (err) {

            console.error(
                "Error loading notifications:",
                err
            );

        } finally {

            setNotificationLoading(false);
        }
    };


    // =====================================================
    // OPEN / CLOSE NOTIFICATIONS
    // =====================================================

    const handleNotificationClick = async () => {

        const newState =
            !showNotifications;


        setShowNotifications(
            newState
        );


        if (
            newState &&
            (
                student?.studentId ||
                student?.student_id
            )
        ) {

            await loadNotifications(
                student.studentId ||
                    student.student_id,

                localStorage.getItem("token")
            );
        }
    };


    // =====================================================
    // MARK ONE NOTIFICATION AS READ
    // =====================================================

    const handleMarkAsRead = async (
        notificationId
    ) => {

        try {

            const token =
                localStorage.getItem("token");


            if (!token) {
                return;
            }


            await axios.put(
                `http://localhost:8080/api/notifications/${notificationId}/read`,
                {},
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            setNotifications(
                (previousNotifications) =>
                    previousNotifications.map(
                        (notification) =>
                            notification.notificationId ===
                            notificationId
                                ? {
                                    ...notification,
                                    isRead: true
                                }
                                : notification
                    )
            );


        } catch (err) {

            console.error(
                "Error marking notification as read:",
                err
            );
        }
    };


    // =====================================================
    // MARK ALL NOTIFICATIONS AS READ
    // =====================================================

    const handleMarkAllAsRead = async () => {

        try {

            const token =
                localStorage.getItem("token");


            const studentId =
                student?.studentId ||
                student?.student_id;


            if (!token || !studentId) {
                return;
            }


            setMarkingAllAsRead(
                true
            );


            await axios.put(
                `http://localhost:8080/api/notifications/student/${studentId}/read-all`,
                {},
                {
                    headers: {
                        Authorization:
                            `Bearer ${token}`
                    }
                }
            );


            setNotifications(
                (previousNotifications) =>
                    previousNotifications.map(
                        (notification) => ({
                            ...notification,
                            isRead: true
                        })
                    )
            );


        } catch (err) {

            console.error(
                "Error marking all notifications as read:",
                err
            );

        } finally {

            setMarkingAllAsRead(
                false
            );
        }
    };


    // =====================================================
    // LOGOUT
    // =====================================================

    const handleLogout = () => {

        localStorage.removeItem(
            "token"
        );

        localStorage.removeItem(
            "username"
        );

        localStorage.removeItem(
            "role"
        );


        window.location.href = "/";
    };


    // =====================================================
    // FORMAT YEAR
    // =====================================================

    const formatYear = (year) => {

        if (
            year === null ||
            year === undefined ||
            year === ""
        ) {

            return "-";
        }


        if (
            typeof year === "number"
        ) {

            return `Year ${year}`;
        }


        const value =
            year.toString();


        if (
            value.includes("YEAR_")
        ) {

            return value.replace(
                "YEAR_",
                "Year "
            );
        }


        if (
            value
                .toLowerCase()
                .startsWith("year")
        ) {

            return value;
        }


        return value;
    };


    // =====================================================
    // FORMAT SEMESTER
    // =====================================================

    const formatSemester = (
        semester
    ) => {

        if (
            semester === null ||
            semester === undefined ||
            semester === ""
        ) {

            return "-";
        }


        if (
            typeof semester === "number"
        ) {

            return `Semester ${semester}`;
        }


        const value =
            semester.toString();


        if (
            value.includes("SEMESTER_")
        ) {

            return value.replace(
                "SEMESTER_",
                "Semester "
            );
        }


        if (
            value
                .toLowerCase()
                .startsWith("semester")
        ) {

            return value;
        }


        return value;
    };


    // =====================================================
    // FORMAT NOTIFICATION DATE
    // =====================================================

    const formatNotificationDate = (
        date
    ) => {

        if (!date) {
            return "";
        }


        try {

            return new Date(
                date
            ).toLocaleString();

        } catch {

            return date;
        }
    };


    // =====================================================
    // UNREAD NOTIFICATION COUNT
    // =====================================================

    const unreadCount =
        notifications.filter(
            (notification) =>
                !notification.isRead
        ).length;


    // =====================================================
    // TABLE STYLES
    // =====================================================

    const headerStyle = {

        padding:
            "14px 16px",

        textAlign:
            "center",

        backgroundColor:
            "#eef3f7",

        color:
            "#23415f",

        fontWeight:
            "700",

        fontSize:
            "14px",

        borderBottom:
            "2px solid #d5e0e8",

        whiteSpace:
            "nowrap"
    };


    const cellStyle = {

        padding:
            "14px 16px",

        textAlign:
            "center",

        borderBottom:
            "1px solid #e4e9ed",

        color:
            "#23415f",

        fontSize:
            "14px",

        whiteSpace:
            "nowrap",

        verticalAlign:
            "middle"
    };


    // =====================================================
    // LOADING SCREEN
    // =====================================================

    if (loading) {

        return (

            <div
                style={{
                    minHeight:
                        "100vh",

                    backgroundColor:
                        "#f4f7fa",

                    fontFamily:
                        "Arial, sans-serif",

                    display:
                        "flex",

                    justifyContent:
                        "center",

                    alignItems:
                        "center",

                    color:
                        "#23415f"
                }}
            >

                <div
                    style={{
                        textAlign:
                            "center"
                    }}
                >

                    <h2>
                        Smart Faculty
                    </h2>

                    <p>
                        Loading your dashboard...
                    </p>

                </div>

            </div>
        );
    }


    // =====================================================
    // ERROR SCREEN
    // =====================================================

    if (error) {

        return (

            <div
                style={{
                    minHeight:
                        "100vh",

                    backgroundColor:
                        "#f4f7fa",

                    fontFamily:
                        "Arial, sans-serif",

                    display:
                        "flex",

                    justifyContent:
                        "center",

                    alignItems:
                        "center"
                }}
            >

                <div
                    style={{
                        backgroundColor:
                            "#ffffff",

                        padding:
                            "35px",

                        borderRadius:
                            "10px",

                        textAlign:
                            "center",

                        border:
                            "1px solid #d5e0e8",

                        boxShadow:
                            "0 2px 10px rgba(0,0,0,0.08)"
                    }}
                >

                    <h2
                        style={{
                            color:
                                "#23415f"
                        }}
                    >
                        Student Dashboard
                    </h2>


                    <p
                        style={{
                            color:
                                "#c0392b"
                        }}
                    >
                        {error}
                    </p>


                    <button
                        onClick={
                            handleLogout
                        }
                        style={{
                            marginTop:
                                "10px",

                            padding:
                                "10px 20px",

                            border:
                                "none",

                            borderRadius:
                                "6px",

                            backgroundColor:
                                "#23415f",

                            color:
                                "#ffffff",

                            cursor:
                                "pointer",

                            fontWeight:
                                "600"
                        }}
                    >
                        Back to Login
                    </button>

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
                minHeight:
                    "100vh",

                backgroundColor:
                    "#f4f7fa",

                fontFamily:
                    "Arial, sans-serif",

                color:
                    "#23415f"
            }}
        >

            {/* =================================================
                HEADER
            ================================================= */}

            <header
                style={{
                    width:
                        "100%",

                    backgroundColor:
                        "#23415f",

                    color:
                        "#ffffff",

                    padding:
                        "18px 40px",

                    boxSizing:
                        "border-box",

                    display:
                        "flex",

                    justifyContent:
                        "space-between",

                    alignItems:
                        "center",

                    boxShadow:
                        "0 2px 8px rgba(0,0,0,0.12)"
                }}
            >

                <div>

                    <h1
                        style={{
                            margin:
                                "0",

                            fontSize:
                                "22px",

                            fontWeight:
                                "600"
                        }}
                    >
                        Smart Faculty
                    </h1>


                    <p
                        style={{
                            margin:
                                "4px 0 0",

                            fontSize:
                                "12px",

                            opacity:
                                "0.85"
                        }}
                    >
                        Timetable Management
                        System
                    </p>

                </div>


                <div
                    style={{
                        display:
                            "flex",

                        alignItems:
                            "center",

                        gap:
                            "12px"
                    }}
                >

                    {/* =================================================
                        NOTIFICATIONS BUTTON
                    ================================================= */}

                    <button
                        onClick={
                            handleNotificationClick
                        }
                        style={{
                            position:
                                "relative",

                            backgroundColor:
                                "transparent",

                            border:
                                "1px solid rgba(255,255,255,0.5)",

                            color:
                                "#ffffff",

                            borderRadius:
                                "6px",

                            padding:
                                "9px 14px",

                            cursor:
                                "pointer",

                            fontSize:
                                "14px"
                        }}
                    >

                        🔔 Notifications


                        {unreadCount > 0 && (

                            <span
                                style={{
                                    position:
                                        "absolute",

                                    top:
                                        "-8px",

                                    right:
                                        "-8px",

                                    backgroundColor:
                                        "#e74c3c",

                                    color:
                                        "#ffffff",

                                    borderRadius:
                                        "50%",

                                    minWidth:
                                        "20px",

                                    height:
                                        "20px",

                                    display:
                                        "flex",

                                    justifyContent:
                                        "center",

                                    alignItems:
                                        "center",

                                    fontSize:
                                        "11px",

                                    fontWeight:
                                        "bold",

                                    padding:
                                        "2px",

                                    boxSizing:
                                        "border-box"
                                }}
                            >
                                {unreadCount}
                            </span>

                        )}

                    </button>


                    {/* =================================================
                        LOGOUT
                    ================================================= */}

                    <button
                        onClick={
                            handleLogout
                        }
                        style={{
                            backgroundColor:
                                "#ffffff",

                            color:
                                "#23415f",

                            border:
                                "none",

                            borderRadius:
                                "6px",

                            padding:
                                "9px 18px",

                            cursor:
                                "pointer",

                            fontSize:
                                "14px",

                            fontWeight:
                                "600"
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
                        width:
                            "100%",

                        maxWidth:
                            "1250px",

                        margin:
                            "0 auto",

                        padding:
                            "20px 35px 0",

                        boxSizing:
                            "border-box"
                    }}
                >

                    <div
                        style={{
                            backgroundColor:
                                "#ffffff",

                            border:
                                "1px solid #d5e0e8",

                            borderRadius:
                                "10px",

                            padding:
                                "25px 30px",

                            boxShadow:
                                "0 2px 8px rgba(0,0,0,0.05)"
                        }}
                    >

                        <div
                            style={{
                                display:
                                    "flex",

                                justifyContent:
                                    "space-between",

                                alignItems:
                                    "center",

                                marginBottom:
                                    "20px"
                            }}
                        >

                            <h3
                                style={{
                                    margin:
                                        "0",

                                    color:
                                        "#23415f",

                                    fontSize:
                                        "19px"
                                }}
                            >
                                Notifications
                            </h3>


                            <div
                                style={{
                                    display:
                                        "flex",

                                    alignItems:
                                        "center",

                                    gap:
                                        "15px"
                                }}
                            >

                                <span
                                    style={{
                                        fontSize:
                                            "13px",

                                        color:
                                            "#687b8d"
                                    }}
                                >
                                    {unreadCount} unread
                                </span>


                                {unreadCount > 0 && (

                                    <button
                                        onClick={
                                            handleMarkAllAsRead
                                        }
                                        disabled={
                                            markingAllAsRead
                                        }
                                        style={{
                                            border:
                                                "none",

                                            backgroundColor:
                                                "#23415f",

                                            color:
                                                "#ffffff",

                                            borderRadius:
                                                "5px",

                                            padding:
                                                "8px 13px",

                                            cursor:
                                                markingAllAsRead
                                                    ? "not-allowed"
                                                    : "pointer",

                                            fontSize:
                                                "12px",

                                            fontWeight:
                                                "600",

                                            opacity:
                                                markingAllAsRead
                                                    ? 0.7
                                                    : 1
                                        }}
                                    >

                                        {markingAllAsRead
                                            ? "Marking..."
                                            : "Mark all as read"}

                                    </button>

                                )}

                            </div>

                        </div>


                        {notificationLoading ? (

                            <p
                                style={{
                                    margin:
                                        "0",

                                    color:
                                        "#687b8d",

                                    fontSize:
                                        "14px"
                                }}
                            >
                                Loading notifications...
                            </p>

                        ) : notifications.length === 0 ? (

                            <div
                                style={{
                                    padding:
                                        "25px",

                                    textAlign:
                                        "center",

                                    border:
                                        "1px solid #e4e9ed",

                                    borderRadius:
                                        "8px",

                                    backgroundColor:
                                        "#f8fafb"
                                }}
                            >

                                <p
                                    style={{
                                        margin:
                                            "0",

                                        color:
                                            "#687b8d",

                                        fontSize:
                                            "14px"
                                    }}
                                >
                                    You don't have
                                    any notifications.
                                </p>

                            </div>

                        ) : (

                            <div
                                style={{
                                    display:
                                        "flex",

                                    flexDirection:
                                        "column",

                                    gap:
                                        "10px"
                                }}
                            >

                                {notifications.map(
                                    (notification) => (

                                        <div
                                            key={
                                                notification.notificationId
                                            }
                                            onClick={() =>
                                                !notification.isRead &&
                                                handleMarkAsRead(
                                                    notification.notificationId
                                                )
                                            }
                                            style={{
                                                padding:
                                                    "16px 18px",

                                                border:
                                                    "1px solid #d5e0e8",

                                                borderRadius:
                                                    "8px",

                                                backgroundColor:
                                                    notification.isRead
                                                        ? "#f8fafb"
                                                        : "#eef5fa",

                                                cursor:
                                                    notification.isRead
                                                        ? "default"
                                                        : "pointer"
                                            }}
                                        >

                                            <div
                                                style={{
                                                    display:
                                                        "flex",

                                                    justifyContent:
                                                        "space-between",

                                                    alignItems:
                                                        "center",

                                                    marginBottom:
                                                        "7px"
                                                }}
                                            >

                                                <strong
                                                    style={{
                                                        color:
                                                            "#23415f",

                                                        fontSize:
                                                            "15px"
                                                    }}
                                                >
                                                    {
                                                        notification.title
                                                    }
                                                </strong>


                                                {!notification.isRead && (

                                                    <span
                                                        style={{
                                                            fontSize:
                                                                "11px",

                                                            backgroundColor:
                                                                "#23415f",

                                                            color:
                                                                "#ffffff",

                                                            borderRadius:
                                                                "10px",

                                                            padding:
                                                                "3px 8px"
                                                        }}
                                                    >
                                                        NEW
                                                    </span>

                                                )}

                                            </div>


                                            <p
                                                style={{
                                                    margin:
                                                        "0 0 8px",

                                                    color:
                                                        "#4d6172",

                                                    fontSize:
                                                        "14px",

                                                    lineHeight:
                                                        "1.5"
                                                }}
                                            >
                                                {
                                                    notification.message
                                                }
                                            </p>


                                            <div
                                                style={{
                                                    display:
                                                        "flex",

                                                    justifyContent:
                                                        "space-between",

                                                    alignItems:
                                                        "center"
                                                }}
                                            >

<div
    style={{
        display:
            "flex",

        justifyContent:
            "flex-end",

        alignItems:
            "center"
    }}
>
    <span
        style={{
            color:
                "#8797a5",

            fontSize:
                "11px"
        }}
    >
        {
            formatNotificationDate(
                notification.createdAt
            )
        }
    </span>
</div>


                                                <span
                                                    style={{
                                                        color:
                                                            "#8797a5",

                                                        fontSize:
                                                            "11px"
                                                    }}
                                                >
                                                    {
                                                        formatNotificationDate(
                                                            notification.createdAt
                                                        )
                                                    }
                                                </span>

                                            </div>

                                        </div>

                                    )
                                )}

                            </div>

                        )}

                    </div>

                </div>

            )}


            {/* =================================================
                MAIN CONTENT
            ================================================= */}

            <main
                style={{
                    width:
                        "100%",

                    maxWidth:
                        "1250px",

                    margin:
                        "0 auto",

                    padding:
                        "30px 35px 50px",

                    boxSizing:
                        "border-box"
                }}
            >

                {/* =================================================
                    WELCOME
                ================================================= */}

                <div
                    style={{
                        backgroundColor:
                            "#ffffff",

                        border:
                            "1px solid #d5e0e8",

                        borderRadius:
                            "10px",

                        padding:
                            "25px 30px",

                        marginBottom:
                            "25px",

                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.05)"
                    }}
                >

                    <h2
                        style={{
                            margin:
                                "0 0 8px",

                            fontSize:
                                "26px",

                            color:
                                "#23415f"
                        }}
                    >
                        Student Dashboard
                    </h2>


                    <p
                        style={{
                            margin:
                                "0",

                            color:
                                "#687b8d",

                            fontSize:
                                "15px"
                        }}
                    >
                        Welcome{" "}

                        <strong>
                            {student?.studentName ||
                                student?.student_name ||
                                username}
                        </strong>
                    </p>

                </div>


                {/* =================================================
                    MY ACADEMIC INFORMATION
                ================================================= */}

                <div
                    style={{
                        backgroundColor:
                            "#ffffff",

                        border:
                            "1px solid #d5e0e8",

                        borderRadius:
                            "10px",

                        padding:
                            "25px 30px",

                        marginBottom:
                            "25px",

                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.05)"
                    }}
                >

                    <h3
                        style={{
                            margin:
                                "0 0 22px",

                            color:
                                "#23415f",

                            fontSize:
                                "19px"
                        }}
                    >
                        My Academic Information
                    </h3>


                    <div
                        style={{
                            display:
                                "grid",

                            gridTemplateColumns:
                                "repeat(3, 1fr)",

                            gap:
                                "25px"
                        }}
                    >

                        <div>

                            <div
                                style={{
                                    fontSize:
                                        "12px",

                                    color:
                                        "#8797a5",

                                    marginBottom:
                                        "6px"
                                }}
                            >
                                Registration Number
                            </div>


                            <div
                                style={{
                                    fontWeight:
                                        "600",

                                    fontSize:
                                        "15px"
                                }}
                            >
                                {student?.registrationNumber ||
                                    student?.registration_number ||
                                    "-"}
                            </div>

                        </div>


                        <div>

                            <div
                                style={{
                                    fontSize:
                                        "12px",

                                    color:
                                        "#8797a5",

                                    marginBottom:
                                        "6px"
                                }}
                            >
                                Academic Year
                            </div>


                            <div
                                style={{
                                    fontWeight:
                                        "600",

                                    fontSize:
                                        "15px"
                                }}
                            >
                                {formatYear(
                                    student?.yearLevel ||
                                    student?.year_level ||
                                    student?.academicYear
                                )}
                            </div>

                        </div>


                        <div>

                            <div
                                style={{
                                    fontSize:
                                        "12px",

                                    color:
                                        "#8797a5",

                                    marginBottom:
                                        "6px"
                                }}
                            >
                                Semester
                            </div>


                            <div
                                style={{
                                    fontWeight:
                                        "600",

                                    fontSize:
                                        "15px"
                                }}
                            >
                                {formatSemester(
                                    student?.semester
                                )}
                            </div>

                        </div>

                    </div>

                </div>


                {/* =================================================
                    MY SUBJECTS
                ================================================= */}

                <section
                    style={{
                        backgroundColor:
                            "#ffffff",

                        border:
                            "1px solid #d5e0e8",

                        borderRadius:
                            "10px",

                        padding:
                            "25px 30px",

                        marginBottom:
                            "25px",

                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.05)"
                    }}
                >

                    <StudentRegisteredSubjects
                        key={
                            student?.studentId ||
                            student?.student_id ||
                            username
                        }
                    />

                </section>


                {/* =================================================
                    STUDENT TIMETABLE
                ================================================= */}

                <section
                    style={{
                        backgroundColor:
                            "#ffffff",

                        border:
                            "1px solid #d5e0e8",

                        borderRadius:
                            "10px",

                        padding:
                            "25px 30px",

                        marginBottom:
                            "25px",

                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.05)"
                    }}
                >

                    <StudentTimetable />

                </section>

            </main>
            {/* =================================================
                CHANGE PASSWORD
               ================================================= */}

<section
    style={{
        marginBottom:
            "25px"
    }}
>

    <ChangePassword />

</section>


            {/* =================================================
                FOOTER
            ================================================= */}

            <footer
                style={{
                    textAlign:
                        "center",

                    padding:
                        "20px",

                    color:
                        "#8797a5",

                    fontSize:
                        "12px"
                }}
            >
                Smart Faculty Timetable Management System
            </footer>

        </div>
    );
}

export default StudentDashboard;