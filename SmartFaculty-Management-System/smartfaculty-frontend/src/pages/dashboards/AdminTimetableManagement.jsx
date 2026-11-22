import { useEffect, useState } from "react";
import axios from "axios";
import ConfirmDialog from "../../components/ConfirmDialog";
import StatusDialog from "../../components/StatusDialog";

function AdminTimetableManagement() {

    // =====================================================
    // STATES
    // =====================================================

    const [timetable, setTimetable] = useState([]);

    const [allocations, setAllocations] = useState([]);
    const [classrooms, setClassrooms] = useState([]);
    const [timeSlots, setTimeSlots] = useState([]);
    const [specializations, setSpecializations] = useState([]);

    const [academicYear, setAcademicYear] = useState("1");
    const [semester, setSemester] = useState("1");

    const [loading, setLoading] = useState(true);
    const [formLoading, setFormLoading] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    // =====================================================
    // CONFIRMATION DIALOG
    // =====================================================

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

    const [showForm, setShowForm] = useState(false);
    const [editingId, setEditingId] = useState(null);

    const [allocationId, setAllocationId] = useState("");
    const [classroomId, setClassroomId] = useState("");
    const [timeSlotId, setTimeSlotId] = useState("");
    const [specializationId, setSpecializationId] = useState("");


    // =====================================================
    // TOKEN
    // =====================================================

    const token =
        localStorage.getItem("token");


    // =====================================================
    // AXIOS CONFIG
    // =====================================================

    const getConfig = () => {

        return token
            ? {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
            : {};
    };


    // =====================================================
    // INITIAL LOAD
    // =====================================================

    useEffect(() => {

        loadInitialData();

    }, []);


    // =====================================================
    // LOAD TIMETABLE
    // =====================================================

    useEffect(() => {

        loadTimetable();

    }, [academicYear, semester]);


    // =====================================================
    // LOAD INITIAL DATA
    // =====================================================

    const loadInitialData = async () => {

        try {

            setError("");

            await Promise.all([
                loadAllocations(),
                loadClassrooms(),
                loadTimeSlots(),
                loadSpecializations()
            ]);

        } catch (err) {

            console.error(
                "Initial data loading error:",
                err
            );

            setError(
                "Unable to load timetable management data."
            );
        }
    };


    // =====================================================
    // LOAD ALLOCATIONS
    // =====================================================

    const loadAllocations = async () => {

        try {

            const response =
                await axios.get(
                    "http://localhost:8080/api/lecturer-allocations",
                    getConfig()
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

            throw err;
        }
    };


    // =====================================================
    // LOAD CLASSROOMS
    // =====================================================

    const loadClassrooms = async () => {

        try {

            const response =
                await axios.get(
                    "http://localhost:8080/api/classrooms",
                    getConfig()
                );

            setClassrooms(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Classroom loading error:",
                err
            );

            throw err;
        }
    };


    // =====================================================
    // LOAD TIME SLOTS
    // =====================================================

    const loadTimeSlots = async () => {

        try {

            const response =
                await axios.get(
                    "http://localhost:8080/api/time-slots",
                    getConfig()
                );

            setTimeSlots(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Time slot loading error:",
                err
            );

            throw err;
        }
    };


    // =====================================================
    // LOAD SPECIALIZATIONS
    // =====================================================

    const loadSpecializations = async () => {

        try {

            const response =
                await axios.get(
                    "http://localhost:8080/api/specializations",
                    getConfig()
                );

            setSpecializations(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );

        } catch (err) {

            console.error(
                "Specialization loading error:",
                err
            );

            throw err;
        }
    };


    // =====================================================
    // LOAD TIMETABLE
    // =====================================================

    const loadTimetable = async () => {

        setLoading(true);
        setError("");

        try {

            const response =
                await axios.get(
                    `http://localhost:8080/api/timetables/year/YEAR_${academicYear}/semester/SEMESTER_${semester}`,
                    getConfig()
                );

            setTimetable(
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
                "Server response:",
                err.response?.data
            );

            setError(
                typeof err.response?.data === "string"
                    ? err.response.data
                    : "Unable to load timetable."
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // GET SUBJECT CODE
    // =====================================================

    const getSubjectCode = (entry) => {

        return (
            entry?.lecturerAllocation?.subject?.subjectCode ||
            entry?.lecturerAllocation?.subject?.subject_code ||
            "-"
        );
    };


    // =====================================================
    // GET SUBJECT NAME
    // =====================================================

    const getSubjectName = (entry) => {

        return (
            entry?.lecturerAllocation?.subject?.subjectName ||
            entry?.lecturerAllocation?.subject?.subject_name ||
            ""
        );
    };


    // =====================================================
    // GET LECTURER NAME
    // =====================================================

    const getLecturerName = (entry) => {

        return (
            entry?.lecturerAllocation?.lecturer?.lecturerName ||
            entry?.lecturerAllocation?.lecturer?.lecturer_name ||
            "-"
        );
    };


    // =====================================================
    // GET ROOM
    // =====================================================

    const getRoom = (entry) => {

        return (
            entry?.classroom?.roomCode ||
            entry?.classroom?.roomName ||
            "-"
        );
    };


    // =====================================================
    // GET SPECIALIZATION
    // =====================================================

    const getSpecializationName = (entry) => {

        return (
            entry?.specialization?.specializationName ||
            entry?.specialization?.name ||
            entry?.specialization?.specializationCode ||
            "-"
        );
    };


    // =====================================================
    // GET DAY
    // =====================================================

    const getDay = (entry) => {

        return (
            entry?.timeSlot?.day ||
            "-"
        );
    };


    // =====================================================
    // GET START TIME
    // =====================================================

    const getStartTime = (entry) => {

        return (
            entry?.timeSlot?.startTime ||
            "-"
        );
    };


    // =====================================================
    // GET END TIME
    // =====================================================

    const getEndTime = (entry) => {

        return (
            entry?.timeSlot?.endTime ||
            "-"
        );
    };


    // =====================================================
    // GET ALLOCATION LABEL
    // =====================================================

    const getAllocationLabel = (allocation) => {

        const subjectCode =
            allocation?.subject?.subjectCode ||
            allocation?.subject?.subject_code ||
            "Subject";


        const lecturerName =
            allocation?.lecturer?.lecturerName ||
            allocation?.lecturer?.lecturer_name ||
            "Lecturer";


        return (
            `${subjectCode} - ${lecturerName}`
        );
    };


    // =====================================================
    // GET SPECIALIZATION CODE FOR SUBJECT
    // =====================================================

    const getAllocationSpecializationCode = (allocation) => {

        const subjectCode =
            allocation?.subject?.subjectCode ||
            allocation?.subject?.subject_code ||
            "";

        const match = String(subjectCode)
            .toUpperCase()
            .match(/^[A-Z]+/);

        return match ? match[0] : "";
    };


    // =====================================================
    // FILTER SPECIALIZATIONS FOR SELECTED ALLOCATION
    // =====================================================

    const getAvailableSpecializations = () => {

        const selectedAllocation =
            allocations.find(
                (allocation) =>
                    String(allocation?.allocationId) ===
                    String(allocationId)
            );

        if (!selectedAllocation) {
            return specializations;
        }

        const specializationCode =
            getAllocationSpecializationCode(
                selectedAllocation
            );

        if (!specializationCode) {
            return specializations;
        }

        const matchingSpecializations =
            specializations.filter(
                (specialization) => {
                    const code =
                        specialization?.specializationCode ||
                        specialization?.specialization_code ||
                        "";

                    return String(code)
                        .toUpperCase() ===
                        specializationCode;
                }
            );

        return matchingSpecializations.length > 0
            ? matchingSpecializations
            : specializations;
    };


    // =====================================================
    // GET TIME SLOT LABEL
    // =====================================================

    const getTimeSlotLabel = (timeSlot) => {

        const day =
            timeSlot?.day || "";


        const start =
            timeSlot?.startTime || "";


        const end =
            timeSlot?.endTime || "";


        return (
            `${day} ${start} - ${end}`
        );
    };


    // =====================================================
    // OPEN ADD FORM
    // =====================================================

    const openAddForm = () => {

        setEditingId(null);

        setAllocationId("");
        setClassroomId("");
        setTimeSlotId("");
        setSpecializationId("");

        setError("");
        setSuccess("");

        setShowForm(true);
    };


    // =====================================================
    // OPEN EDIT FORM
    // =====================================================

    const openEditForm = (entry) => {

        setEditingId(
            entry?.timetableId
        );


        setAllocationId(
            entry?.lecturerAllocation?.allocationId
                ? String(
                    entry.lecturerAllocation.allocationId
                )
                : ""
        );


        setClassroomId(
            entry?.classroom?.classroomId
                ? String(
                    entry.classroom.classroomId
                )
                : ""
        );


        setTimeSlotId(
            entry?.timeSlot?.timeSlotId
                ? String(
                    entry.timeSlot.timeSlotId
                )
                : ""
        );


        setSpecializationId(
            entry?.specialization?.specializationId
                ? String(
                    entry.specialization.specializationId
                )
                : ""
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

        setEditingId(null);

        setAllocationId("");
        setClassroomId("");
        setTimeSlotId("");
        setSpecializationId("");

        setError("");
    };


    // =====================================================
    // SUBMIT FORM
    // =====================================================

    const submitForm = async (event) => {

        event.preventDefault();

        setError("");
        setSuccess("");


        // =================================================
        // VALIDATION
        // =================================================

        if (!allocationId) {

            setError(
                "Please select a lecturer allocation."
            );

            return;
        }


        if (!classroomId) {

            setError(
                "Please select a classroom."
            );

            return;
        }


        if (!timeSlotId) {

            setError(
                "Please select a time slot."
            );

            return;
        }


        if (!specializationId) {

            setError(
                "Please select a specialization."
            );

            return;
        }


        // =================================================
        // FIND SELECTED OBJECTS
        // =================================================

        const selectedAllocation =
            allocations.find(
                (allocation) =>
                    String(
                        allocation.allocationId
                    ) === String(
                        allocationId
                    )
            );


        const selectedClassroom =
            classrooms.find(
                (classroom) =>
                    String(
                        classroom.classroomId
                    ) === String(
                        classroomId
                    )
            );


        const selectedTimeSlot =
            timeSlots.find(
                (timeSlot) =>
                    String(
                        timeSlot.timeSlotId
                    ) === String(
                        timeSlotId
                    )
            );


        const selectedSpecialization =
            specializations.find(
                (specialization) =>
                    String(
                        specialization.specializationId
                    ) === String(
                        specializationId
                    )
            );


        if (!selectedAllocation) {

            setError(
                "Selected lecturer allocation could not be found."
            );

            return;
        }


        if (!selectedClassroom) {

            setError(
                "Selected classroom could not be found."
            );

            return;
        }


        if (!selectedTimeSlot) {

            setError(
                "Selected time slot could not be found."
            );

            return;
        }


        if (!selectedSpecialization) {

            setError(
                "Selected specialization could not be found."
            );

            return;
        }


        // =================================================
        // REQUEST DATA
        // =================================================

        const timetableData = {

            lecturerAllocation: {

                allocationId:
                    Number(
                        allocationId
                    )
            },


            classroom: {

                classroomId:
                    Number(
                        classroomId
                    )
            },


            timeSlot: {

                timeSlotId:
                    Number(
                        timeSlotId
                    )
            },


            specialization: {

                specializationId:
                    Number(
                        specializationId
                    )
            },


            yearLevel:
                `YEAR_${academicYear}`,


            semester:
                `SEMESTER_${semester}`,


            active: true
        };


        console.log(
            "Timetable data:",
            timetableData
        );


        // =================================================
        // SAVE
        // =================================================

        try {

            setFormLoading(true);


            // =================================================
            // CREATE
            // =================================================

            if (!editingId) {

                await axios.post(
                    "http://localhost:8080/api/timetables",
                    timetableData,
                    getConfig()
                );


                setSuccess(
                    "Timetable entry added successfully."
                );
            }


            // =================================================
            // UPDATE
            // =================================================

            else {

                await axios.put(
                    `http://localhost:8080/api/timetables/${editingId}`,
                    timetableData,
                    getConfig()
                );


                setSuccess(
                    "Timetable entry updated successfully."
                );
            }


            // =================================================
            // RELOAD
            // =================================================

            await loadTimetable();


            // =================================================
            // CLOSE FORM
            // =================================================

            setShowForm(false);

            setEditingId(null);

            setAllocationId("");
            setClassroomId("");
            setTimeSlotId("");
            setSpecializationId("");


        } catch (err) {

            console.error(
                "Timetable save error:",
                err
            );


            console.error(
                "Server response:",
                err.response?.data
            );


            setError(
                typeof err.response?.data === "string"
                    ? err.response.data
                    : "Unable to save timetable entry."
            );

        } finally {

            setFormLoading(false);
        }
    };


    // =====================================================
    // DELETE
    // =====================================================


const deleteEntry = (id) => {
    showConfirmDialog(
        "Are you sure you want to delete this timetable entry?",
        async () => {
            setError("");
            setSuccess("");

            try {
                const response = await axios.delete(
                    `http://localhost:8080/api/timetables/${id}`,
                    getConfig()
                );

                const message =
                    typeof response.data === "string"
                        ? response.data
                        : response.data?.message ||
                          "Timetable removal completed successfully.";

                setSuccess(message);

                await loadTimetable();

            } catch (err) {
                console.error("Timetable delete error:", err);
                console.error("Server response:", err.response?.data);

                setError(
                    err.response?.data?.message ||
                    (typeof err.response?.data === "string"
                        ? err.response.data
                        : "Unable to remove timetable entry.")
                );
            }
        }
    );
};


    // =====================================================
    // SORT TIMETABLE
    // =====================================================

    const dayOrder = [
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday"
    ];


    const sortedTimetable =
        [...timetable].sort(
            (a, b) => {

                const dayDifference =
                    dayOrder.indexOf(
                        getDay(a)
                    ) -
                    dayOrder.indexOf(
                        getDay(b)
                    );


                if (dayDifference !== 0) {

                    return dayDifference;
                }


                return getStartTime(a)
                    .localeCompare(
                        getStartTime(b)
                    );
            }
        );


    // =====================================================
    // FILTER ALLOCATIONS
    // =====================================================

    const filteredAllocations =
        allocations.filter(
            (allocation) => {

                return (
                    Number(
                        allocation?.academicYear
                    ) ===
                    Number(
                        academicYear
                    )
                    &&
                    Number(
                        allocation?.semester
                    ) ===
                    Number(
                        semester
                    )
                );
            }
        );


    // =====================================================
    // FILTER TIME SLOTS
    // =====================================================

    const sortedTimeSlots =
        [...timeSlots].sort(
            (a, b) => {

                const dayDifference =
                    dayOrder.indexOf(
                        a.day
                    ) -
                    dayOrder.indexOf(
                        b.day
                    );


                if (dayDifference !== 0) {

                    return dayDifference;
                }


                return String(
                    a.startTime || ""
                ).localeCompare(
                    String(
                        b.startTime || ""
                    )
                );
            }
        );


    // =====================================================
    // MAIN UI
    // =====================================================

    return (

        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#f5f6fa",
                padding: "30px"
            }}
        >

            {/* =================================================
                HEADER
            ================================================= */}

            <div
                style={{
                    backgroundColor: "#ffffff",
                    padding: "25px",
                    borderRadius: "12px",
                    marginBottom: "25px",
                    boxShadow:
                        "0 2px 8px rgba(0,0,0,0.08)"
                }}
            >

                <h3
                    style={{
                        margin: "0 0 8px 0",
                        color: "#111827"
                    }}
                >
                    Timetable Management
                </h3>


                

            </div>


            {/* =================================================
                MESSAGES
            ================================================= */}

            {error && (

                <div
                    style={{
                        backgroundColor: "#ffe6e6",
                        color: "#b00020",
                        padding: "14px",
                        borderRadius: "8px",
                        marginBottom: "20px"
                    }}
                >
                    {error}
                </div>

            )}


            {success && (

                <div
                    style={{
                        backgroundColor: "#e7f7ed",
                        color: "#18794e",
                        padding: "14px",
                        borderRadius: "8px",
                        marginBottom: "20px"
                    }}
                >
                    {success}
                </div>

            )}

            


            {/* =================================================
                FILTERS
            ================================================= */}

            <div
                style={{
                    backgroundColor: "#ffffff",
                    padding: "25px",
                    borderRadius: "12px",
                    marginBottom: "25px",
                    boxShadow:
                        "0 2px 8px rgba(0,0,0,0.08)"
                }}
            >

                <div
                    style={{
                        display: "flex",
                        gap: "15px",
                        alignItems: "end",
                        flexWrap: "wrap"
                    }}
                >
                    

                    {/* ACADEMIC YEAR */}

                    <div>

                        <label
                            style={labelStyle}
                        >
                            Academic Year
                        </label>


                        <select
                            value={academicYear}
                            onChange={(e) => {

                                const value =
                                    e.target.value;

                                setAcademicYear(
                                    value
                                );


                                if (
                                    value === "4"
                                    &&
                                    semester === "2"
                                ) {

                                    setSemester("1");
                                }


                                setShowForm(false);
                            }}
                            style={inputStyle}
                        >

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


                    {/* SEMESTER */}

                    <div>

                        <label
                            style={labelStyle}
                        >
                            Semester
                        </label>


                        <select
                            value={semester}
                            onChange={(e) =>
                                setSemester(
                                    e.target.value
                                )
                            }
                            style={inputStyle}
                        >

                            <option value="1">
                                Semester 1
                            </option>


                            {academicYear !== "4" && (

                                <option value="2">
                                    Semester 2
                                </option>

                            )}

                        </select>

                    </div>


                    {/* ADD BUTTON */}

                    <button
                        type="button"
                        onClick={openAddForm}
                        style={{
                            padding: "11px 18px",
                            border: "none",
                            borderRadius: "7px",
                            cursor: "pointer",
                            backgroundColor: "#2563eb",
                            color: "#ffffff",
                            fontWeight: "600"
                        }}
                    >
                        + Add Timetable Entry
                    </button>

                </div>

            </div>


            {/* =================================================
                TIMETABLE
            ================================================= */}

            <div
                style={{
                    backgroundColor: "#ffffff",
                    padding: "25px",
                    borderRadius: "12px",
                    boxShadow:
                        "0 2px 8px rgba(0,0,0,0.08)"
                }}
            >

                <h2
                    style={{
                        color: "#111827"
                    }}
                >
                    Year {academicYear} - Semester {semester}
                </h2>


                {loading ? (

                    <p
                        style={{
                            color: "#111827"
                        }}
                    >
                        Loading timetable...
                    </p>

                ) : sortedTimetable.length === 0 ? (

                    <div
                        style={{
                            padding: "35px",
                            textAlign: "center",
                            color: "#111827",
                            border:
                                "1px solid #e5e7eb",
                            borderRadius: "8px"
                        }}
                    >
                        No timetable entries found.
                    </div>

                ) : (

                    <div
                        style={{
                            overflowX: "auto"
                        }}
                    >

                        <table
                            style={{
                                width: "100%",
                                borderCollapse:
                                    "collapse"
                            }}
                        >

                            <thead>

                                <tr
                                    style={{
                                        backgroundColor:
                                            "#f3f4f6"
                                    }}
                                >

                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        #
                                    </th>


                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        Day
                                    </th>


                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        Time
                                    </th>


                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        Subject
                                    </th>


                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        Lecturer
                                    </th>


                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        Specialization
                                    </th>


                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        Room
                                    </th>


                                    <th
                                        style={
                                            tableHeaderStyle
                                        }
                                    >
                                        Actions
                                    </th>

                                </tr>

                            </thead>


                            <tbody>

                                {sortedTimetable.map(
                                    (entry, index) => (

                                        <tr
                                            key={
                                                entry.timetableId
                                            }
                                            style={{
                                                borderBottom:
                                                    "1px solid #e5e7eb"
                                            }}
                                        >

                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >
                                                {index + 1}
                                            </td>


                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >

                                                <strong>
                                                    {
                                                        getDay(
                                                            entry
                                                        )
                                                    }
                                                </strong>

                                            </td>


                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >

                                                {
                                                    getStartTime(
                                                        entry
                                                    )
                                                }

                                                {" - "}

                                                {
                                                    getEndTime(
                                                        entry
                                                    )
                                                }

                                            </td>


                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >

                                                <strong>
                                                    {
                                                        getSubjectCode(
                                                            entry
                                                        )
                                                    }
                                                </strong>

                                                <br />

                                                <small
                                                    style={{
                                                        color: "#111827"
                                                    }}
                                                >
                                                    {
                                                        getSubjectName(
                                                            entry
                                                        )
                                                    }
                                                </small>

                                            </td>


                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >

                                                {
                                                    getLecturerName(
                                                        entry
                                                    )
                                                }

                                            </td>


                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >

                                                {
                                                    getSpecializationName(
                                                        entry
                                                    )
                                                }

                                            </td>


                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >

                                                {
                                                    getRoom(
                                                        entry
                                                    )
                                                }

                                            </td>


                                            <td
                                                style={
                                                    tableCellStyle
                                                }
                                            >

                                                <div
                                                    style={{
                                                        display:
                                                            "flex",
                                                        gap:
                                                            "8px"
                                                    }}
                                                >

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            openEditForm(
                                                                entry
                                                            )
                                                        }
                                                        style={{
                                                            padding:
                                                                "7px 12px",
                                                            border:
                                                                "none",
                                                            borderRadius:
                                                                "6px",
                                                            cursor:
                                                                "pointer",
                                                            backgroundColor:
                                                                "#f59e0b",
                                                            color:
                                                                "#ffffff"
                                                        }}
                                                    >
                                                        Edit
                                                    </button>


                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            deleteEntry(
                                                                entry.timetableId
                                                            )
                                                        }
                                                        style={{
                                                            padding:
                                                                "7px 12px",
                                                            border:
                                                                "none",
                                                            borderRadius:
                                                                "6px",
                                                            cursor:
                                                                "pointer",
                                                            backgroundColor:
                                                                "#dc2626",
                                                            color:
                                                                "#ffffff"
                                                        }}
                                                    >
                                                        Delete
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

            </div>


            {/* =================================================
                FORM
            ================================================= */}

            {showForm && (

                <div
                    style={{
                        backgroundColor: "#ffffff",
                        padding: "25px",
                        borderRadius: "12px",
                        marginTop: "25px",
                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.08)"
                    }}
                >

                    <div
                        style={{
                            display: "flex",
                            justifyContent:
                                "space-between",
                            alignItems: "center",
                            marginBottom: "20px"
                        }}
                    >

                        <h2
                            style={{
                                margin: 0,
                                color: "#111827"
                            }}
                        >
                            {editingId
                                ? "Edit Timetable Entry"
                                : "Add Timetable Entry"}
                        </h2>


                        <button
                            type="button"
                            onClick={closeForm}
                            style={{
                                border: "none",
                                background:
                                    "transparent",
                                fontSize: "24px",
                                cursor: "pointer",
                                color: "#111827"
                            }}
                        >
                            ×
                        </button>

                    </div>
                    


                    <form
                        onSubmit={submitForm}
                    >

                        {/* =================================================
                            LECTURER ALLOCATION
                        ================================================= */}

                        <div
                            style={
                                formGroupStyle
                            }
                        >

                            <label
                                style={
                                    labelStyle
                                }
                            >
                                Subject / Lecturer
                            </label>

                            {editingId ? (

                                <div
                                    style={readOnlyInputStyle}
                                >
                                    {getAllocationLabel(
                                        allocations.find(
                                            (allocation) =>
                                                String(
                                                    allocation?.allocationId
                                                ) ===
                                                String(
                                                    allocationId
                                                )
                                        )
                                    )}
                                </div>

                            ) : (

                                <>
                                    <select
                                        value={allocationId}
                                        onChange={(e) => {
                                            const value =
                                                e.target.value;

                                            setAllocationId(value);

                                            const selectedAllocation =
                                                allocations.find(
                                                    (allocation) =>
                                                        String(
                                                            allocation?.allocationId
                                                        ) ===
                                                        String(value)
                                                );

                                            const code =
                                                getAllocationSpecializationCode(
                                                    selectedAllocation
                                                );

                                            if (code) {
                                                const matchingSpecialization =
                                                    specializations.find(
                                                        (specialization) => {
                                                            const specializationCode =
                                                                specialization?.specializationCode ||
                                                                specialization?.specialization_code ||
                                                                "";

                                                            return String(
                                                                specializationCode
                                                            ).toUpperCase() === code;
                                                        }
                                                    );

                                                setSpecializationId(
                                                    matchingSpecialization
                                                        ? String(
                                                            matchingSpecialization.specializationId
                                                        )
                                                        : ""
                                                );
                                            } else {
                                                setSpecializationId(
                                                    ""
                                                );
                                            }
                                        }}
                                        style={
                                            inputStyleFull
                                        }
                                    >

                                        <option value="">
                                            Select Subject / Lecturer
                                        </option>

                                        {filteredAllocations.map(
                                            (allocation) => (

                                                <option
                                                    key={
                                                        allocation.allocationId
                                                    }
                                                    value={
                                                        allocation.allocationId
                                                    }
                                                >
                                                    {getAllocationLabel(
                                                        allocation
                                                    )}
                                                </option>

                                            )
                                        )}

                                    </select>

                                    {filteredAllocations.length === 0 && (
                                        <small
                                            style={{
                                                display: "block",
                                                marginTop: "6px",
                                                color: "#111827"
                                            }}
                                        >
                                            No lecturer allocations are available for Year {academicYear}, Semester {semester}.
                                        </small>
                                    )}
                                </>

                            )}

                            {editingId && (
                                <small
                                    style={readOnlyHelpStyle}
                                >
                                    Assigned by HOD and cannot be changed.
                                </small>
                            )}

                        </div>


                        {/* =================================================
                            CLASSROOM
                        ================================================= */}

                        <div
                            style={
                                formGroupStyle
                            }
                        >

                            <label
                                style={
                                    labelStyle
                                }
                            >
                                Classroom
                            </label>


                            <select
                                value={classroomId}
                                onChange={(e) =>
                                    setClassroomId(
                                        e.target.value
                                    )
                                }
                                style={
                                    inputStyleFull
                                }
                            >

                                <option value="">
                                    Select Classroom
                                </option>


                                {classrooms.map(
                                    (classroom) => (

                                        <option
                                            key={
                                                classroom.classroomId
                                            }
                                            value={
                                                classroom.classroomId
                                            }
                                        >

                                            {
                                                classroom.roomCode
                                            }

                                            {" - "}

                                            {
                                                classroom.roomName
                                            }

                                        </option>

                                    )
                                )}

                            </select>

                        </div>


                        {/* =================================================
                            TIME SLOT
                        ================================================= */}

                        <div
                            style={
                                formGroupStyle
                            }
                        >

                            <label
                                style={
                                    labelStyle
                                }
                            >
                                Day / Time
                            </label>


                            <select
                                value={timeSlotId}
                                onChange={(e) =>
                                    setTimeSlotId(
                                        e.target.value
                                    )
                                }
                                style={
                                    inputStyleFull
                                }
                            >

                                <option value="">
                                    Select Day / Time
                                </option>


                                {sortedTimeSlots.map(
                                    (timeSlot) => (

                                        <option
                                            key={
                                                timeSlot.timeSlotId
                                            }
                                            value={
                                                timeSlot.timeSlotId
                                            }
                                        >

                                            {
                                                getTimeSlotLabel(
                                                    timeSlot
                                                )
                                            }

                                        </option>

                                    )
                                )}

                            </select>

                        </div>


                        {/* =================================================
                            SPECIALIZATION
                        ================================================= */}

                        <div
                            style={
                                formGroupStyle
                            }
                        >

                            <label
                                style={
                                    labelStyle
                                }
                            >
                                Specialization
                            </label>

                            {editingId ? (

                                <div
                                    style={readOnlyInputStyle}
                                >
                                    {getSpecializationName(
                                        timetable.find(
                                            (entry) =>
                                                String(
                                                    entry?.timetableId
                                                ) ===
                                                String(
                                                    editingId
                                                )
                                        )
                                    )}
                                </div>

                            ) : (

                                <select
                                    value={specializationId}
                                    onChange={(e) =>
                                        setSpecializationId(
                                            e.target.value
                                        )
                                    }
                                    style={
                                        inputStyleFull
                                    }
                                >

                                    <option value="">
                                        Select Specialization
                                    </option>

                                    {getAvailableSpecializations().map(
                                        (specialization) => (

                                            <option
                                                key={
                                                    specialization.specializationId
                                                }
                                                value={
                                                    specialization.specializationId
                                                }
                                            >
                                                {specialization.specializationCode ||
                                                    specialization.specializationName ||
                                                    specialization.name}

                                                {specialization.specializationCode &&
                                                specialization.specializationName
                                                    ? ` - ${specialization.specializationName}`
                                                    : ""}
                                            </option>

                                        )
                                    )}

                                </select>

                            )}

                            {editingId && (
                                <small
                                    style={readOnlyHelpStyle}
                                >
                                    Specialization is permanent for this timetable entry.
                                </small>
                            )}

                        </div>


                        {/* =================================================
                            BUTTONS
                        ================================================= */}

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
                                style={{
                                    padding:
                                        "11px 20px",
                                    border:
                                        "none",
                                    borderRadius:
                                        "7px",
                                    cursor:
                                        formLoading
                                            ? "not-allowed"
                                            : "pointer",
                                    backgroundColor:
                                        "#2563eb",
                                    color:
                                        "#ffffff",
                                    fontWeight:
                                        "600"
                                }}
                            >

                                {formLoading
                                    ? "Saving..."
                                    : editingId
                                        ? "Update Timetable"
                                        : "Save Timetable"}

                            </button>


                            <button
                                type="button"
                                onClick={
                                    closeForm
                                }
                                style={{
                                    padding:
                                        "11px 20px",
                                    border:
                                        "1px solid #d1d5db",
                                    borderRadius:
                                        "7px",
                                    cursor:
                                        "pointer",
                                    backgroundColor:
                                        "#ffffff",
                                    color:
                                        "#111827"
                                }}
                            >
                                Cancel
                            </button>

                        </div>

                    </form>

                </div>

            )}
            {/* =================================================
            CONFIRMATION DIALOG
        ================================================= */}

        <ConfirmDialog
            open={confirmDialog.open}
            message={confirmDialog.message}
            onConfirm={handleConfirmAction}
            onCancel={closeConfirmDialog}
        />

        {/* =================================================
            SUCCESS / ERROR DIALOG
        ================================================= */}

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


// =========================================================
// STYLES
// =========================================================

const tableHeaderStyle = {

    padding: "13px 12px",

    textAlign: "left",

    borderBottom:
        "2px solid #d1d5db",

    fontSize: "14px",

    fontWeight: "600",

    color: "#111827"
};


const tableCellStyle = {

    padding: "13px 12px",

    verticalAlign: "middle",

    fontSize: "14px",

    color: "#111827"
};


const compactHeaderStyle = {
    border: "1px solid #ccc",
    padding: "12px",
    backgroundColor: "#f3f4f6",
    color: "#111827",
    fontWeight: "700"
};

const compactTimeStyle = {
    border: "1px solid #ccc",
    padding: "12px",
    fontWeight: "700",
    whiteSpace: "nowrap",
    color: "#111827"
};

const compactCellStyle = {
    border: "1px solid #ccc",
    padding: "10px",
    minWidth: "160px",
    height: "70px",
    verticalAlign: "middle",
    color: "#111827"
};


const formGroupStyle = {

    marginBottom: "18px"
};


const labelStyle = {

    display: "block",

    marginBottom: "7px",

    fontWeight: "600",

    fontSize: "14px",

    color: "#111827"
};


const inputStyle = {

    minWidth: "200px",

    boxSizing: "border-box",

    padding: "11px 12px",

    border:
        "1px solid #d1d5db",

    borderRadius: "7px",

    fontSize: "14px",

    backgroundColor: "#ffffff",

    color: "#111827"
};


const readOnlyInputStyle = {

    width: "100%",

    boxSizing: "border-box",

    padding: "11px 12px",

    border:
        "1px solid #d1d5db",

    borderRadius: "7px",

    fontSize: "14px",

    backgroundColor: "#f3f4f6",

    color: "#111827",

    fontWeight: "600",

    minHeight: "42px",

    display: "flex",

    alignItems: "center"
};


const readOnlyHelpStyle = {

    display: "block",

    marginTop: "6px",

    color: "#111827",

    fontSize: "12px"
};


const inputStyleFull = {

    width: "100%",

    boxSizing: "border-box",

    padding: "11px 12px",

    border:
        "1px solid #d1d5db",

    borderRadius: "7px",

    fontSize: "14px",

    backgroundColor: "#ffffff",

    color: "#111827"
};


export default AdminTimetableManagement;