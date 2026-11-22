import { useEffect, useState } from "react";
import api from "../../services/api";

function MyTimetable() {

    const [timetables, setTimetables] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const year = "YEAR_1";
    const semester = "SEMESTER_1";

    useEffect(() => {

        const loadTimetable = async () => {

            try {

                const response = await api.get(
                    `/api/timetables/year/${year}/semester/${semester}`
                );

                console.log(
                    "Timetable API response:",
                    response.data
                );

                setTimetables(response.data);
                setError("");

            } catch (error) {

                console.error(
                    "Timetable API error:",
                    error
                );

                setError("Unable to load timetable.");

            } finally {

                setLoading(false);

            }
        };

        loadTimetable();

    }, []);

    const formatTime = (time) => {

        if (!time) {
            return "";
        }

        return time.substring(0, 5);
    };

    const formatTimeRange = (timeSlot) => {

        if (!timeSlot) {
            return "";
        }

        return `${formatTime(timeSlot.startTime)} - ${formatTime(
            timeSlot.endTime
        )}`;

    };

    /*
     * Get unique time slots from the timetable data.
     * This means we use the actual times stored
     * in your database.
     */

    const timeSlots = [
        ...new Map(
            timetables
                .filter(item => item.timeSlot)
                .map(item => [
                    item.timeSlot.timeSlotId,
                    item.timeSlot
                ])
        ).values()
    ].sort(
        (a, b) => a.slotNumber - b.slotNumber
    );

    const days = [
        "MONDAY",
        "TUESDAY",
        "WEDNESDAY",
        "THURSDAY",
        "FRIDAY"
    ];

    if (loading) {

        return (
            <p>
                Loading timetable...
            </p>
        );

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

            <h2>
                My Timetable
            </h2>

            <p>
                Year 1 - Semester 1
            </p>

            {timetables.length === 0 ? (

                <p>
                    No timetable entries found.
                </p>

            ) : (

                <table
                    border="1"
                    cellPadding="10"
                    cellSpacing="0"
                    style={{
                        borderCollapse: "collapse",
                        width: "100%"
                    }}
                >

                    <thead>

                        <tr>

                            <th>
                                Time
                            </th>

                            {days.map(day => (

                                <th key={day}>
                                    {day}
                                </th>

                            ))}

                        </tr>

                    </thead>

                    <tbody>

                        {timeSlots.map(timeSlot => (

                            <tr key={timeSlot.timeSlotId}>

                                <td>
                                    <strong>
                                        {formatTimeRange(timeSlot)}
                                    </strong>
                                </td>

                                {days.map(day => {

                                    const entry =
                                        timetables.find(
                                            item =>
                                                item.timeSlot?.timeSlotId ===
                                                    timeSlot.timeSlotId &&
                                                item.timeSlot?.day === day
                                        );

                                    return (

                                        <td key={day}>

                                            {entry ? (

                                                <div>

                                                    <strong>
                                                        {
                                                            entry
                                                                .lecturerAllocation
                                                                ?.subject
                                                                ?.subjectCode
                                                        }
                                                    </strong>

                                                    <br />

                                                    <small>
                                                        {
                                                            entry
                                                                .lecturerAllocation
                                                                ?.subject
                                                                ?.subjectName
                                                        }
                                                    </small>

                                                    <br />

                                                    <small>
                                                        {
                                                            entry
                                                                .classroom
                                                                ?.classroomName ||
                                                            entry
                                                                .classroom
                                                                ?.roomNumber ||
                                                            ""
                                                        }
                                                    </small>

                                                </div>

                                            ) : (

                                                "-"

                                            )}

                                        </td>

                                    );

                                })}

                            </tr>

                        ))}

                    </tbody>

                </table>

            )}

        </div>
    );
}

export default MyTimetable;