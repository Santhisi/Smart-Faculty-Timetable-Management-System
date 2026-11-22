import { useEffect, useState } from "react";
import axios from "axios";

function StudentRegisteredSubjects() {

    const [student, setStudent] = useState(null);
    const [registrations, setRegistrations] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    // =====================================================
    // LOAD REGISTERED SUBJECTS
    // =====================================================

    useEffect(() => {

        loadRegisteredSubjects();

    }, []);


    const loadRegisteredSubjects = async () => {

        try {

            setLoading(true);
            setError("");


            const token =
                localStorage.getItem("token");

            const username =
                localStorage.getItem("username");


            if (!token || !username) {

                setError(
                    "You are not logged in."
                );

                setLoading(false);

                return;
            }


            const config = {

                headers: {

                    Authorization:
                        `Bearer ${token}`
                }
            };


            // =================================================
            // GET ALL STUDENTS
            // =================================================

            const studentResponse =
                await axios.get(
                    "http://localhost:8080/api/students",
                    config
                );


            const students =
                studentResponse.data || [];


            // =================================================
            // FIND CURRENT STUDENT
            // =================================================

            const currentStudent =
                students.find(
                    (item) =>
                        item.registrationNumber ===
                            username ||
                        item.user?.username ===
                            username ||
                        item.username ===
                            username ||
                        item.email ===
                            username
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


            // =================================================
            // GET THIS STUDENT'S REGISTRATIONS
            // =================================================

            const registrationResponse =
                await axios.get(

                    `http://localhost:8080/api/student-subject-registrations/student/${studentId}`,

                    config
                );


            console.log(
                "My Registered Subjects:",
                registrationResponse.data
            );


            setRegistrations(
                Array.isArray(
                    registrationResponse.data
                )
                    ? registrationResponse.data
                    : []
            );


        } catch (err) {

            console.error(
                "Error loading registered subjects:",
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
                    err.response?.data?.message ||
                    "Unable to load registered subjects."
                );
            }

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // LOADING
    // =====================================================

    if (loading) {

        return (

            <div>

                <h3
                    style={{
                        marginTop: 0,
                        color: "#23415f"
                    }}
                >
                    My Subjects
                </h3>

                <p
                    style={{
                        color: "#687b8d"
                    }}
                >
                    Loading your registered subjects...
                </p>

            </div>
        );
    }


    // =====================================================
    // ERROR
    // =====================================================

    if (error) {

        return (

            <div>

                <h3
                    style={{
                        marginTop: 0,
                        color: "#23415f"
                    }}
                >
                    My Subjects
                </h3>

                <div
                    style={{
                        backgroundColor: "#fdecec",
                        color: "#c0392b",
                        border:
                            "1px solid #f3c4c4",
                        borderRadius: "7px",
                        padding: "12px 15px"
                    }}
                >
                    {error}
                </div>

            </div>
        );
    }


    // =====================================================
    // MAIN
    // =====================================================

    return (

        <div>

            {/* =================================================
                HEADER
            ================================================= */}

            <div
                style={{
                    marginBottom: "20px"
                }}
            >

                <h3
                    style={{
                        margin:
                            "0 0 6px",
                        color:
                            "#23415f",
                        fontSize:
                            "20px"
                    }}
                >
                    My Subjects
                </h3>


               

            </div>


            

            {/* =================================================
                NO SUBJECTS
            ================================================= */}

            {registrations.length === 0 ? (

                <div
                    style={{
                        padding: "25px",
                        textAlign: "center",
                        backgroundColor:
                            "#f8fafb",
                        border:
                            "1px solid #d5e0e8",
                        borderRadius:
                            "8px"
                    }}
                >

                    <p
                        style={{
                            margin: 0,
                            color:
                                "#687b8d"
                        }}
                    >
                        No registered subjects
                        found.
                    </p>

                </div>

            ) : (

                /* =================================================
                   SUBJECT TABLE
                ================================================= */

                <div
                    style={{
                        overflowX:
                            "auto"
                    }}
                >

                    <table
                        style={{
                            width:
                                "100%",
                            borderCollapse:
                                "collapse"
                        }}
                    >

                        <thead>

                            <tr
                                style={{
                                    backgroundColor:
                                        "#eef3f7"
                                }}
                            >

                                <th
                                    style={headerStyle}
                                >
                                    #
                                </th>

                                <th
                                    style={headerStyle}
                                >
                                    Subject Code
                                </th>

                                <th
                                    style={headerStyle}
                                >
                                    Subject Name
                                </th>

                                <th
                                    style={headerStyle}
                                >
                                    Academic Year
                                </th>

                                <th
                                    style={headerStyle}
                                >
                                    Semester
                                </th>

                            </tr>

                        </thead>


                        <tbody>

                            {registrations.map(
                                (
                                    registration,
                                    index
                                ) => (

                                    <tr
                                        key={
                                            registration.registrationId ||
                                            index
                                        }
                                    >

                                        <td
                                            style={
                                                cellStyle
                                            }
                                        >
                                            {index + 1}
                                        </td>


                                        <td
                                            style={{
                                                ...cellStyle,
                                                fontWeight:
                                                    "700"
                                            }}
                                        >
                                            {
                                                registration
                                                    .subject
                                                    ?.subjectCode ||
                                                "-"
                                            }
                                        </td>


                                        <td
                                            style={
                                                cellStyle
                                            }
                                        >
                                            {
                                                registration
                                                    .subject
                                                    ?.subjectName ||
                                                "-"
                                            }
                                        </td>


                                        <td
                                            style={
                                                cellStyle
                                            }
                                        >
                                            {
                                                formatYear(
                                                    registration
                                                        .academicYear
                                            )
                                            }
                                        </td>


                                        <td
                                            style={
                                                cellStyle
                                            }
                                        >
                                            {
                                                formatSemester(
                                                    registration
                                                        .semester
                                                )
                                            }
                                        </td>

                                    </tr>

                                )
                            )}

                        </tbody>

                    </table>

                </div>

            )}

        </div>
    );
}


// =========================================================
// FORMAT YEAR
// =========================================================

const formatYear = (year) => {

    if (
        year === null ||
        year === undefined ||
        year === ""
    ) {
        return "-";
    }

    if (typeof year === "number") {

        return `Year ${year}`;
    }

    const value =
        year.toString();

    if (
        value.toUpperCase()
            .startsWith("YEAR_")
    ) {

        return value.replace(
            /YEAR_/i,
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


// =========================================================
// FORMAT SEMESTER
// =========================================================

const formatSemester = (semester) => {

    if (
        semester === null ||
        semester === undefined ||
        semester === ""
    ) {
        return "-";
    }

    if (typeof semester === "number") {

        return `Semester ${semester}`;
    }

    const value =
        semester.toString();

    if (
        value.toUpperCase()
            .startsWith("SEMESTER_")
    ) {

        return value.replace(
            /SEMESTER_/i,
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


// =========================================================
// STYLES
// =========================================================

const headerStyle = {

    padding:
        "12px",

    border:
        "1px solid #d5e0e8",

    textAlign:
        "center",

    color:
        "#23415f",

    fontWeight:
        "700",

    fontSize:
        "13px",

    whiteSpace:
        "nowrap"
};


const cellStyle = {

    padding:
        "12px",

    border:
        "1px solid #d5e0e8",

    color:
        "#23415f",

    fontSize:
        "14px",

    textAlign:
        "center",

    verticalAlign:
        "middle"
};


export default StudentRegisteredSubjects;

