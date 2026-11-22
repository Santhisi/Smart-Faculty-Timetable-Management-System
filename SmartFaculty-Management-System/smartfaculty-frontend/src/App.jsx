import {
    BrowserRouter,
    Routes,
    Route,
    Navigate
} from "react-router-dom";

import Home from "./pages/Home";
import AboutUs from "./pages/AboutUs";
import ContactUs from "./pages/ContactUs";
import Login from "./pages/Login";

import AdminDashboard from "./pages/dashboards/AdminDashboard";
import HODDashboard from "./pages/dashboards/HODDashboard";
import LecturerDashboard from "./pages/dashboards/LecturerDashboard";
import StudentSubjectRegistration from "./pages/dashboards/StudentSubjectRegistration";
import StudentDashboard from "./pages/dashboards/StudentDashboard";

import AdminTimetableManagement from "./pages/dashboards/AdminTimetableManagement";

function App() {
    return (
        <BrowserRouter>
            <Routes>
                {/* ================= PUBLIC HOME ================= */}
                <Route
                    path="/"
                    element={<Home />}
                />

                {/* ================= ABOUT ================= */}
                <Route
                    path="/about"
                    element={<AboutUs />}
                />

                {/* ================= CONTACT ================= */}
                <Route
                    path="/contact"
                    element={<ContactUs />}
                />

                {/* ================= LOGIN ================= */}
                <Route
                    path="/login"
                    element={<Login />}
                />

                {/* ================= ADMIN ================= */}
                <Route
                    path="/admin-dashboard"
                    element={<AdminDashboard />}
                />

                <Route
                    path="/admin/timetable"
                    element={
                        <AdminTimetableManagement />
                    }
                />

                {/* ================= HOD ================= */}
                <Route
                    path="/hod-dashboard"
                    element={<HODDashboard />}
                />

                {/* ================= LECTURER ================= */}
                <Route
                    path="/lecturer-dashboard"
                    element={
                        <LecturerDashboard />
                    }
                />

                {/* ================= STUDENT ================= */}
                <Route
                    path="/student-dashboard"
                    element={<StudentDashboard />}
                />

                <Route
                    path="/student-registration"
                    element={
                        <StudentSubjectRegistration />
                    }
                />

                {/* ================= UNKNOWN URL ================= */}
                <Route
                    path="*"
                    element={
                        <Navigate
                            to="/"
                            replace
                        />
                    }
                />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
