import { useEffect, useState } from "react";
import api from "../../services/api";

function MySubjects() {

    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {

        const loadSubjects = async () => {

            try {

                const response = await api.get("/api/subjects");

                console.log("Subjects API response:", response.data);

                setSubjects(response.data);
                setError("");

            } catch (error) {

                console.error("Subjects API error:", error);

                setError("Unable to load subjects.");

            } finally {

                setLoading(false);

            }
        };

        loadSubjects();

    }, []);

    if (loading) {
        return <p>Loading subjects...</p>;
    }

    if (error) {
        return (
            <p style={{ color: "red" }}>
                {error}
            </p>
        );
    }

    return (
        <div>

            <h2>My Subjects</h2>

            {subjects.length === 0 ? (

                <p>No subjects found.</p>

            ) : (

                <table
                    border="1"
                    cellPadding="10"
                    cellSpacing="0"
                >

                    <thead>

                        <tr>

                            <th>Subject Code</th>

                            <th>Subject Name</th>

                            <th>Academic Year</th>

                            <th>Semester</th>

                            <th>Credits</th>

                            <th>Compulsory</th>

                        </tr>

                    </thead>

                    <tbody>

                        {subjects.map((subject, index) => (

                            <tr
                                key={
                                    subject.subjectId ||
                                    subject.subjectCode ||
                                    index
                                }
                            >

                                <td>
                                    {subject.subjectCode}
                                </td>

                                <td>
                                    {subject.subjectName}
                                </td>

                                <td>
                                    {subject.academicYear}
                                </td>

                                <td>
                                    {subject.semester}
                                </td>

                                <td>
                                    {subject.credits}
                                </td>

                                <td>
                                    {subject.compulsory
                                        ? "Yes"
                                        : "No"}
                                </td>

                            </tr>

                        ))}

                    </tbody>

                </table>

            )}

        </div>
    );
}

export default MySubjects;