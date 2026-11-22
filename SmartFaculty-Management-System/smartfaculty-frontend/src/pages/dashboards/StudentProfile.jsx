import { useEffect, useState } from "react";
import api from "../../services/api";

function StudentProfile() {

    const [student, setStudent] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const username = localStorage.getItem("username");

    useEffect(() => {

        const loadProfile = async () => {

            try {

                const response = await api.get("/api/students");

                const students = response.data;

                // Find the logged-in student using registration number
                const currentStudent = students.find(
                    (item) =>
                        item.registrationNumber === username
                );

                if (currentStudent) {
                    setStudent(currentStudent);
                } else {
                    setError("Student profile not found.");
                }

            } catch (error) {

                console.error("Profile error:", error);
                setError("Unable to load your profile.");

            } finally {

                setLoading(false);

            }
        };

        loadProfile();

    }, [username]);

    if (loading) {
        return <p>Loading profile...</p>;
    }

    if (error) {
        return <p style={{ color: "red" }}>{error}</p>;
    }

    return (
        <div>

            <h2>My Profile</h2>

            <div>

                <p>
                    <strong>Registration Number:</strong>{" "}
                    {student.registrationNumber}
                </p>

                <p>
                    <strong>Email:</strong>{" "}
                    {student.email}
                </p>

                <p>
                    <strong>Semester:</strong>{" "}
                    {student.semester}
                </p>

                <p>
                    <strong>Major Specialization:</strong>{" "}
                    {student.majorSpecialization
                        ? "Assigned"
                        : "Not Assigned"}
                </p>

                <p>
                    <strong>Minor Specialization:</strong>{" "}
                    {student.minorSpecialization
                        ? "Assigned"
                        : "Not Assigned"}
                </p>

            </div>

        </div>
    );
}

export default StudentProfile;