import { useEffect, useState } from "react";
import api from "../../services/api";

function RegisteredSubjects() {

    const [registrations, setRegistrations] =
        useState([]);

    const [loading, setLoading] =
        useState(true);

    const [error, setError] =
        useState("");


    // =====================================================
    // LOAD ALL REGISTRATIONS
    // =====================================================

    useEffect(() => {

        loadRegistrations();

    }, []);


    const loadRegistrations = async () => {

        try {

            setLoading(true);
            setError("");


            const response =
                await api.get(
                    "/api/student-subject-registrations"
                );


            console.log(
                "All Registration API response:",
                response.data
            );


            setRegistrations(
                Array.isArray(response.data)
                    ? response.data
                    : []
            );


        } catch (error) {

            console.error(
                "Registration API error:",
                error
            );


            setError(
                "Unable to load registered subjects."
            );

        } finally {

            setLoading(false);
        }
    };


    // =====================================================
    // LOADING
    // =====================================================

    if (loading) {

        return (
            <p>
                Loading registered subjects...
            </p>
        );
    }


    // =====================================================
    // ERROR
    // =====================================================

    if (error) {

        return (
            <p
                style={{
                    color: "red"
                }}
            >
                {error}
            </p>
        );
    }


    // =====================================================
    // MAIN
    // =====================================================

    return (

        <div>

            <h2>
                Registered Subjects
            </h2>


            <p
                style={{
                    color: "#687b8d"
                }}
            >
                All student subject registrations
            </p>


            {registrations.length === 0 ? (

                <p>
                    No registered subjects found.
                </p>

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
                                        "#eef3f7"
                                }}
                            >

                                <th
                                    style={headerStyle}
                                >
                                    Registration Number
                                </th>

                                <th
                                    style={headerStyle}
                                >
                                    Student Name
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
                                            style={cellStyle}
                                        >
                                            {
                                                registration
                                                    .student
                                                    ?.registrationNumber ||
                                                "-"
                                            }
                                        </td>


                                        <td
                                            style={cellStyle}
                                        >
                                            {
                                                registration
                                                    .student
                                                    ?.studentName ||
                                                "-"
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
                                                registration
                                                    .subject
                                                    ?.subjectCode ||
                                                "-"
                                            }
                                        </td>


                                        <td
                                            style={cellStyle}
                                        >
                                            {
                                                registration
                                                    .subject
                                                    ?.subjectName ||
                                                "-"
                                            }
                                        </td>


                                        <td
                                            style={cellStyle}
                                        >
                                            {
                                                registration
                                                    .academicYear
                                            }
                                        </td>


                                        <td
                                            style={cellStyle}
                                        >
                                            {
                                                registration
                                                    .semester
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


// =====================================================
// STYLES
// =====================================================

const headerStyle = {

    padding: "12px",

    border:
        "1px solid #d5e0e8",

    textAlign: "center",

    fontWeight: "700",

    color: "#23415f"
};


const cellStyle = {

    padding: "12px",

    border:
        "1px solid #d5e0e8",

    textAlign: "center",

    color: "#23415f"
};


export default RegisteredSubjects;