import { useEffect, useState } from "react";
import axios from "axios";

function TimetableChangeRequests() {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadRequests = async () => {
        try {
            setLoading(true);
            setError("");

            const token = localStorage.getItem("token");

            const response = await axios.get(
                "http://localhost:8080/api/timetable-change-requests",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            console.log("Timetable Change Requests:", response.data);

            setRequests(response.data || []);

        } catch (error) {
            console.error(
                "Error loading timetable change requests:",
                error
            );

            setError(
                "Unable to load timetable change requests."
            );
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadRequests();
    }, []);

    const handleApprove = async (requestId) => {
        const response = window.prompt(
            "Enter response for the lecturer:",
            "Request approved."
        );

        if (response === null) {
            return;
        }

        try {
            const token = localStorage.getItem("token");

            await axios.put(
                `http://localhost:8080/api/timetable-change-requests/${requestId}/approve`,
                {},
                {
                    params: {
                        adminResponse:
                            response || "Request approved.",
                    },
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            alert("Request approved successfully.");

            loadRequests();

        } catch (error) {
            console.error(
                "Error approving request:",
                error
            );

            alert("Unable to approve request.");
        }
    };

    const handleReject = async (requestId) => {
        const response = window.prompt(
            "Enter reason for rejection:",
            "Request rejected."
        );

        if (response === null) {
            return;
        }

        try {
            const token = localStorage.getItem("token");

            await axios.put(
                `http://localhost:8080/api/timetable-change-requests/${requestId}/reject`,
                {},
                {
                    params: {
                        adminResponse:
                            response || "Request rejected.",
                    },
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            alert("Request rejected successfully.");

            loadRequests();

        } catch (error) {
            console.error(
                "Error rejecting request:",
                error
            );

            alert("Unable to reject request.");
        }
    };

    const formatDate = (date) => {
        if (!date) {
            return "-";
        }

        try {
            return new Date(date).toLocaleString();
        } catch {
            return date;
        }
    };

    if (loading) {
        return (
            <div
                style={{
                    textAlign: "center",
                    padding: "40px",
                    color: "#23415f",
                }}
            >
                Loading change requests...
            </div>
        );
    }

    if (error) {
        return (
            <div
                style={{
                    padding: "30px",
                    color: "#c0392b",
                }}
            >
                {error}
            </div>
        );
    }

    return (
        <div
            style={{
                minHeight: "100vh",
                backgroundColor: "#f4f7fa",
                fontFamily: "Arial, sans-serif",
                color: "#23415f",
            }}
        >
            {/* HEADER */}

            <header
                style={{
                    backgroundColor: "#23415f",
                    color: "#ffffff",
                    padding: "18px 40px",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                }}
            >
                <div>
                    <h1
                        style={{
                            margin: "0",
                            fontSize: "22px",
                        }}
                    >
                        Smart Faculty
                    </h1>

                    <p
                        style={{
                            margin: "4px 0 0",
                            fontSize: "12px",
                            opacity: "0.85",
                        }}
                    >
                        Timetable Management System
                    </p>
                </div>
            </header>

            {/* MAIN */}

            <main
                style={{
                    maxWidth: "1250px",
                    margin: "0 auto",
                    padding: "30px 35px 50px",
                }}
            >
                <div
                    style={{
                        backgroundColor: "#ffffff",
                        border: "1px solid #d5e0e8",
                        borderRadius: "10px",
                        padding: "25px 30px",
                        marginBottom: "25px",
                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.05)",
                    }}
                >
                    <h2
                        style={{
                            margin: "0 0 8px",
                            color: "#23415f",
                        }}
                    >
                        Timetable Change Requests
                    </h2>

                    <p
                        style={{
                            margin: "0",
                            color: "#687b8d",
                        }}
                    >
                        Review timetable change requests submitted
                        by lecturers.
                    </p>
                </div>

                {/* REQUEST TABLE */}

                <div
                    style={{
                        backgroundColor: "#ffffff",
                        border: "1px solid #d5e0e8",
                        borderRadius: "10px",
                        padding: "25px 30px",
                        boxShadow:
                            "0 2px 8px rgba(0,0,0,0.05)",
                        overflowX: "auto",
                    }}
                >
                    {requests.length === 0 ? (
                        <p
                            style={{
                                color: "#687b8d",
                            }}
                        >
                            No timetable change requests found.
                        </p>
                    ) : (
                        <table
                            style={{
                                width: "100%",
                                minWidth: "1100px",
                                borderCollapse: "collapse",
                            }}
                        >
                            <thead>
                                <tr>
                                    {[
                                        "Lecturer",
                                        "Subject",
                                        "Reason",
                                        "Requested Change",
                                        "Status",
                                        "Created",
                                        "Action",
                                    ].map((heading) => (
                                        <th
                                            key={heading}
                                            style={{
                                                padding:
                                                    "14px 12px",
                                                backgroundColor:
                                                    "#eef3f7",
                                                color: "#23415f",
                                                borderBottom:
                                                    "2px solid #d5e0e8",
                                                textAlign:
                                                    "center",
                                                whiteSpace:
                                                    "nowrap",
                                            }}
                                        >
                                            {heading}
                                        </th>
                                    ))}
                                </tr>
                            </thead>

                            <tbody>
                                {requests.map((request) => (
                                    <tr
                                        key={
                                            request.requestId
                                        }
                                    >
                                        <td
                                            style={{
                                                padding: "14px",
                                                textAlign:
                                                    "center",
                                                borderBottom:
                                                    "1px solid #e4e9ed",
                                            }}
                                        >
                                            {request.lecturer
                                                ?.lecturerName ||
                                                request.lecturer
                                                    ?.lecturer_name ||
                                                "-"}
                                        </td>

                                        <td
                                            style={{
                                                padding: "14px",
                                                textAlign:
                                                    "center",
                                                borderBottom:
                                                    "1px solid #e4e9ed",
                                                fontWeight: "600",
                                            }}
                                        >
                                            {request.timetable
                                                ?.lecturerAllocation
                                                ?.subject
                                                ?.subjectCode ||
                                                request.timetable
                                                    ?.subject
                                                    ?.subjectCode ||
                                                "-"}
                                        </td>

                                        <td
                                            style={{
                                                padding: "14px",
                                                textAlign:
                                                    "left",
                                                borderBottom:
                                                    "1px solid #e4e9ed",
                                                maxWidth: "250px",
                                            }}
                                        >
                                            {request.reason}
                                        </td>

                                        <td
                                            style={{
                                                padding: "14px",
                                                textAlign:
                                                    "left",
                                                borderBottom:
                                                    "1px solid #e4e9ed",
                                            }}
                                        >
                                            <div>
                                                <strong>
                                                    Day:
                                                </strong>{" "}
                                                {request.requestedDay ||
                                                    "-"}
                                            </div>

                                            <div>
                                                <strong>
                                                    Time:
                                                </strong>{" "}
                                                {request.requestedStartTime ||
                                                    "-"}{" "}
                                                -{" "}
                                                {request.requestedEndTime ||
                                                    "-"}
                                            </div>

                                            <div>
                                                <strong>
                                                    Classroom:
                                                </strong>{" "}
                                                {request.requestedClassroom ||
                                                    "-"}
                                            </div>
                                        </td>

                                        <td
                                            style={{
                                                padding: "14px",
                                                textAlign:
                                                    "center",
                                                borderBottom:
                                                    "1px solid #e4e9ed",
                                            }}
                                        >
                                            <span
                                                style={{
                                                    padding:
                                                        "5px 10px",
                                                    borderRadius:
                                                        "12px",
                                                    fontSize:
                                                        "12px",
                                                    fontWeight:
                                                        "600",
                                                    backgroundColor:
                                                        request.status ===
                                                        "PENDING"
                                                            ? "#fff3cd"
                                                            : request.status ===
                                                                "APPROVED"
                                                            ? "#d4edda"
                                                            : "#f8d7da",
                                                    color:
                                                        request.status ===
                                                        "PENDING"
                                                            ? "#856404"
                                                            : request.status ===
                                                                "APPROVED"
                                                            ? "#155724"
                                                            : "#721c24",
                                                }}
                                            >
                                                {
                                                    request.status
                                                }
                                            </span>
                                        </td>

                                        <td
                                            style={{
                                                padding: "14px",
                                                textAlign:
                                                    "center",
                                                borderBottom:
                                                    "1px solid #e4e9ed",
                                                whiteSpace:
                                                    "nowrap",
                                            }}
                                        >
                                            {formatDate(
                                                request.createdAt
                                            )}
                                        </td>

                                        <td
                                            style={{
                                                padding: "14px",
                                                textAlign:
                                                    "center",
                                                borderBottom:
                                                    "1px solid #e4e9ed",
                                            }}
                                        >
                                            {request.status ===
                                            "PENDING" ? (
                                                <div
                                                    style={{
                                                        display:
                                                            "flex",
                                                        gap: "8px",
                                                        justifyContent:
                                                            "center",
                                                    }}
                                                >
                                                    <button
                                                        onClick={() =>
                                                            handleApprove(
                                                                request.requestId
                                                            )
                                                        }
                                                        style={{
                                                            backgroundColor:
                                                                "#27ae60",
                                                            color:
                                                                "#ffffff",
                                                            border:
                                                                "none",
                                                            borderRadius:
                                                                "5px",
                                                            padding:
                                                                "8px 12px",
                                                            cursor:
                                                                "pointer",
                                                            fontWeight:
                                                                "600",
                                                        }}
                                                    >
                                                        Approve
                                                    </button>

                                                    <button
                                                        onClick={() =>
                                                            handleReject(
                                                                request.requestId
                                                            )
                                                        }
                                                        style={{
                                                            backgroundColor:
                                                                "#c0392b",
                                                            color:
                                                                "#ffffff",
                                                            border:
                                                                "none",
                                                            borderRadius:
                                                                "5px",
                                                            padding:
                                                                "8px 12px",
                                                            cursor:
                                                                "pointer",
                                                            fontWeight:
                                                                "600",
                                                        }}
                                                    >
                                                        Reject
                                                    </button>
                                                </div>
                                            ) : (
                                                <span
                                                    style={{
                                                        color:
                                                            "#8797a5",
                                                        fontSize:
                                                            "13px",
                                                    }}
                                                >
                                                    Processed
                                                </span>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    )}
                </div>
            </main>

            <footer
                style={{
                    textAlign: "center",
                    padding: "20px",
                    color: "#8797a5",
                    fontSize: "12px",
                }}
            >
                Smart Faculty Timetable Management System
            </footer>
        </div>
    );
}

export default TimetableChangeRequests;