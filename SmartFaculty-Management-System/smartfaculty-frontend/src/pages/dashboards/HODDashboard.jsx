import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import "./HODDashboard.css";
import ChangePassword from "../ChangePassword";
import StatusDialog from "../../components/StatusDialog";
import ConfirmDialog from "../../components/ConfirmDialog";

function HODDashboard() {

    // =====================================================
    // STATES
    // =====================================================

    const [allocations, setAllocations] = useState([]);
    const [lecturers, setLecturers] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [departments, setDepartments] = useState([]);

    // =====================================================
// HOD'S PERSONAL LECTURER DATA
// =====================================================

const [myAllocations, setMyAllocations] = useState([]);
const [myTimetables, setMyTimetables] = useState([]);
const [myChangeRequests, setMyChangeRequests] = useState([]);

const [myAllocationLoading, setMyAllocationLoading] = useState(false);
const [myTimetableLoading, setMyTimetableLoading] = useState(false);
const [myChangeRequestLoading, setMyChangeRequestLoading] = useState(false);

const [myAllocationError, setMyAllocationError] = useState("");
const [myTimetableError, setMyTimetableError] = useState("");
const [myChangeRequestError, setMyChangeRequestError] = useState("");
const [success, setSuccess] = useState("");


// =====================================================
// HOD CHANGE REQUEST FORM
// =====================================================
const [editingMyRequestId, setEditingMyRequestId] = useState(null);
const [showMyRequestForm, setShowMyRequestForm] = useState(false);
const [selectedMyTimetable, setSelectedMyTimetable] = useState(null);


const [myRequestReason, setMyRequestReason] = useState("");
const [myRequestedDay, setMyRequestedDay] = useState("");
const [myRequestedStartTime, setMyRequestedStartTime] = useState("");
const [myRequestedEndTime, setMyRequestedEndTime] = useState("");
const [myRequestedClassroom, setMyRequestedClassroom] = useState("");

const [showDeleteDialog, setShowDeleteDialog] = useState(false);
const [selectedRequestId, setSelectedRequestId] = useState(null);
const [requestActionMessage, setRequestActionMessage] = useState("");

const [myRequestSubmitting, setMyRequestSubmitting] = useState(false);

    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const [error, setError] = useState("");

    // Form
    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState(null);

    const [selectedLecturer, setSelectedLecturer] = useState("");
    const [selectedYear, setSelectedYear] = useState("");
    const [selectedSemester, setSelectedSemester] = useState("");
    const [selectedSubject, setSelectedSubject] = useState("");

    // Filters
    const [filterYear, setFilterYear] = useState("");
    const [filterSemester, setFilterSemester] = useState("");
    const [searchText, setSearchText] = useState("");

    // Notification
    const [unreadNotifications, setUnreadNotifications] = useState(0);
    const [hodNotifications, setHodNotifications] = useState([]);
    const [showNotifications, setShowNotifications] = useState(false);
    const [notificationLoading, setNotificationLoading] = useState(false);
    const [notificationError, setNotificationError] = useState("");

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
    // TOKEN / USER
    // =====================================================

    const token = localStorage.getItem("token");

    const username =
        localStorage.getItem("username") || "HOD";


    const authConfig = token
        ? {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
        : {};


    // =====================================================
    // LOAD DASHBOARD
    // =====================================================

    useEffect(() => {
        loadDashboard();
    }, []);


    // Load only the unread notification count.
    // Notifications themselves remain hidden until the HOD clicks the button.
    useEffect(() => {

    if (lecturers.length > 0) {

        loadUnreadHODNotificationCount();

        loadHODPersonalData();
    }

}, [lecturers]);


    const loadDashboard = async () => {

        setLoading(true);
        setError("");

        try {

            // =================================================
            // IMPORTANT:
            // Load ONLY the logged-in HOD's personalized data.
            //
            // Example:
            // hod_ict
            // ->
            // /api/hod/dashboard/hod_ict
            //
            // Backend returns:
            // - HOD department
            // - department lecturers
            // - department subjects
            // - department allocations
            // =================================================

            const [hodResponse, departmentsResponse] = await Promise.all([
                axios.get(
                    `http://localhost:8080/api/hod/dashboard/${username}`,
                    authConfig
                ),
                axios.get(
                    "http://localhost:8080/api/departments",
                    authConfig
                )
            ]);


            const data = hodResponse.data;

            const allDepartments =
                Array.isArray(departmentsResponse.data)
                    ? departmentsResponse.data
                    : [];


            console.log(
                "HOD dashboard data:",
                data
            );


            // =================================================
            // PERSONALIZED ALLOCATIONS
            // =================================================

            setAllocations(
                Array.isArray(data.allocations)
                    ? data.allocations
                    : []
            );


            // =================================================
            // PERSONALIZED LECTURERS
            // =================================================

            setLecturers(
                Array.isArray(data.lecturers)
                    ? data.lecturers
                    : []
            );


            // =================================================
            // PERSONALIZED SUBJECTS
            // =================================================

            setSubjects(
                Array.isArray(data.subjects)
                    ? data.subjects
                    : []
            );


            // =================================================
            // HOD'S DEPARTMENT
            // =================================================
            //
            // There is only ONE department for this HOD.
            //
            // Example:
            // hod_ict -> DIT
            //
            // We put it into an array because the existing
            // dashboard uses departments.length.
            // =================================================

            setDepartments(allDepartments);


        } catch (err) {

            console.error(
                "HOD dashboard loading error:",
                err
            );


            console.error(
                "Server response:",
                err.response?.data
            );


            setError(
                err.response?.data?.message ||
                "Unable to load HOD dashboard data."
            );


        } finally {

            setLoading(false);

        }
    };
    // =====================================================
// LOAD HOD PERSONAL LECTURER DATA
// =====================================================

const loadHODPersonalData = async () => {

    const lecturerId = getHODLecturerId();

    if (!lecturerId) {

        console.error(
            "HOD lecturer ID could not be found."
        );

        return;
    }


    await Promise.all([
        loadHODPersonalAllocations(lecturerId),
        loadHODPersonalTimetable(lecturerId),
        loadHODPersonalChangeRequests(lecturerId)
    ]);
};


// =====================================================
// LOAD HOD'S PERSONAL ALLOCATIONS
// =====================================================

const loadHODPersonalAllocations = async (lecturerId) => {

    try {

        setMyAllocationLoading(true);
        setMyAllocationError("");

        const response = await axios.get(
            `http://localhost:8080/api/lecturer-allocations/lecturer/${lecturerId}`,
            authConfig
        );

        console.log(
            "HOD personal allocations:",
            response.data
        );

        setMyAllocations(
            Array.isArray(response.data)
                ? response.data
                : []
        );

    } catch (err) {

        console.error(
            "HOD personal allocation loading error:",
            err
        );

        console.error(
            "Server response:",
            err.response?.data
        );

        setMyAllocations([]);

        setMyAllocationError(
            "Unable to load your teaching allocations."
        );

    } finally {

        setMyAllocationLoading(false);
    }
};


// =====================================================
// LOAD HOD'S PERSONAL TIMETABLE
// =====================================================

const loadHODPersonalTimetable = async (lecturerId) => {

    try {

        setMyTimetableLoading(true);
        setMyTimetableError("");

        const response = await axios.get(
            `http://localhost:8080/api/timetables/lecturer/${lecturerId}`,
            authConfig
        );

        console.log(
            "HOD personal timetable:",
            response.data
        );

        setMyTimetables(
            Array.isArray(response.data)
                ? response.data
                : []
        );

    } catch (err) {

        console.error(
            "HOD personal timetable loading error:",
            err
        );

        console.error(
            "Server response:",
            err.response?.data
        );

        setMyTimetables([]);

        setMyTimetableError(
            "Unable to load your timetable."
        );

    } finally {

        setMyTimetableLoading(false);
    }
};


// =====================================================
// LOAD HOD'S PERSONAL CHANGE REQUESTS
// =====================================================

const loadHODPersonalChangeRequests = async (lecturerId) => {

    try {

        setMyChangeRequestLoading(true);
        setMyChangeRequestError("");

        const response = await axios.get(
            `http://localhost:8080/api/timetable-change-requests/lecturer/${lecturerId}`,
            authConfig
        );

        console.log(
            "HOD personal change requests:",
            response.data
        );

        setMyChangeRequests(
            Array.isArray(response.data)
                ? response.data
                : []
        );

    } catch (err) {

        console.error(
            "HOD personal change request loading error:",
            err
        );

        console.error(
            "Server response:",
            err.response?.data
        );

        setMyChangeRequests([]);

        setMyChangeRequestError(
            "Unable to load your timetable change requests."
        );

    } finally {

        setMyChangeRequestLoading(false);
    }
};


    // =====================================================
    // SUBJECT HELPERS
    // =====================================================

    const getSubjectCode = (subject) => {

        return (
            subject?.subjectCode ||
            subject?.subject_code ||
            "-"
        );
    };


    const getSubjectName = (subject) => {

        return (
            subject?.subjectName ||
            subject?.subject_name ||
            ""
        );
    };


    const getLecturerName = (lecturer) => {

        return (
            lecturer?.lecturerName ||
            lecturer?.lecturer_name ||
            lecturer?.user?.username ||
            lecturer?.email ||
            "-"
        );
    };

    // =====================================================
    // PERSONAL TIMETABLE SUBJECT CODE
    // =====================================================

const getMySubjectCode = (item) => {

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
// PERSONAL TIMETABLE YEAR
// =====================================================

const getMyYear = (item) => {

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
        return text.replace(
            "YEAR_",
            "Year "
        );
    }


    if (
        text.toLowerCase().startsWith("year")
    ) {
        return text;
    }


    return text;
};


// =====================================================
// PERSONAL TIMETABLE SEMESTER
// =====================================================

const getMySemester = (item) => {

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
// PERSONAL TIMETABLE DAY
// =====================================================

const getMyDay = (timetable) => {

    return (
        timetable?.timeSlot?.day ||
        timetable?.day ||
        "-"
    );
};


// =====================================================
// PERSONAL TIMETABLE TIME
// =====================================================

const formatMyTime = (time) => {

    if (!time) {
        return "-";
    }

    const text = String(time);

    return text.length >= 5
        ? text.substring(0, 5)
        : text;
};


const getMyStartTime = (timetable) => {

    return formatMyTime(
        timetable?.timeSlot?.startTime ||
        timetable?.startTime
    );
};


const getMyEndTime = (timetable) => {

    return formatMyTime(
        timetable?.timeSlot?.endTime ||
        timetable?.endTime
    );
};


// =====================================================
// PERSONAL TIMETABLE CLASSROOM
// =====================================================

const getMyClassroom = (timetable) => {

    return (
        timetable?.classroom?.roomCode ||
        timetable?.classroom?.room_code ||
        timetable?.roomCode ||
        timetable?.room_code ||
        "-"
    );
};


// =====================================================
// CHANGE REQUEST SUBJECT
// =====================================================

const getMyRequestSubject = (request) => {

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
// CHANGE REQUEST DATE
// =====================================================

const formatMyRequestDate = (dateTime) => {

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
// OPEN HOD PERSONAL CHANGE REQUEST FORM
// =====================================================

const openMyRequestForm = (timetable) => {

    setSelectedMyTimetable(timetable);

    setMyRequestReason("");
    setMyRequestedDay("");
    setMyRequestedStartTime("");
    setMyRequestedEndTime("");


    const currentClassroom =
        getMyClassroom(timetable);


    setMyRequestedClassroom(
        currentClassroom === "-"
            ? ""
            : currentClassroom
    );


    setShowMyRequestForm(true);


    setTimeout(() => {

        window.scrollTo({
            top: document.body.scrollHeight,
            behavior: "smooth"
        });

    }, 100);
};
// =====================================================
// EDIT HOD PERSONAL CHANGE REQUEST
// =====================================================

const editMyChangeRequest = (request) => {

    if (request?.status !== "PENDING") {
        return;
    }

    setEditingMyRequestId(
        request?.requestId || request?.request_id
    );

    setSelectedMyTimetable(
        request?.timetable || null
    );

    setMyRequestReason(
        request?.reason || ""
    );

    setMyRequestedDay(
        request?.requestedDay || ""
    );

    setMyRequestedStartTime(
        request?.requestedStartTime
            ? formatMyTime(request.requestedStartTime)
            : ""
    );

    setMyRequestedEndTime(
        request?.requestedEndTime
            ? formatMyTime(request.requestedEndTime)
            : ""
    );

    setMyRequestedClassroom(
        request?.requestedClassroom || ""
    );

    setShowMyRequestForm(true);

    window.scrollTo({
        top: document.body.scrollHeight,
        behavior: "smooth"
    });
};

// =====================================================
// CLOSE HOD PERSONAL CHANGE REQUEST FORM
// =====================================================

// =====================================================
// CLOSE HOD PERSONAL CHANGE REQUEST FORM
// =====================================================

const closeMyRequestForm = () => {

    if (myRequestSubmitting) {
        return;
    }

    setShowMyRequestForm(false);

    setSelectedMyTimetable(null);
    setEditingMyRequestId(null);

    setMyRequestReason("");
    setMyRequestedDay("");
    setMyRequestedStartTime("");
    setMyRequestedEndTime("");
    setMyRequestedClassroom("");
};


// =====================================================
// SUBMIT HOD PERSONAL CHANGE REQUEST
// =====================================================

// =====================================================
// SUBMIT / UPDATE HOD PERSONAL CHANGE REQUEST
// =====================================================

const submitMyRequest = async () => {

    if (!myRequestReason.trim()) {
        alert("Please enter a reason.");
        return;
    }

    const lecturerId = getHODLecturerId();

    const timetableId =
        selectedMyTimetable?.timetableId ||
        selectedMyTimetable?.timetable_id;

    if (!lecturerId) {
        alert("HOD lecturer ID could not be found.");
        return;
    }

    if (!editingMyRequestId && !timetableId) {
        alert("Timetable ID could not be found.");
        return;
    }

    try {

        setMyRequestSubmitting(true);

        const requestData = {

            lecturer: {
                lecturerId: lecturerId
            },

            ...(timetableId
                ? {
                    timetable: {
                        timetableId: timetableId
                    }
                }
                : {}),

            reason: myRequestReason.trim(),

            requestedDay:
                myRequestedDay || null,

            requestedStartTime:
                myRequestedStartTime || null,

            requestedEndTime:
                myRequestedEndTime || null,

            requestedClassroom:
                myRequestedClassroom || null,

            status: "PENDING"
        };

        if (editingMyRequestId) {

            // UPDATE EXISTING REQUEST

            await axios.put(
                `http://localhost:8080/api/timetable-change-requests/${editingMyRequestId}`,
                requestData,
                authConfig
            );

            setSuccess(
                "Timetable change request updated successfully."
            );

        } else {

            // CREATE NEW REQUEST

            await axios.post(
                "http://localhost:8080/api/timetable-change-requests",
                requestData,
                authConfig
            );

            setSuccess(
                "Timetable change request submitted successfully."
            );
        }

        closeMyRequestForm();

        await loadHODPersonalChangeRequests(
            lecturerId
        );

    } catch (err) {

        console.error(
            "HOD change request submission/update error:",
            err
        );

        console.error(
            "Server response:",
            err.response?.data
        );

        alert(
            err.response?.data?.message ||
            "Unable to save timetable change request."
        );

    } finally {

        setMyRequestSubmitting(false);
    }
};

    // =====================================================
    // LOGGED-IN HOD INFORMATION
    // =====================================================

    const hodLecturer = useMemo(() => {

        return lecturers.find(
            (lecturer) =>
                lecturer?.user?.role === "HOD" ||
                lecturer?.user?.role?.name === "HOD" ||
                lecturer?.user?.username === username ||
                lecturer?.username === username ||
                lecturer?.email === username
        ) || null;

    }, [lecturers, username]);


    const getHODName = () => {

        return (
            hodLecturer?.lecturerName ||
            hodLecturer?.lecturer_name ||
            username
        );
    };


    const getHODDepartmentName = () => {

        return (
            hodLecturer?.department?.departmentName ||
            hodLecturer?.department?.department_name ||
            hodLecturer?.departmentName ||
            hodLecturer?.department_name ||
            "-"
        );
    };


    // =====================================================
    // FILTER SUBJECTS BY YEAR + SEMESTER
    // =====================================================

    const filteredSubjectsForForm = useMemo(() => {

        // Do not show subjects until both year
        // and semester are selected.

        if (
            selectedYear === "" ||
            selectedSemester === ""
        ) {
            return [];
        }


        return subjects.filter((subject) => {

            const subjectYear =
                Number(
                    subject?.academicYear ??
                    subject?.academic_year
                );


            const subjectSemester =
                Number(
                    subject?.semester
                );


            return (
                subjectYear === Number(selectedYear) &&
                subjectSemester === Number(selectedSemester)
            );

        });

    }, [
        subjects,
        selectedYear,
        selectedSemester
    ]);


    // =====================================================
    // FILTER ALLOCATIONS
    // =====================================================

    const filteredAllocations = useMemo(() => {

        const search =
            searchText
                .trim()
                .toLowerCase();


        return allocations.filter((allocation) => {

            const year =
                Number(
                    allocation?.academicYear
                );


            const semester =
                Number(
                    allocation?.semester
                );


            const lecturer =
                getLecturerName(
                    allocation?.lecturer
                ).toLowerCase();


            const subject =
                getSubjectCode(
                    allocation?.subject
                ).toLowerCase();


            const matchesYear =
                filterYear === "" ||
                year === Number(filterYear);


            const matchesSemester =
                filterSemester === "" ||
                semester === Number(filterSemester);


            const matchesSearch =
                search === "" ||
                lecturer.includes(search) ||
                subject.includes(search);


            return (
                matchesYear &&
                matchesSemester &&
                matchesSearch
            );

        });

    }, [
        allocations,
        filterYear,
        filterSemester,
        searchText
    ]);


    // =====================================================
    // DASHBOARD COUNTS
    // =====================================================

    const totalAllocations =
        allocations.length;


    const totalLecturers =
        lecturers.length;


    const totalSubjects =
        subjects.length;


    const totalDepartments =
        departments.length;


    // =====================================================
    // OPEN ADD FORM
    // =====================================================

    const openAddForm = () => {

        setEditingId(null);

        setSelectedLecturer("");
        setSelectedYear("");
        setSelectedSemester("");
        setSelectedSubject("");

        setShowForm(true);
    };


    // =====================================================
    // OPEN EDIT FORM
    // =====================================================

    const openEditForm = (allocation) => {

        setEditingId(
            allocation.allocationId
        );


        setSelectedLecturer(
            allocation?.lecturer?.lecturerId
                ? String(
                    allocation.lecturer.lecturerId
                )
                : ""
        );


        setSelectedYear(
            allocation?.academicYear !== null &&
            allocation?.academicYear !== undefined
                ? String(
                    allocation.academicYear
                )
                : ""
        );


        setSelectedSemester(
            allocation?.semester !== null &&
            allocation?.semester !== undefined
                ? String(
                    allocation.semester
                )
                : ""
        );


        setSelectedSubject(
            allocation?.subject?.subjectId
                ? String(
                    allocation.subject.subjectId
                )
                : ""
        );


        setShowForm(true);
    };


    // =====================================================
    // CLOSE FORM
    // =====================================================

    const closeForm = () => {

        setShowForm(false);

        setEditingId(null);

        setSelectedLecturer("");
        setSelectedYear("");
        setSelectedSemester("");
        setSelectedSubject("");
    };


    // =====================================================
    // YEAR CHANGE
    // =====================================================

    const handleYearChange = (e) => {

        const value =
            e.target.value;


        setSelectedYear(value);


        // Reset subject because the year changed.
        setSelectedSubject("");
    };


    // =====================================================
    // SEMESTER CHANGE
    // =====================================================

    const handleSemesterChange = (e) => {

        const value =
            e.target.value;


        setSelectedSemester(value);


        // Reset subject because the semester changed.
        setSelectedSubject("");
    };


    // =====================================================
    // SAVE ALLOCATION
    // =====================================================

    const saveAllocation = async (e) => {

        e.preventDefault();

        setError("");


        // =================================================
        // VALIDATE LECTURER
        // =================================================

        if (!selectedLecturer) {

            alert(
                "Please select a lecturer."
            );

            return;
        }


        // =================================================
        // VALIDATE YEAR
        // =================================================

        if (!selectedYear) {

            alert(
                "Please select an academic year."
            );

            return;
        }


        // =================================================
        // VALIDATE SEMESTER
        // =================================================

        if (!selectedSemester) {

            alert(
                "Please select a semester."
            );

            return;
        }


        // =================================================
        // VALIDATE SUBJECT
        // =================================================

        if (!selectedSubject) {

            alert(
                "Please select a subject."
            );

            return;
        }


        // =================================================
        // CREATE REQUEST DATA
        // =================================================

        const allocationData = {

            lecturer: {
                lecturerId:
                    Number(selectedLecturer)
            },

            subject: {
                subjectId:
                    Number(selectedSubject)
            },

            academicYear:
                Number(selectedYear),

            semester:
                Number(selectedSemester)
        };


        try {

            setSaving(true);


            // =================================================
            // UPDATE EXISTING ALLOCATION
            // =================================================

            // =================================================
// UPDATE EXISTING ALLOCATION
// =================================================

if (editingId) {

    await axios.put(
        `http://localhost:8080/api/lecturer-allocations/${editingId}`,
        allocationData,
        authConfig
    );

    setSuccess(
        "Lecturer allocation updated successfully."
    );

} else {

                // =================================================
                // CREATE NEW ALLOCATION
                // =================================================

                await axios.post(
                    "http://localhost:8080/api/lecturer-allocations",
                    allocationData,
                    authConfig
                );


                setSuccess("Lecturer allocation added successfully.");
            }


            // Close form
            closeForm();


            // Reload personalized HOD data
            await loadDashboard();


        } catch (err) {

            console.error(
                "Saving allocation error:",
                err
            );


            console.error(
                "Server response:",
                err.response?.data
            );


            setError(
    err.response?.data?.message ||
    "Unable to save lecturer allocation."
);


        } finally {

            setSaving(false);

        }
    };


    // =====================================================
    // DELETE ALLOCATION
    // =====================================================

    const deleteAllocation = (id) => {
    showConfirmDialog(
        "Are you sure you want to delete this lecturer allocation?",
        async () => {
            try {
                setError("");
                setSuccess("");

                await axios.delete(
                    `http://localhost:8080/api/lecturer-allocations/${id}`,
                    authConfig
                );

                setSuccess("Lecturer allocation deleted successfully.");

                await loadDashboard();

            } catch (err) {
                console.error("Delete allocation error:", err);

                setError(
                    err.response?.data?.message ||
                    "Unable to delete lecturer allocation."
                );
            }
        }
    );
};
// =====================================================
// OPEN DELETE CONFIRMATION DIALOG
// =====================================================

const handleDeleteMyRequestClick = (requestId) => {

    setSelectedRequestId(requestId);

    setShowDeleteDialog(true);
};


// =====================================================
// CLOSE DELETE CONFIRMATION DIALOG
// =====================================================

const closeDeleteMyRequestDialog = () => {

    setShowDeleteDialog(false);

    setSelectedRequestId(null);
};


// =====================================================
// CONFIRM AND DELETE CHANGE REQUEST
// =====================================================

const confirmDeleteMyRequest = async () => {

    if (!selectedRequestId) {
        return;
    }

    const lecturerId = getHODLecturerId();

    try {

        await axios.delete(
            `http://localhost:8080/api/timetable-change-requests/${selectedRequestId}`,
            authConfig
        );

        closeDeleteMyRequestDialog();

        setRequestActionMessage(
            "Timetable change request deleted successfully."
        );

        await loadHODPersonalChangeRequests(
            lecturerId
        );

    } catch (err) {

        console.error(
            "HOD delete change request error:",
            err
        );

        console.error(
            "Server response:",
            err.response?.data
        );

        setRequestActionMessage(
            err.response?.data?.message ||
            err.response?.data ||
            "Unable to delete timetable change request."
        );
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
    // FIND LOGGED-IN HOD LECTURER ID
    // =====================================================

    const getHODLecturerId = () => {

        const hodLecturer = lecturers.find(
            (lecturer) =>
                lecturer?.user?.role === "HOD" ||
                lecturer?.user?.role?.name === "HOD" ||
                lecturer?.user?.username === username ||
                lecturer?.username === username ||
                lecturer?.email === username
        );

        return (
            hodLecturer?.lecturerId ||
            hodLecturer?.lecturer_id ||
            null
        );
    };


    // =====================================================
    // LOAD UNREAD HOD NOTIFICATION COUNT ONLY
    // =====================================================

    const loadUnreadHODNotificationCount = async () => {

        const lecturerId = getHODLecturerId();

        if (!lecturerId) {
            return;
        }

        try {

            const response = await axios.get(
                `http://localhost:8080/api/notifications/hod/${lecturerId}/unread`,
                authConfig
            );

            const notifications =
                Array.isArray(response.data)
                    ? response.data
                    : [];

            setUnreadNotifications(notifications.length);

        } catch (err) {

            console.error(
                "HOD unread notification count loading error:",
                err
            );

            setUnreadNotifications(0);
        }
    };


    // =====================================================
    // LOAD HOD NOTIFICATIONS
    // =====================================================

    const loadHODNotifications = async () => {

        const lecturerId = getHODLecturerId();

        if (!lecturerId) {
            setNotificationError(
                "HOD lecturer information could not be found."
            );
            return;
        }

        try {

            setNotificationLoading(true);
            setNotificationError("");

            const response = await axios.get(
                `http://localhost:8080/api/notifications/hod/${lecturerId}`,
                authConfig
            );

            const notifications =
                Array.isArray(response.data)
                    ? response.data
                    : [];

            setHodNotifications(notifications);

            setUnreadNotifications(
                notifications.filter(
                    (notification) =>
                        notification?.isRead !== true
                ).length
            );

        } catch (err) {

            console.error(
                "HOD notification loading error:",
                err
            );

            setHodNotifications([]);
            setUnreadNotifications(0);

            setNotificationError(
                err.response?.data?.message ||
                "Unable to load HOD notifications."
            );

        } finally {

            setNotificationLoading(false);
        }
    };


    // =====================================================
    // OPEN / CLOSE NOTIFICATIONS
    // =====================================================

    const handleNotifications = async () => {

        const nextState = !showNotifications;

        setShowNotifications(nextState);

        if (nextState) {
            await loadHODNotifications();
        }
    };


    // =====================================================
    // MARK ONE HOD NOTIFICATION AS READ
    // =====================================================

    const markHODNotificationAsRead = async (notification) => {

        const notificationId =
            notification?.notificationId ||
            notification?.id;

        if (!notificationId || notification?.isRead === true) {
            return;
        }

        try {

            await axios.put(
                `http://localhost:8080/api/notifications/${notificationId}`,
                {},
                authConfig
            );

            setHodNotifications((previous) =>
                previous.map((item) =>
                    (item?.notificationId || item?.id) ===
                    notificationId
                        ? { ...item, isRead: true }
                        : item
                )
            );

            setUnreadNotifications((previous) =>
                Math.max(0, previous - 1)
            );

        } catch (err) {

            console.error(
                "Mark HOD notification as read error:",
                err
            );
        }
    };


    // =====================================================
    // MARK ALL HOD NOTIFICATIONS AS READ
    // =====================================================

    const markAllHODNotificationsAsRead = async () => {

        const lecturerId = getHODLecturerId();

        if (!lecturerId) {
            return;
        }

        try {

            await axios.put(
                `http://localhost:8080/api/notifications/hod/${lecturerId}/read-all`,
                {},
                authConfig
            );

            setHodNotifications((previous) =>
                previous.map((item) => ({
                    ...item,
                    isRead: true
                }))
            );

            setUnreadNotifications(0);

        } catch (err) {

            console.error(
                "Mark all HOD notifications as read error:",
                err
            );

            setNotificationError(
                err.response?.data?.message ||
                "Unable to mark notifications as read."
            );
        }
    };


    // =====================================================
    // LOADING
    // =====================================================

    if (loading) {

        return (
            <div className="hod-page">

                <div className="hod-loading">

                    Loading HOD dashboard...

                </div>

            </div>
        );
    }


    // =====================================================
    // MAIN DASHBOARD
    // =====================================================

    return (

        <div className="hod-page">

            {/* =================================================
                HEADER
            ================================================= */}

            <header className="hod-header">

                <div className="hod-brand">

                    <h1>
                        Smart Faculty
                    </h1>

                    <p>
                        Timetable Management System
                    </p>

                </div>


                <div className="hod-header-actions">

                    <button
                        className="hod-notification-button"
                        onClick={handleNotifications}
                        type="button"
                    >

                        <span>
                            Notification
                        </span>


                        {unreadNotifications > 0 && (

                            <span className="hod-notification-badge">

                                {unreadNotifications}

                            </span>

                        )}

                    </button>


                    <button
                        className="hod-logout-button"
                        onClick={handleLogout}
                        type="button"
                    >
                        Logout
                    </button>

                </div>

            </header>


            {/* =============================================
                HOD NOTIFICATIONS

                IMPORTANT: This section is intentionally OUTSIDE
                the main dashboard content and appears directly
                BELOW THE HEADER and ABOVE the HOD Dashboard.
            ============================================= */}

            {showNotifications && (

                <div className="hod-notification-container">

                    <section className="hod-section hod-notification-section">

                        <div className="hod-section-header">
                            <div>
                                <h2>Notifications</h2>
                                
                            </div>

                            <div className="hod-notification-actions">
                                <button
                                    type="button"
                                    className="hod-primary-button"
                                    onClick={loadHODNotifications}
                                    disabled={notificationLoading}
                                >
                                    {notificationLoading ? "Refreshing..." : "Refresh"}
                                </button>

                                {unreadNotifications > 0 && (
                                    <button
                                        type="button"
                                        className="hod-secondary-button"
                                        onClick={markAllHODNotificationsAsRead}
                                    >
                                        Mark All as Read
                                    </button>
                                )}

                                <button
                                    type="button"
                                    className="hod-secondary-button"
                                    onClick={() => setShowNotifications(false)}
                                >
                                    Close
                                </button>
                            </div>
                        </div>

                        {notificationError && (
                            <div className="hod-error">
                                {notificationError}
                            </div>
                        )}

                        {notificationLoading ? (
                            <div className="hod-empty-cell">Loading notifications...</div>
                        ) : hodNotifications.length === 0 ? (
                            <div className="hod-empty-cell">No notifications found.</div>
                        ) : (
                            <div className="hod-notification-list">
                                {hodNotifications.map((notification, index) => (
                                    <div
                                        key={notification?.notificationId || notification?.id || index}
                                        onClick={() => markHODNotificationAsRead(notification)}
                                        className={
                                            notification?.isRead === true
                                                ? "hod-notification-card"
                                                : "hod-notification-card hod-notification-unread"
                                        }
                                    >
                                        <div className="hod-notification-card-header">
                                            

                                            {notification?.isRead !== true && (
                                                <span className="hod-new-badge">NEW</span>
                                            )}
                                        </div>

                                        <p className="hod-notification-message">
                                            {notification?.message || ""}
                                        </p>

                                        <small className="hod-notification-date">
                                            {notification?.createdAt
                                                ? new Date(notification.createdAt).toLocaleString()
                                                : ""}
                                        </small>
                                    </div>
                                ))}
                            </div>
                        )}

                    </section>
                    
                

                </div>

            )}


            {/* =================================================
                CONTENT
            ================================================= */}

            <main className="hod-content">


                {/* =============================================
                    TITLE CARD
                ============================================= */}

                <section className="hod-welcome-card">

                    <h2>
                        HOD Dashboard
                    </h2>


                    <p>

                        Welcome{" "}

                        <strong>
                            {getHODName()}
                        </strong>

                    </p>

                </section>


                {/* =============================================
                    ERROR
                ============================================= */}

                {error && (

                    <div className="hod-error">
                        {error}
                    </div>

                )}

                {/* =============================================
                    SUMMARY CARDS
                ============================================= */}

                <section className="hod-summary-grid">

                    <div className="hod-summary-card">

                        <div className="hod-summary-label">
                            Total Allocations
                        </div>


                        <div className="hod-summary-number">
                            {totalAllocations}
                        </div>

                    </div>


                    <div className="hod-summary-card">

                        <div className="hod-summary-label">
                            Total Lecturers
                        </div>


                        <div className="hod-summary-number">
                            {totalLecturers}
                        </div>

                    </div>


                    <div className="hod-summary-card">

                        <div className="hod-summary-label">
                            Total Subjects
                        </div>


                        <div className="hod-summary-number">
                            {totalSubjects}
                        </div>

                    </div>


                    <div className="hod-summary-card">

                        <div className="hod-summary-label">
                            Total Departments
                        </div>


                        <div className="hod-summary-number">
                            {totalDepartments}
                        </div>

                    </div>

                </section>


                {/* =============================================
                    MY INFORMATION
                ============================================= */}

                <section className="hod-information-card">

                    <h3>My Information</h3>

                    <div className="hod-information-grid">

                        <div className="hod-information-item">
                            <div className="hod-information-label">Lecturer Code</div>
                            <div className="hod-information-value">
                                {hodLecturer?.lecturerCode || hodLecturer?.lecturer_code || "-"}
                            </div>
                        </div>

                        <div className="hod-information-item">
                            <div className="hod-information-label">Lecturer Name</div>
                            <div className="hod-information-value">
                                {getHODName()}
                            </div>
                        </div>

                        <div className="hod-information-item">
                            <div className="hod-information-label">Email</div>
                            <div className="hod-information-value hod-information-email">
                                {hodLecturer?.email || "-"}
                            </div>
                        </div>

                        <div className="hod-information-item">
                            <div className="hod-information-label">Department</div>
                            <div className="hod-information-value">
                                {getHODDepartmentName()}
                            </div>
                        </div>

                    </div>

                </section>
                

                {/* =============================================
                    TEACHING ALLOCATIONS
                ============================================= */}

                <section className="hod-section">

                    <div className="hod-section-header">

                        <div>

                            <h2>
                                Teaching Allocations
                            </h2>


                            

                        </div>


                        <button
                            className="hod-primary-button"
                            onClick={openAddForm}
                            type="button"
                        >
                            Add New Allocation
                        </button>

                    </div>

                    


                    {/* =========================================
                        FILTERS
                    ========================================= */}

                    <div className="hod-filters">

                        <div className="hod-filter-group">

                            <label>
                                Academic Year
                            </label>


                            <select
                                value={filterYear}
                                onChange={(e) =>
                                    setFilterYear(
                                        e.target.value
                                    )
                                }
                            >

                                <option value="">
                                    All Years
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

                        </div>


                        <div className="hod-filter-group">

                            <label>
                                Semester
                            </label>


                            <select
                                value={filterSemester}
                                onChange={(e) =>
                                    setFilterSemester(
                                        e.target.value
                                    )
                                }
                            >

                                <option value="">
                                    All Semesters
                                </option>


                                <option value="1">
                                    Semester 1
                                </option>


                                <option value="2">
                                    Semester 2
                                </option>

                            </select>

                        </div>


                        <div className="hod-filter-group hod-search-group">

                            <label>
                                Search
                            </label>


                            <input
                                type="text"
                                value={searchText}
                                onChange={(e) =>
                                    setSearchText(
                                        e.target.value
                                    )
                                }
                                placeholder="Search lecturer or subject..."
                            />

                        </div>


                        <button
                            className="hod-clear-button"
                            type="button"
                            onClick={() => {

                                setFilterYear("");
                                setFilterSemester("");
                                setSearchText("");

                            }}
                        >
                            Clear
                        </button>

                    </div>


                    {/* =========================================
                        TABLE
                    ========================================= */}

                    <div className="hod-table-wrapper">

                        <table className="hod-allocation-table">

                            <thead>

                                <tr>

                                    <th className="col-number">
                                        #
                                    </th>


                                    <th className="col-lecturer">
                                        Lecturer
                                    </th>


                                    <th className="col-subject">
                                        Subject
                                    </th>


                                    <th className="col-year">
                                        Academic Year
                                    </th>


                                    <th className="col-semester">
                                        Semester
                                    </th>


                                    <th className="col-actions">
                                        Actions
                                    </th>

                                </tr>

                            </thead>


                            <tbody>

                                {filteredAllocations.length === 0 ? (

                                    <tr>

                                        <td
                                            colSpan="6"
                                            className="hod-empty-cell"
                                        >
                                            No lecturer allocations found.
                                        </td>

                                    </tr>

                                ) : (

                                    filteredAllocations.map(
                                        (allocation, index) => (

                                            <tr
                                                key={
                                                    allocation.allocationId
                                                }
                                            >

                                                <td className="col-number">

                                                    {index + 1}

                                                </td>


                                                <td className="col-lecturer">

                                                    <strong>

                                                        {getLecturerName(
                                                            allocation.lecturer
                                                        )}

                                                    </strong>

                                                </td>


                                                <td className="col-subject">

                                                    <strong>

                                                        {getSubjectCode(
                                                            allocation.subject
                                                        )}

                                                    </strong>


                                                    <span className="hod-subject-name">

                                                        {getSubjectName(
                                                            allocation.subject
                                                        )}

                                                    </span>

                                                </td>


                                                <td className="col-year">

                                                    Year{" "}

                                                    {
                                                        allocation.academicYear
                                                    }

                                                </td>


                                                <td className="col-semester">

                                                    Semester{" "}

                                                    {
                                                        allocation.semester
                                                    }

                                                </td>


                                                <td className="col-actions">

                                                    <div className="hod-action-buttons">

                                                        <button
                                                            className="hod-edit-button"
                                                            type="button"
                                                            onClick={() =>
                                                                openEditForm(
                                                                    allocation
                                                                )
                                                            }
                                                        >
                                                            Edit
                                                        </button>


                                                        <button
                                                            className="hod-delete-button"
                                                            type="button"
                                                            onClick={() =>
                                                                deleteAllocation(
                                                                    allocation.allocationId
                                                                )
                                                            }
                                                        >
                                                            Delete
                                                        </button>

                                                    </div>

                                                </td>

                                            </tr>

                                        )
                                    )

                                )}

                            </tbody>

                        </table>

                    </div>


                    <div className="hod-result-count">

                        Showing{" "}

                        <strong>
                            {filteredAllocations.length}
                        </strong>{" "}

                        of{" "}

                        <strong>
                            {totalAllocations}
                        </strong>{" "}

                        allocations

                    </div>

                </section>


                {/* =============================================
                    ADD / EDIT FORM
                ============================================= */}

                {showForm && (

                    <section className="hod-section hod-form-section">

                        <div className="hod-section-header">

                            <div>

                                <h2>

                                    {editingId
                                        ? "Edit Teaching Allocation"
                                        : "Add Teaching Allocation"}

                                </h2>


                            </div>

                        </div>


                        <form
                            className="hod-allocation-form"
                            onSubmit={saveAllocation}
                        >

                            {/* =================================
                                LECTURER
                            ================================= */}

                            <div className="hod-form-group">

                                <label>
                                    Lecturer
                                </label>


                                <select
                                    value={selectedLecturer}
                                    onChange={(e) =>
                                        setSelectedLecturer(
                                            e.target.value
                                        )
                                    }
                                    required
                                >

                                    <option value="">
                                        Select Lecturer
                                    </option>


                                    {lecturers.map(
                                        (lecturer) => (

                                            <option
                                                key={
                                                    lecturer.lecturerId
                                                }
                                                value={
                                                    lecturer.lecturerId
                                                }
                                            >

                                                {getLecturerName(
                                                    lecturer
                                                )}


                                                {lecturer.lecturerCode
                                                    ? ` (${lecturer.lecturerCode})`
                                                    : ""}

                                            </option>

                                        )
                                    )}

                                </select>

                            </div>


                            {/* =================================
                                YEAR
                            ================================= */}

                            <div className="hod-form-group">

                                <label>
                                    Academic Year
                                </label>


                                <select
                                    value={selectedYear}
                                    onChange={handleYearChange}
                                    required
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

                            </div>


                            {/* =================================
                                SEMESTER
                            ================================= */}

                            <div className="hod-form-group">

                                <label>
                                    Semester
                                </label>


                                <select
                                    value={selectedSemester}
                                    onChange={handleSemesterChange}
                                    required
                                >

                                    <option value="">
                                        Select Semester
                                    </option>


                                    <option value="1">
                                        Semester 1
                                    </option>


                                    <option value="2">
                                        Semester 2
                                    </option>

                                </select>

                            </div>


                            {/* =================================
                                SUBJECT
                            ================================= */}

                            <div className="hod-form-group">

                                <label>
                                    Subject
                                </label>


                                <select
                                    value={selectedSubject}
                                    onChange={(e) =>
                                        setSelectedSubject(
                                            e.target.value
                                        )
                                    }
                                    disabled={
                                        !selectedYear ||
                                        !selectedSemester
                                    }
                                    required
                                >

                                    <option value="">

                                        {!selectedYear ||
                                        !selectedSemester
                                            ? "Select year and semester first"
                                            : filteredSubjectsForForm.length === 0
                                                ? "No subjects available"
                                                : "Select Subject"}

                                    </option>


                                    {filteredSubjectsForForm.map(
                                        (subject) => (

                                            <option
                                                key={
                                                    subject.subjectId
                                                }
                                                value={
                                                    subject.subjectId
                                                }
                                            >

                                                {getSubjectCode(
                                                    subject
                                                )}

                                                {" - "}

                                                {getSubjectName(
                                                    subject
                                                )}

                                            </option>

                                        )
                                    )}

                                </select>


                                {selectedYear &&
                                    selectedSemester && (

                                        <small className="hod-form-help">

                                            {
                                                filteredSubjectsForForm.length
                                            }{" "}

                                            subject(s) available for{" "}

                                            Year{" "}
                                            {selectedYear},{" "}

                                            Semester{" "}
                                            {selectedSemester}

                                        </small>

                                    )}

                            </div>


                            {/* =================================
                                BUTTONS
                            ================================= */}

                            <div className="hod-form-actions">

                                <button
                                    type="submit"
                                    className="hod-primary-button"
                                    disabled={saving}
                                >

                                    {saving
                                        ? "Saving..."
                                        : editingId
                                            ? "Update Allocation"
                                            : "Add Allocation"}

                                </button>


                                <button
                                    type="button"
                                    className="hod-secondary-button"
                                    onClick={closeForm}
                                    disabled={saving}
                                >
                                    Cancel
                                </button>

                            </div>

                        </form>

                    </section>

                )}

                {/* =============================================
                     MY TEACHING ALLOCATION
                    ============================================= */}

<section className="hod-section">

    <div className="hod-section-header">

        <div>

            <h2>
                My Teaching Allocation
            </h2>

        </div>


        <button
            className="hod-primary-button"
            type="button"
            onClick={() => {

                const lecturerId =
                    getHODLecturerId();

                if (lecturerId) {
                    loadHODPersonalAllocations(
                        lecturerId
                    );
                }

            }}
            disabled={myAllocationLoading}
        >
            {myAllocationLoading
                ? "Refreshing..."
                : "Refresh"}
        </button>

    </div>


    {myAllocationLoading ? (

        <div className="hod-empty-cell">
            Loading your teaching allocations...
        </div>

    ) : myAllocationError ? (

        <div className="hod-error">
            {myAllocationError}
        </div>

    ) : myAllocations.length === 0 ? (

        <div className="hod-empty-cell">
            No teaching allocations found.
        </div>

    ) : (

        <div className="hod-table-wrapper">

            <table className="hod-allocation-table">

                <thead>

                    <tr>

                        <th>
                            #
                        </th>

                        <th>
                            Subject
                        </th>

                        <th>
                            Academic Year
                        </th>

                        <th>
                            Semester
                        </th>

                    </tr>

                </thead>


                <tbody>

                    {myAllocations.map(
                        (allocation, index) => (

                            <tr
                                key={
                                    allocation?.allocationId ||
                                    index
                                }
                            >

                                <td>
                                    {index + 1}
                                </td>


                                <td>

                                    <strong>
                                        {getSubjectCode(
                                            allocation.subject
                                        )}
                                    </strong>

                                    <br />

                                    <span className="hod-subject-name">
                                        {getSubjectName(
                                            allocation.subject
                                        )}
                                    </span>

                                </td>


                                <td>

                                    Year{" "}

                                    {
                                        allocation?.academicYear ??
                                        "-"
                                    }

                                </td>


                                <td>

                                    Semester{" "}

                                    {
                                        allocation?.semester ??
                                        "-"
                                    }

                                </td>

                            </tr>

                        )
                    )}

                </tbody>

            </table>

        </div>

    )}

</section>

{/* =============================================
    MY TIMETABLE
============================================= */}

<section className="hod-section">

    <div className="hod-section-header">

        <div>

            <h2>
                My Timetable
            </h2>

        </div>


        <button
            className="hod-primary-button"
            type="button"
            onClick={() => {

                const lecturerId =
                    getHODLecturerId();

                if (lecturerId) {
                    loadHODPersonalTimetable(
                        lecturerId
                    );
                }

            }}
            disabled={myTimetableLoading}
        >
            {myTimetableLoading
                ? "Refreshing..."
                : "Refresh"}
        </button>

    </div>


    {myTimetableLoading ? (

        <div className="hod-empty-cell">
            Loading your timetable...
        </div>

    ) : myTimetableError ? (

        <div className="hod-error">
            {myTimetableError}
        </div>

    ) : myTimetables.length === 0 ? (

        <div className="hod-empty-cell">
            No timetable records found.
        </div>

    ) : (

        <div className="hod-table-wrapper">

            <table className="hod-allocation-table">

                <thead>

                    <tr>

                        <th>
                            #
                        </th>

                        <th>
                            Subject
                        </th>

                        <th>
                            Year
                        </th>

                        <th>
                            Semester
                        </th>

                        <th>
                            Day
                        </th>

                        <th>
                            Time
                        </th>

                        <th>
                            Classroom
                        </th>

                        <th>
                            Action
                        </th>

                    </tr>

                </thead>


                <tbody>

                    {myTimetables.map(
                        (timetable, index) => (

                            <tr
                                key={
                                    timetable?.timetableId ||
                                    timetable?.timetable_id ||
                                    index
                                }
                            >

                                <td>
                                    {index + 1}
                                </td>


                                <td>

                                    <strong>
                                        {getMySubjectCode(
                                            timetable
                                        )}
                                    </strong>

                                </td>


                                <td>
                                    {getMyYear(
                                        timetable
                                    )}
                                </td>


                                <td>
                                    {getMySemester(
                                        timetable
                                    )}
                                </td>


                                <td>

                                    <strong>
                                        {getMyDay(
                                            timetable
                                        )}
                                    </strong>

                                </td>


                                <td>

                                    {getMyStartTime(
                                        timetable
                                    )}

                                    {" - "}

                                    {getMyEndTime(
                                        timetable
                                    )}

                                </td>


                                <td>

                                    <strong>
                                        {getMyClassroom(
                                            timetable
                                        )}
                                    </strong>

                                </td>


                                <td>

                                    <button
                                        type="button"
                                        className="hod-primary-button"
                                        onClick={() =>
                                            openMyRequestForm(
                                                timetable
                                            )
                                        }
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

</section>

{/* =============================================
    MY CHANGE REQUEST FORM
============================================= */}

{showMyRequestForm && (

    <section className="hod-section">

        <div className="hod-section-header">

            <div>

               <h2>
    {editingMyRequestId
        ? "Edit Timetable Change Request"
        : "Request Timetable Change"}
</h2>

            </div>

        </div>


        {selectedMyTimetable && (

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

                {getMySubjectCode(
                    selectedMyTimetable
                )}

                <br />

                <strong>
                    Current Day:
                </strong>{" "}

                {getMyDay(
                    selectedMyTimetable
                )}

                <br />

                <strong>
                    Current Time:
                </strong>{" "}

                {getMyStartTime(
                    selectedMyTimetable
                )}

                {" - "}

                {getMyEndTime(
                    selectedMyTimetable
                )}

                <br />

                <strong>
                    Current Classroom:
                </strong>{" "}

                {getMyClassroom(
                    selectedMyTimetable
                )}

            </div>

        )}


        <div className="hod-allocation-form">

            <div className="hod-form-group">

                <label>
                    Requested Day
                </label>

                <select
                    value={myRequestedDay}
                    onChange={(e) =>
                        setMyRequestedDay(
                            e.target.value
                        )
                    }
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


            <div className="hod-form-group">

                <label>
                    Requested Classroom
                </label>

                <input
                    type="text"
                    value={myRequestedClassroom}
                    onChange={(e) =>
                        setMyRequestedClassroom(
                            e.target.value
                        )
                    }
                    placeholder="Example: C1"
                />

            </div>


            <div className="hod-form-group">

                <label>
                    Requested Start Time
                </label>

                <input
                    type="time"
                    value={myRequestedStartTime}
                    onChange={(e) =>
                        setMyRequestedStartTime(
                            e.target.value
                        )
                    }
                />

            </div>


            <div className="hod-form-group">

                <label>
                    Requested End Time
                </label>

                <input
                    type="time"
                    value={myRequestedEndTime}
                    onChange={(e) =>
                        setMyRequestedEndTime(
                            e.target.value
                        )
                    }
                />

            </div>


            <div
                className="hod-form-group"
                style={{
                    gridColumn: "1 / -1"
                }}
            >

                <label>
                    Reason
                </label>

                <textarea
                    value={myRequestReason}
                    onChange={(e) =>
                        setMyRequestReason(
                            e.target.value
                        )
                    }
                    rows="4"
                    placeholder="Enter the reason for the timetable change..."
                />

            </div>


            <div className="hod-form-actions">

                <button
                    type="button"
                    className="hod-secondary-button"
                    onClick={closeMyRequestForm}
                    disabled={myRequestSubmitting}
                >
                    Cancel
                </button>


                <button
                    type="button"
                    className="hod-primary-button"
                    onClick={submitMyRequest}
                    disabled={myRequestSubmitting}
                >
                    {myRequestSubmitting
    ? "Saving..."
    : editingMyRequestId
        ? "Update Request"
        : "Submit Request"}
                </button>

            </div>

        </div>

    </section>

)}

{/* =============================================
    MY CHANGE REQUESTS
============================================= */}

<section className="hod-section">

    <div className="hod-section-header">

        <div>

            <h2>
                My Timetable Change Requests
            </h2>

        </div>


        <button
            className="hod-primary-button"
            type="button"
            onClick={() => {

                const lecturerId =
                    getHODLecturerId();

                if (lecturerId) {
                    loadHODPersonalChangeRequests(
                        lecturerId
                    );
                }

            }}
            disabled={myChangeRequestLoading}
        >
            {myChangeRequestLoading
                ? "Refreshing..."
                : "Refresh Requests"}
        </button>

    </div>


    {myChangeRequestLoading ? (

        <div className="hod-empty-cell">
            Loading change requests...
        </div>

    ) : myChangeRequestError ? (

        <div className="hod-error">
            {myChangeRequestError}
        </div>

    ) : myChangeRequests.length === 0 ? (

        <div className="hod-empty-cell">
            No timetable change requests found.
        </div>

    ) : (

        <div className="hod-table-wrapper">

            <table className="hod-allocation-table">

                <thead>

                    <tr>

                        <th>
                            #
                        </th>

                        <th>
                            Subject
                        </th>

                        <th>
                            Requested Day
                        </th>

                        <th>
                            Requested Time
                        </th>

                        <th>
                            Requested Classroom
                        </th>

                        <th>
                            Status
                        </th>

                        <th>
                            Requested At
                        </th>

                        <th>
                            Reason
                        </th>

                        <th>
                          Actions
                        </th>

                    </tr>

                </thead>


                <tbody>

                    {myChangeRequests.map(
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

                                    <td>
                                        {index + 1}
                                    </td>


                                    <td>

                                        <strong>
                                            {getMyRequestSubject(
                                                request
                                            )}
                                        </strong>

                                    </td>


                                    <td>

                                        {request?.requestedDay ||
                                            "-"}

                                    </td>


                                    <td>

                                        {request?.requestedStartTime
                                            ? formatMyTime(
                                                request.requestedStartTime
                                            )
                                            : "-"}

                                        {" - "}

                                        {request?.requestedEndTime
                                            ? formatMyTime(
                                                request.requestedEndTime
                                            )
                                            : "-"}

                                    </td>


                                    <td>

                                        <strong>
                                            {
                                                request?.requestedClassroom ||
                                                "-"
                                            }
                                        </strong>

                                    </td>


                                    <td>

                                        <span
                                            style={{
                                                display: "inline-block",
                                                padding: "5px 10px",
                                                borderRadius: "12px",
                                                fontSize: "11px",
                                                fontWeight: "700",

                                                backgroundColor:
                                                    status === "APPROVED"
                                                        ? "#e8f5e9"
                                                        : status === "REJECTED"
                                                            ? "#fdecea"
                                                            : "#fff4e5",

                                                color:
                                                    status === "APPROVED"
                                                        ? "#2e7d32"
                                                        : status === "REJECTED"
                                                            ? "#c0392b"
                                                            : "#b26a00"
                                            }}
                                        >
                                            {status}
                                        </span>

                                    </td>


                                    <td>

                                        {formatMyRequestDate(
                                            request?.createdAt
                                        )}

                                    </td>


                                    <td
                                        style={{
                                            whiteSpace: "normal",
                                            textAlign: "left",
                                            lineHeight: "1.5"
                                        }}
                                    >

                                        {
                                            request?.reason ||
                                            "-"
                                        }

                                    </td>
                                    {/* ACTION BUTTONS */}

<td>

    {status === "PENDING" ? (

        <div
            className="hod-action-buttons"
            style={{
                display: "flex",
                gap: "8px"
            }}
        >

            <button
    className="my-request-edit-btn"
    onClick={() => editMyChangeRequest(request)}
>
    Edit
</button>

<button
    className="my-request-delete-btn"
    onClick={() =>
        handleDeleteMyRequestClick(
            request?.requestId || request?.request_id
        )
    }
>
    Delete
</button>

        </div>

    ) : (

        <span style={{ color: "#888" }}>
            No actions available
        </span>

    )}

</td>

                                </tr>

                            );

                        }
                    )}

                </tbody>

            </table>

            {requestActionMessage && (

    <div
        style={{
            padding: "12px 15px",
            marginBottom: "15px",
            borderRadius: "6px",
            backgroundColor:
                requestActionMessage.includes("successfully")
                    ? "#e8f5e9"
                    : "#fdecea",
            color:
                requestActionMessage.includes("successfully")
                    ? "#2e7d32"
                    : "#c0392b",
            fontWeight: "600"
        }}
    >
        {requestActionMessage}
    </div>

)}

        </div>
        

    )}

</section>

                


                

            {/* =================================================
                CHANGE PASSWORD
            ================================================= */}

            <section style={{ marginTop: "25px" }}>
                <ChangePassword />
            </section>


            </main>


            {/* =================================================
                FOOTER
            ================================================= */}

            <footer className="hod-footer">
                Smart Faculty Timetable Management System
            </footer>
            {/* =================================================
    DELETE CHANGE REQUEST CONFIRMATION DIALOG
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


            <h2
                style={{
                    marginBottom: "12px",
                    color: "#23415f",
                    fontSize: "22px"
                }}
            >
                Confirm Deletion
            </h2>


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


            <div
                style={{
                    display: "flex",
                    justifyContent: "center",
                    gap: "12px"
                }}
            >

                <button
                    type="button"
                    onClick={closeDeleteMyRequestDialog}
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


                <button
                    type="button"
                    onClick={confirmDeleteMyRequest}
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

            <StatusDialog
    type="success"
    message={success}
    onClose={() => setSuccess("")}
/>

            {/* =============================================
                CONFIRMATION DIALOG
            ============================================= */}

            <ConfirmDialog
                open={confirmDialog.open}
                message={confirmDialog.message}
                onConfirm={handleConfirmAction}
                onCancel={closeConfirmDialog}
            />


            {/* =============================================
                SUCCESS / ERROR DIALOG
            ============================================= */}

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

export default HODDashboard;