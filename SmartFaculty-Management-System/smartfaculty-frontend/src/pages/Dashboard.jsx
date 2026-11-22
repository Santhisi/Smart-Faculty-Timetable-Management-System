import StudentDashboard from "./dashboards/StudentDashboard";

function Dashboard() {

    const role = localStorage.getItem("role");

    if (role === "STUDENT") {
        return <StudentDashboard />;
    }

    return (
        <div style={{ padding: "30px" }}>

            <h1>
                Smart Faculty Timetable Management System
            </h1>

            <h2>
                Dashboard
            </h2>

            <p>
                Your role is: <strong>{role}</strong>
            </p>

        </div>
    );
}

export default Dashboard;