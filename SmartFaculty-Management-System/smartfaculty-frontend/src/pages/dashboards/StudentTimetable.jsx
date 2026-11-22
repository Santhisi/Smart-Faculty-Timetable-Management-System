import { useEffect, useState } from "react";
import axios from "axios";

function StudentTimetable() {
    const [timetables, setTimetables] = useState([]);
    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const days = [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY",
    ];

    useEffect(() => {
        loadStudentTimetable();
    }, []);

    const loadStudentTimetable = async () => {
        try {
            setLoading(true);
            setError("");

            const token = localStorage.getItem("token");
            const username = localStorage.getItem("username");

            if (!token || !username) {
                setError("You are not logged in.");
                setLoading(false);
                return;
            }

            // Get all students
            const studentResponse = await axios.get(
                "http://localhost:8080/api/students",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const students = studentResponse.data;

            // Find the logged-in student
            const currentStudent = students.find(
                (student) =>
                    student.registrationNumber === username ||
                    student.user?.username === username
            );

            if (!currentStudent) {
                setError(
                    "Student information could not be found."
                );
                setLoading(false);
                return;
            }

            setStudent(currentStudent);

            console.log(
                "Current Student:",
                currentStudent
            );

            // Get ONLY this student's timetable
            const timetableResponse = await axios.get(
                `http://localhost:8080/api/timetables/student/${currentStudent.studentId}`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            console.log(
                "My Timetable:",
                timetableResponse.data
            );

            setTimetables(timetableResponse.data);
            setLoading(false);

        } catch (error) {
            console.error(
                "Error loading student timetable:",
                error
            );

            if (error.response?.status === 403) {
                setError(
                    "Access denied. Please log in again."
                );
            } else if (error.response?.status === 404) {
                setError(
                    "Student timetable endpoint was not found."
                );
            } else {
                setError(
                    "Unable to load your timetable."
                );
            }

            setLoading(false);
        }
    };

    /*
     * Convert:
     * YEAR_1 -> Year 1
     */
    const formatYear = (year) => {
        if (!year) {
            return "-";
        }

        if (typeof year === "number") {
            return `Year ${year}`;
        }

        return year
            .toString()
            .replace("YEAR_", "Year ")
            .replace("_", " ");
    };

    /*
     * Convert:
     * SEMESTER_1 -> Semester 1
     */
    const formatSemester = (semester) => {
        if (!semester) {
            return "-";
        }

        if (typeof semester === "number") {
            return `Semester ${semester}`;
        }

        return semester
            .toString()
            .replace("SEMESTER_", "Semester ")
            .replace("_", " ");
    };

    /*
     * Get lecturer initials only.
     *
     * Examples:
     *
     * Nimal Silva -> NS
     * Nevil Rathnayake -> NR
     *
     * If the backend already contains
     * initials or shortName, use that.
     */
    const getLecturerInitials = (lecturer) => {
        if (!lecturer) {
            return "-";
        }

        if (lecturer.initials) {
            return lecturer.initials;
        }

        if (lecturer.shortName) {
            return lecturer.shortName;
        }

        const name =
            lecturer.lecturerName ||
            lecturer.name ||
            "";

        if (!name.trim()) {
            return "-";
        }

        const words = name
            .trim()
            .split(/\s+/)
            .filter(Boolean);

        if (words.length === 1) {
            return words[0]
                .substring(0, 2)
                .toUpperCase();
        }

        return (
            words[0].charAt(0) +
            words[words.length - 1].charAt(0)
        ).toUpperCase();
    };

    /*
     * Format time:
     *
     * 08:00:00 -> 08:00
     */
    const formatTime = (time) => {
        if (!time) {
            return "-";
        }

        return time.substring(0, 5);
    };

    /*
     * Create unique time slots
     */
    const timeSlots = [];

    timetables.forEach((timetable) => {
        const start = timetable.timeSlot?.startTime;
        const end = timetable.timeSlot?.endTime;

        if (start && end) {
            const existing = timeSlots.find(
                (slot) =>
                    slot.start === start &&
                    slot.end === end
            );

            if (!existing) {
                timeSlots.push({
                    start,
                    end,
                });
            }
        }
    });

    /*
     * Sort time slots by starting time
     */
    timeSlots.sort((a, b) =>
        a.start.localeCompare(b.start)
    );

    /*
     * Find timetable entry for a specific
     * day and time.
     */
    const getTimetableEntry = (
        day,
        start,
        end
    ) => {
        return timetables.find(
            (timetable) =>
                timetable.timeSlot?.day === day &&
                timetable.timeSlot?.startTime === start &&
                timetable.timeSlot?.endTime === end
        );
    };

    /*
     * Loading
     */
    if (loading) {
        return (
            <div
                style={{
                    padding: "30px",
                    textAlign: "center",
                    color: "#23415f",
                }}
            >
                <h2>My Timetable</h2>

                <p>
                    Loading your timetable...
                </p>
            </div>
        );
    }

    /*
     * Error
     */
    if (error) {
        return (
            <div
                style={{
                    padding: "30px",
                    textAlign: "center",
                }}
            >
                <h2
                    style={{
                        color: "#23415f",
                    }}
                >
                    My Timetable
                </h2>

                <p
                    style={{
                        color: "#c0392b",
                    }}
                >
                    {error}
                </p>
            </div>
        );
    }

    return (
        <div
            style={{
                width: "100%",
                boxSizing: "border-box",
            }}
        >
            {/* ================= HEADER ================= */}

            <div
                style={{
                    marginBottom: "25px",
                }}
            >
                <h2
                   style={{
                    margin: "0 0 8px",
                    color: "#23415f",
                    fontSize: "22px"
                }}
                >
                    My Timetable
                </h2>



             
            </div>

            {/* ================= NO TIMETABLE ================= */}

            {timetables.length === 0 ? (
                <div
                    style={{
                        backgroundColor: "#f4f7fa",
                        border:
                            "1px solid #d5e0e8",
                        borderRadius: "10px",
                        padding: "45px 25px",
                        textAlign: "center",
                    }}
                >
                    <div
                        style={{
                            fontSize: "40px",
                            marginBottom: "15px",
                        }}
                    >
                        🗓️
                    </div>

                    <h3
                        style={{
                            color: "#23415f",
                            margin:
                                "0 0 10px",
                        }}
                    >
                        No Timetable Available
                    </h3>

                    <p
                        style={{
                            color: "#687b8d",
                            margin: "0",
                        }}
                    >
                        No timetable has been
                        published for your
                        current year and
                        semester yet.
                    </p>
                </div>
            ) : (
                /* ================= TIMETABLE ================= */

                <div
                    style={{
                        overflowX: "auto",
                        width: "100%",
                        border:
                            "1px solid #d5e0e8",
                        borderRadius: "10px",
                    }}
                >
                    <table
                        style={{
                            width: "100%",
                            borderCollapse:
                                "collapse",
                            minWidth: "950px",
                            textAlign: "center",
                            backgroundColor:
                                "#ffffff",
                        }}
                    >
                        <thead>
                            <tr>
                                <th
                                    style={{
                                        backgroundColor:
                                            "#23415f",
                                        color:
                                            "#ffffff",
                                        border:
                                            "1px solid #d5e0e8",
                                        padding:
                                            "14px",
                                        width:
                                            "130px",
                                    }}
                                >
                                    Time
                                </th>

                                {days.map(
                                    (day) => (
                                        <th
                                            key={day}
                                            style={{
                                                backgroundColor:
                                                    "#23415f",
                                                color:
                                                    "#ffffff",
                                                border:
                                                    "1px solid #d5e0e8",
                                                padding:
                                                    "14px",
                                                minWidth:
                                                    "160px",
                                            }}
                                        >
                                            {day}
                                        </th>
                                    )
                                )}
                            </tr>
                        </thead>

                        <tbody>
                            {timeSlots.map(
                                (slot) => (
                                    <tr
                                        key={`${slot.start}-${slot.end}`}
                                    >
                                        {/* TIME */}

                                        <td
                                            style={{
                                                border:
                                                    "1px solid #d5e0e8",
                                                padding:
                                                    "14px 10px",
                                                fontWeight:
                                                    "600",
                                                color:
                                                    "#526b82",
                                                whiteSpace:
                                                    "nowrap",
                                                backgroundColor:
                                                    "#f4f7fa",
                                            }}
                                        >
                                            {formatTime(
                                                slot.start
                                            )}
                                            {" - "}
                                            {formatTime(
                                                slot.end
                                            )}
                                        </td>

                                        {/* DAYS */}

                                        {days.map(
                                            (day) => {
                                                const entry =
                                                    getTimetableEntry(
                                                        day,
                                                        slot.start,
                                                        slot.end
                                                    );

                                                return (
                                                    <td
                                                        key={`${day}-${slot.start}`}
                                                        style={{
                                                            border:
                                                                "1px solid #d5e0e8",
                                                            padding:
                                                                "12px",
                                                            minWidth:
                                                                "160px",
                                                            height:
                                                                "100px",
                                                            verticalAlign:
                                                                "middle",
                                                            backgroundColor:
                                                                entry
                                                                    ? "#f8fbfd"
                                                                    : "#ffffff",
                                                        }}
                                                    >
                                                        {entry ? (
                                                            <div>
                                                                {/* SUBJECT CODE */}

                                                                <div
                                                                    style={{
                                                                        fontWeight:
                                                                            "700",
                                                                        fontSize:
                                                                            "17px",
                                                                        color:
                                                                            "#23415f",
                                                                        marginBottom:
                                                                            "8px",
                                                                    }}
                                                                >
                                                                    {entry
                                                                        .lecturerAllocation
                                                                        ?.subject
                                                                        ?.subjectCode ||
                                                                        "-"}
                                                                </div>

                                                                {/* CLASSROOM CODE */}

                                                                <div
                                                                    style={{
                                                                        display:
                                                                            "inline-block",
                                                                        backgroundColor:
                                                                            "#e7f0f8",
                                                                        color:
                                                                            "#23415f",
                                                                        padding:
                                                                            "4px 9px",
                                                                        borderRadius:
                                                                            "4px",
                                                                        fontSize:
                                                                            "13px",
                                                                        fontWeight:
                                                                            "600",
                                                                        marginBottom:
                                                                            "8px",
                                                                    }}
                                                                >
                                                                    {entry
                                                                        .classroom
                                                                        ?.roomCode ||
                                                                        "-"}
                                                                </div>

                                                                {/* LECTURER INITIALS ONLY */}

                                                                <div
                                                                    style={{
                                                                        fontSize:
                                                                            "13px",
                                                                        color:
                                                                            "#526b82",
                                                                        lineHeight:
                                                                            "1.4",
                                                                        fontWeight:
                                                                            "600",
                                                                    }}
                                                                >
                                                                    {getLecturerInitials(
                                                                        entry
                                                                            .lecturerAllocation
                                                                            ?.lecturer
                                                                    )}
                                                                </div>
                                                            </div>
                                                        ) : (
                                                            <span
                                                                style={{
                                                                    color:
                                                                        "#b4c0ca",
                                                                }}
                                                            >
                                                                -
                                                            </span>
                                                        )}
                                                    </td>
                                                );
                                            }
                                        )}
                                    </tr>
                                )
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {/* ================= NOTE ================= */}

            {timetables.length > 0 && (
                <p
                    style={{
                        marginTop: "15px",
                        fontSize: "12px",
                        color: "#7a8c9c",
                    }}
                >
                    * Your timetable displays
                    only the classes relevant to
                    your current academic year
                    and semester.
                </p>
            )}
        </div>
    );
}

export default StudentTimetable;