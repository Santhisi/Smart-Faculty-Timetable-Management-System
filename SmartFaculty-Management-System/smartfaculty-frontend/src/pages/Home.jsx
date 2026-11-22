import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

import universityLogo from "../assets/university-logo.png";
import facultyPhoto from "../assets/faculty-photo.jpg";

function Home() {
    const navigate = useNavigate();

    const [academicYear, setAcademicYear] = useState("all");
    const [semester, setSemester] = useState("1");

    const [timetables, setTimetables] = useState([]);
    const [loadingTimetable, setLoadingTimetable] = useState(false);
    const [timetableError, setTimetableError] = useState("");

    const days = [
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday"
    ];

    const yearOptions = [
        { value: "all", label: "All Years" },
        { value: "1", label: "Year 1" },
        { value: "2", label: "Year 2" },
        { value: "3", label: "Year 3" },
        { value: "4", label: "Year 4" }
    ];

    const semesterOptions = [
    { value: "all", label: "All Semesters" },
    { value: "1", label: "Semester 1" },
    { value: "2", label: "Semester 2" }
];

    useEffect(() => {
        loadTimetable();
    }, [academicYear, semester]);

    const loadTimetable = async () => {
    setLoadingTimetable(true);
    setTimetableError("");

    try {
        const years =
            academicYear === "all"
                ? ["1", "2", "3", "4"]
                : [academicYear];

        /*
         * IMPORTANT:
         * Use the semester selected by the user.
         *
         * "all" means load both semesters.
         * "1" means ONLY Semester 1.
         * "2" means ONLY Semester 2.
         */
        const semesters =
            semester === "all"
                ? ["1", "2"]
                : [semester];

        const requests = [];

        years.forEach((year) => {
            semesters.forEach((sem) => {

                /*
                 * Year 4 does not have Semester 2 in this
                 * timetable publishing setup.
                 */
                if (year === "4" && sem === "2") {
                    return;
                }

                requests.push({
                    year,
                    semester: sem,
                    request: axios.get(
                        `http://localhost:8080/api/timetables/year/YEAR_${year}/semester/SEMESTER_${sem}`,
                        (() => {
                            const token =
                                localStorage.getItem("token");

                            return token
                                ? {
                                      headers: {
                                          Authorization:
                                              `Bearer ${token}`
                                      }
                                  }
                                : {};
                        })()
                    )
                });
            });
        });

        const results = await Promise.allSettled(
            requests.map((item) => item.request)
        );

        const combined = [];

        results.forEach((result, index) => {

            if (
                result.status !== "fulfilled" ||
                !Array.isArray(result.value?.data)
            ) {
                return;
            }

            const requestedYear =
                requests[index].year;

            const requestedSemester =
                requests[index].semester;

            result.value.data.forEach((entry) => {

                /*
                 * =================================================
                 * STRICT SEMESTER CHECK
                 * =================================================
                 *
                 * The backend endpoint is already semester-specific.
                 * However, we also verify the semester contained
                 * inside the returned JSON.
                 *
                 * This prevents Semester 1 records from appearing
                 * when Semester 2 is selected.
                 */

                const rawSemester =
                    entry?.semester ??
                    entry?.semesterNumber ??
                    entry?.semester_number ??
                    null;

                let actualSemester = null;

                if (
                    typeof rawSemester === "object" &&
                    rawSemester !== null
                ) {
                    actualSemester =
                        rawSemester?.semesterNumber ??
                        rawSemester?.semester_number ??
                        rawSemester?.value ??
                        rawSemester?.name ??
                        null;
                } else {
                    actualSemester = rawSemester;
                }

                if (actualSemester !== null) {

                    const normalizedSemester =
                        String(actualSemester)
                            .toUpperCase()
                            .replace("SEMESTER_", "")
                            .trim();

                    if (
                        normalizedSemester !==
                        String(requestedSemester)
                    ) {
                        return;
                    }
                }

                /*
                 * =================================================
                 * STRICT YEAR CHECK
                 * =================================================
                 */

                const rawYear =
                    entry?.yearLevel ??
                    entry?.academicYear ??
                    null;

                let actualYear = null;

                if (
                    typeof rawYear === "object" &&
                    rawYear !== null
                ) {
                    actualYear =
                        rawYear?.yearNumber ??
                        rawYear?.yearLevel ??
                        rawYear?.value ??
                        rawYear?.name ??
                        null;
                } else {
                    actualYear = rawYear;
                }

                if (actualYear !== null) {

                    const normalizedYear =
                        String(actualYear)
                            .toUpperCase()
                            .replace("YEAR_", "")
                            .replace("YEAR", "")
                            .trim();

                    if (
                        normalizedYear !==
                        String(requestedYear)
                    ) {
                        return;
                    }
                }

                /*
                 * Add the record only after it passes the
                 * requested year/semester validation.
                 */
                combined.push({
                    ...entry,
                    __homeYear: requestedYear,
                    __homeSemester: requestedSemester
                });
            });
        });

        /*
         * =========================================================
         * REMOVE DUPLICATES
         * =========================================================
         */

        const unique = [];
        const seen = new Set();

        combined.forEach((entry, index) => {

            const id =
                entry?.timetableId ??
                entry?.id ??
                `${entry.__homeYear}-${entry.__homeSemester}-${entry?.timeSlot?.day}-${entry?.timeSlot?.startTime}-${entry?.timeSlot?.endTime}-${index}`;

            if (!seen.has(String(id))) {
                seen.add(String(id));
                unique.push(entry);
            }
        });

        /*
         * =========================================================
         * FINAL SAFETY FILTER
         * =========================================================
         *
         * This is important for the Home page.
         *
         * If the user selected one specific semester,
         * absolutely no other semester is allowed into state.
         */

        const finalTimetables =
            unique.filter((entry) => {

                const matchesYear =
                    academicYear === "all" ||
                    String(entry.__homeYear) ===
                        String(academicYear);

                const matchesSemester =
                    semester === "all" ||
                    String(entry.__homeSemester) ===
                        String(semester);

                return (
                    matchesYear &&
                    matchesSemester
                );
            });

        setTimetables(finalTimetables);

        if (finalTimetables.length === 0) {
            setTimetableError(
                
            );
        } else {
            setTimetableError("");
        }

    } catch (error) {

        console.error(
            "Home timetable loading error:",
            error
        );

        console.error(
            "Server response:",
            error.response?.data
        );

        setTimetables([]);

        setTimetableError(
            "Unable to load the timetable. Please try again."
        );

    } finally {
        setLoadingTimetable(false);
    }
};

    const goToLogin = () => {
        navigate("/login");
    };

    const goToAbout = () => {
        navigate("/about");
    };

    const goToContact = () => {
        navigate("/contact");
    };

    const scrollToTimetable = () => {
        document.getElementById("timetable")?.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    };

    const normalizeDay = (day) => {
        if (!day) {
            return "";
        }

        return String(day)
            .replace("DAY_", "")
            .trim()
            .toLowerCase();
    };

    const getDay = (entry) => {
        return normalizeDay(entry?.timeSlot?.day);
    };

    const getStartTime = (entry) => {
        return entry?.timeSlot?.startTime || "";
    };

    const getEndTime = (entry) => {
        return entry?.timeSlot?.endTime || "";
    };

    const formatTime = (time) => {
        if (!time) {
            return "";
        }

        return String(time).substring(0, 5);
    };

    const getSubjectCode = (entry) => {
        return (
            entry?.lecturerAllocation?.subject?.subjectCode ||
            entry?.lecturerAllocation?.subject?.subject_code ||
            entry?.subject?.subjectCode ||
            entry?.subject?.subject_code ||
            "-"
        );
    };

    const getLecturerName = (entry) => {
        return (
            entry?.lecturerAllocation?.lecturer?.lecturerName ||
            entry?.lecturerAllocation?.lecturer?.lecturer_name ||
            entry?.lecturerAllocation?.lecturer?.name ||
            entry?.lecturer?.lecturerName ||
            entry?.lecturer?.lecturer_name ||
            ""
        );
    };

    const getLecturerInitials = (entry) => {
        const name = getLecturerName(entry).trim();

        if (!name) {
            return "-";
        }

        const cleanedWords = name
            .replace(/[^A-Za-z\s.]/g, " ")
            .split(/\s+/)
            .filter(Boolean)
            .filter((word) => {
                const normalized = word
                    .replace(/\./g, "")
                    .toLowerCase();

                return ![
                    "mr",
                    "mrs",
                    "ms",
                    "miss",
                    "dr",
                    "prof",
                    "lecturer",
                    "sir"
                ].includes(normalized);
            });

        if (cleanedWords.length === 0) {
            return "-";
        }

        if (cleanedWords.length === 1) {
            return cleanedWords[0]
                .replace(/\./g, "")
                .substring(0, 2)
                .toUpperCase();
        }

        const first = cleanedWords[0]
            .replace(/\./g, "")
            .charAt(0);

        const last =
            cleanedWords[cleanedWords.length - 1]
                .replace(/\./g, "")
                .charAt(0);

        return `${first}${last}`.toUpperCase();
    };

    const getClassroomCode = (entry) => {
        return (
            entry?.classroom?.roomCode ||
            entry?.classroom?.room_code ||
            entry?.classroom?.classroomCode ||
            entry?.classroom?.classroom_code ||
            "-"
        );
    };

    const getCompactEntry = (entry) => {
        return `${getSubjectCode(entry)}-${getLecturerInitials(entry)}-${getClassroomCode(entry)}`;
    };

    const getYearNumber = (entry) => {
        const value =
            entry?.yearLevel ??
            entry?.academicYear ??
            entry?.__homeYear ??
            "";

        if (typeof value === "number") {
            return value;
        }

        const match = String(value).match(/[1-4]/);

        return match ? Number(match[0]) : "";
    };

    const timeSlots = useMemo(() => {
        const uniqueSlots = [];

        timetables.forEach((entry) => {
            const start = getStartTime(entry);
            const end = getEndTime(entry);

            if (!start || !end) {
                return;
            }

            const exists = uniqueSlots.some(
                (slot) =>
                    slot.start === start &&
                    slot.end === end
            );

            if (!exists) {
                uniqueSlots.push({
                    start,
                    end
                });
            }
        });

        return uniqueSlots.sort((a, b) =>
            String(a.start).localeCompare(
                String(b.start)
            )
        );
    }, [timetables]);

    const getEntriesForCell = (day, start, end) => {
        return timetables
            .filter(
                (entry) =>
                    getDay(entry) === day.toLowerCase() &&
                    getStartTime(entry) === start &&
                    getEndTime(entry) === end
            )
            .sort((a, b) =>
                getCompactEntry(a).localeCompare(
                    getCompactEntry(b)
                )
            );
    };
    const getEntriesForPrintCell = (entries, day, start, end) => {
    return entries
        .filter(
            (entry) =>
                getDay(entry) === day.toLowerCase() &&
                getStartTime(entry) === start &&
                getEndTime(entry) === end
        )
        .sort((a, b) =>
            getCompactEntry(a).localeCompare(
                getCompactEntry(b)
            )
        );
};

const getPrintTimeSlots = (entries) => {
    const uniqueSlots = [];

    entries.forEach((entry) => {
        const start = getStartTime(entry);
        const end = getEndTime(entry);

        if (!start || !end) {
            return;
        }

        if (
            !uniqueSlots.some(
                (slot) =>
                    slot.start === start &&
                    slot.end === end
            )
        ) {
            uniqueSlots.push({
                start,
                end
            });
        }
    });

    return uniqueSlots.sort((a, b) =>
        String(a.start).localeCompare(String(b.start))
    );
};

    const timetableTitle = () => {
    if (
        academicYear === "all" &&
        semester === "all"
    ) {
        return "All Years - All Semesters";
    }

    if (academicYear === "all") {
        return `All Years - Semester ${semester}`;
    }

    if (semester === "all") {
        return `Year ${academicYear} - All Semesters`;
    }

    return `Year ${academicYear} - Semester ${semester}`;
};

const getRomanYear = (year) => {
    const romanYears = {
        "1": "I",
        "2": "II",
        "3": "III",
        "4": "IV"
    };

    return romanYears[String(year)] || year;
};

const getPrintGroups = () => {
    const years =
        academicYear === "all"
            ? ["1", "2", "3", "4"]
            : [academicYear];

    const semesters =
        semester === "all"
            ? ["1", "2"]
            : [semester];

    const groups = [];

    semesters.forEach((sem) => {
        years.forEach((year) => {
            // Year 4 has Semester 1 only
            if (year === "4" && sem === "2") {
                return;
            }

            const entries = timetables.filter(
                (entry) =>
                    String(entry.__homeYear) === String(year) &&
                    String(entry.__homeSemester) === String(sem)
            );

            // Do not create an empty print page
            if (entries.length > 0) {
                groups.push({
                    year,
                    semester: sem,
                    entries
                });
            }
        });
    });

    return groups;
};

const toRomanYear = (year) => {
    return ["", "I", "II", "III", "IV"][Number(year)] || year;
};

const getPrintHeader = (year, sem) => {
    const semesterRoman = sem === "1" ? "I" : "II";

    return `Faculty of Social Sciences and Languages - Time Table – 2025/2026 - Year ${toRomanYear(year)} Semester ${semesterRoman}`;
};


    return (
        <div style={pageStyle}>
            <style>{responsiveStyles}</style>

            {/* ================= HEADER ================= */}
            <header style={headerStyle}>
                <div
                    style={headerInnerStyle}
                    className="smart-home-header-inner"
                >
                    <button
                        type="button"
                        onClick={() =>
                            window.scrollTo({
                                top: 0,
                                behavior: "smooth"
                            })
                        }
                        style={brandButtonStyle}
                    >
                        {/* ONE UNIVERSITY LOGO ONLY */}
                        <img
                            src={universityLogo}
                            alt="Sabaragamuwa University of Sri Lanka"
                            style={logoStyle}
                        />

                        <div style={brandTextStyle}>
                            <div
                                style={facultyNameHeaderStyle}
                            >
                                Faculty of Social Sciences
                                <br />
                                and Languages
                            </div>

                            <div
                                style={universityNameHeaderStyle}
                            >
                                Sabaragamuwa University
                                of Sri Lanka
                            </div>
                        </div>
                    </button>

                    <nav
                        style={navStyle}
                        className="smart-home-nav"
                    >
                        <button
                            type="button"
                            onClick={() =>
                                window.scrollTo({
                                    top: 0,
                                    behavior: "smooth"
                                })
                            }
                            style={navButtonStyle}
                        >
                            Home
                        </button>

                        <button
                            type="button"
                            onClick={goToAbout}
                            style={navButtonStyle}
                        >
                            About Us
                        </button>

                        <button
                            type="button"
                            onClick={goToContact}
                            style={navButtonStyle}
                        >
                            Contact Us
                        </button>

                        <button
                            type="button"
                            onClick={goToLogin}
                            style={loginButtonStyle}
                        >
                            Login
                        </button>
                    </nav>
                </div>
            </header>

            {/* ================= HERO ================= */}
            <section style={heroStyle}>
                <div
                    style={heroContentStyle}
                    className="smart-home-hero-content"
                >
                    <div
                        style={heroLeftStyle}
                        className="smart-home-hero-left"
                    >
                        <div style={welcomeLabelStyle}>
                            WELCOME TO
                        </div>

                        <h1 style={facultyHeroTitleLargeStyle}>
                            Faculty of Social Sciences
                            <br />
                            and Languages
                        </h1>

                        <div
                            style={universityHeroTitleStyle}
                        >
                            Sabaragamuwa University of Sri Lanka
                        </div>

                        <h2 style={heroSystemTitleStyle}>
                            Smart Faculty Timetable Management System
                        </h2>

                        <p style={heroDescriptionStyle}>
                            A centralized platform for
                            accessing and managing faculty
                            timetables efficiently.
                        </p>

                        <button
                            type="button"
                            onClick={scrollToTimetable}
                            style={heroButtonStyle}
                        >
                            View Faculty Timetable
                            <span style={arrowStyle}>
                                →
                            </span>
                        </button>
                    </div>

                    <div
                        style={heroImageWrapperStyle}
                        className="smart-home-hero-image"
                    >
                        <img
                            src={facultyPhoto}
                            alt="Faculty of Social Sciences and Languages"
                            style={heroImageStyle}
                        />
                    </div>
                </div>
            </section>

            {/* ================= FEATURES ================= */}
            <section style={featuresSectionStyle}>
                <div
                    style={featuresGridStyle}
                    className="smart-home-features-grid"
                >
                    <FeatureCard
                        title="Easy Access"
                        text="View faculty timetables quickly and easily."
                    />

                    <FeatureCard
                        title="Organized Schedules"
                        text="Find classes, subjects and schedules in one place."
                    />

                    <FeatureCard
                        title="Timetable Updates"
                        text="Stay informed when approved timetable changes occur."
                    />

                    <FeatureCard
                        title="For Faculty Users"
                        text="Designed for students, lecturers, HODs and administrators."
                    />
                </div>
            </section>

            {/* ================= TIMETABLE ================= */}
           
            <section
                id="timetable"
                
                style={timetableSectionStyle}
            >
                            {/* ================= PRINT ONLY TIMETABLE ================= */}

<div className="smart-home-print-only">

    {getPrintGroups().map((group) => {

        const printTimeSlots =
            getPrintTimeSlots(
                group.entries
            );

        return (
            <div
                key={`${group.year}-${group.semester}`}
                className="smart-home-print-page"
            >

                <div className="smart-home-print-header">
                    {getPrintHeader(
                        group.year,
                        group.semester
                    )}
                </div>

                <table className="smart-home-print-table">

                    <thead>
                        <tr>

                            <th>
                                Time
                            </th>

                            {days.map(
                                (day) => (
                                    <th key={day}>
                                        {day}
                                    </th>
                                )
                            )}

                        </tr>
                    </thead>

                    <tbody>

                        {printTimeSlots.map(
                            (slot) => (
                                <tr
                                    key={
                                        `${group.year}-${group.semester}-${slot.start}-${slot.end}`
                                    }
                                >

                                    <td>
                                        {formatTime(
                                            slot.start
                                        )}
                                        {" - "}
                                        {formatTime(
                                            slot.end
                                        )}
                                    </td>

                                    {days.map(
                                        (day) => {

                                            const entries =
                                                getEntriesForPrintCell(
                                                    group.entries,
                                                    day,
                                                    slot.start,
                                                    slot.end
                                                );

                                            return (
                                                <td
                                                    key={
                                                        `${day}-${slot.start}-${slot.end}`
                                                    }
                                                >

                                                    {entries.length ===
                                                    0 ? (
                                                        "-"
                                                    ) : (
                                                        entries.map(
                                                            (
                                                                entry,
                                                                index
                                                            ) => (
                                                                <div
                                                                    key={
                                                                        entry?.timetableId ||
                                                                        entry?.id ||
                                                                        index
                                                                    }
                                                                >
                                                                    {getCompactEntry(
                                                                        entry
                                                                    )}
                                                                </div>
                                                            )
                                                        )
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
        );
    })}

</div>


{/* YOUR EXISTING WEBSITE TIMETABLE STARTS HERE */}
<div
    style={sectionHeaderStyle}
    className="smart-home-timetable-web-header"
>
                    <div>
                        <div style={sectionLabelStyle}>
                            FACULTY TIMETABLE
                        </div>

                        <h2 style={sectionTitleStyle}>
                            View Timetable
                        </h2>

                        
                    </div>
                </div>

                <div
                    style={filterRowStyle}
                    className="smart-home-timetable-filter-row"
                >
                    <div style={filterGroupStyle}>
                        <label
                            style={filterLabelStyle}
                        >
                            Academic Year
                        </label>

                        <select
                            value={academicYear}
                            onChange={(event) =>
                                setAcademicYear(
                                    event.target.value
                                )
                            }
                            style={selectStyle}
                        >
                            {yearOptions.map(
                                (option) => (
                                    <option
                                        key={option.value}
                                        value={option.value}
                                    >
                                        {option.label}
                                    </option>
                                )
                            )}
                        </select>
                    </div>

                    <div style={filterGroupStyle}>
                        <label
                            style={filterLabelStyle}
                        >
                            Semester
                        </label>

                        <select
                            value={semester}
                            onChange={(event) =>
                                setSemester(
                                    event.target.value
                                )
                            }
                            style={selectStyle}
                        >
                            {semesterOptions.map((option) => (
                                <option
                                    key={option.value}
                                    value={option.value}
                                >
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    </div>

                    <button
                        type="button"
                        onClick={() => {
                            setAcademicYear("all");
                            setSemester("1");
                        }}
                        style={clearButtonStyle}
                    >
                        Clear
                    </button>

                    <button
                        type="button"
                        onClick={() => window.print()}
                        disabled={
                            loadingTimetable ||
                            timetables.length === 0
                        }
                        style={printButtonStyle}
                    >
                        Print
                    </button>
                </div>

                <div
    style={selectedScheduleStyle}
    className="smart-home-selected-schedule"
>
                    Showing:{" "}
                    <strong>
                        {timetableTitle()}
                    </strong>
                </div>

                {timetableError && (
                    <div style={errorStyle}>
                        {timetableError}
                    </div>
                )}

                {loadingTimetable ? (
                    <div style={loadingBoxStyle}>
                        Loading timetable...
                    </div>
                ) : timetables.length === 0 ? (
                    <div style={emptyBoxStyle}>
                        No timetable records found for
                        the selected filter.
                    </div>
                ) : (
                    <div style={tableScrollStyle}>
                        <table
                            style={tableStyle}
                            className="smart-home-web-table"
                        >
                            <thead>
                                <tr>
                                    <th
                                        style={
                                            headerCellStyle
                                        }
                                    >
                                        Time
                                    </th>

                                    {days.map(
                                        (day) => (
                                            <th
                                                key={day}
                                                style={
                                                    headerCellStyle
                                                }
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
                                            <td
                                                style={
                                                    timeCellStyle
                                                }
                                            >
                                                {formatTime(
                                                    slot.start
                                                )}
                                                {" - "}
                                                {formatTime(
                                                    slot.end
                                                )}
                                            </td>

                                            {days.map(
                                                (day) => {
                                                    const entries =
                                                        getEntriesForCell(
                                                            day,
                                                            slot.start,
                                                            slot.end
                                                        );
                                                        const getEntriesForPrintCell = (
    entries,
    day,
    start,
    end
) => {
    return entries
        .filter(
            (entry) =>
                getDay(entry) === day.toLowerCase() &&
                getStartTime(entry) === start &&
                getEndTime(entry) === end
        )
        .sort((a, b) =>
            getCompactEntry(a).localeCompare(
                getCompactEntry(b)
            )
        );
};

const getPrintTimeSlots = (entries) => {
    const uniqueSlots = [];

    entries.forEach((entry) => {
        const start = getStartTime(entry);
        const end = getEndTime(entry);

        if (!start || !end) {
            return;
        }

        const exists = uniqueSlots.some(
            (slot) =>
                slot.start === start &&
                slot.end === end
        );

        if (!exists) {
            uniqueSlots.push({
                start,
                end
            });
        }
    });

    return uniqueSlots.sort((a, b) =>
        String(a.start).localeCompare(
            String(b.start)
        )
    );
};

const toRomanYear = (year) => {
    const romanYears = {
        "1": "I",
        "2": "II",
        "3": "III",
        "4": "IV"
    };

    return (
        romanYears[String(year)] ||
        year
    );
};

const getPrintHeader = (year, sem) => {
    const semesterRoman =
        sem === "1"
            ? "I"
            : "II";

    return (
        `Faculty of Social Sciences and Languages - ` +
        `Time Table – 2025/2026 - ` +
        `Year ${toRomanYear(year)} ` +
        `Semester ${semesterRoman}`
    );
};

/*
 * ============================================================
 * PRINT GROUPS
 * ============================================================
 *
 * The print pages follow the current filter.
 *
 * Year 1 + Semester 1
 *     -> ONE page
 *
 * All Years + Semester 1
 *     -> Year I, II, III, IV pages
 *
 * All Years + Semester 2
 *     -> only Year I, II, III pages if they contain data
 *
 * Year 1 + All Semesters
 *     -> Year I Semester I and Semester II pages
 *
 * No empty pages are created.
 */
const getPrintGroups = () => {

    const years =
        academicYear === "all"
            ? ["1", "2", "3", "4"]
            : [academicYear];

    const semesters =
        semester === "all"
            ? ["1", "2"]
            : [semester];

    const groups = [];

    semesters.forEach((sem) => {

        years.forEach((year) => {

            /*
             * Year 4 Semester 2 is not used.
             */
            if (
                year === "4" &&
                sem === "2"
            ) {
                return;
            }

            /*
             * Only use entries that belong to this
             * exact year + semester.
             */
            const entries =
                timetables.filter(
                    (entry) =>
                        String(entry.__homeYear) ===
                            String(year) &&
                        String(entry.__homeSemester) ===
                            String(sem)
                );

            /*
             * IMPORTANT:
             * Do NOT create an empty print page.
             */
            if (entries.length === 0) {
                return;
            }

            groups.push({
                year,
                semester: sem,
                entries
            });
        });
    });

    return groups;
};

                                                    return (
                                                        <td
                                                            key={`${day}-${slot.start}-${slot.end}`}
                                                            style={
                                                                tableCellStyle
                                                            }
                                                        >
                                                            {entries.length ===
                                                            0 ? (
                                                                <span
                                                                    style={{
                                                                        color: "#94a3b8"
                                                                    }}
                                                                >
                                                                    -
                                                                </span>
                                                            ) : (
                                                                <div
                                                                    style={
                                                                        entryListStyle
                                                                    }
                                                                >
                                                                    {entries.map(
                                                                        (
                                                                            entry,
                                                                            index
                                                                        ) => (
                                                                            <div
                                                                                key={
                                                                                    entry?.timetableId ||
                                                                                    entry?.id ||
                                                                                    index
                                                                                }
                                                                                style={
                                                                                    compactEntryStyle
                                                                                }
                                                                            >
                                                                                {getCompactEntry(
                                                                                    entry
                                                                                )}
                                                                            </div>
                                                                        )
                                                                    )}
                                                                </div>
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

               
            </section>

            {/* ================= ABOUT PREVIEW ================= */}
            <section style={aboutPreviewStyle}>
                <div style={aboutPreviewInnerStyle}>
                    <div style={sectionLabelStyle}>
                        ABOUT SMART FACULTY
                    </div>

                    <h2 style={aboutPreviewTitleStyle}>
                        Smart Faculty Timetable
                        Management System
                    </h2>

                    <p style={aboutPreviewTextStyle}>
                        A web-based timetable management
                        platform created for the Faculty of
                        Social Sciences and Languages,
                        Sabaragamuwa University of Sri Lanka.
                    </p>

                    <button
                        type="button"
                        onClick={goToAbout}
                        style={secondaryButtonStyle}
                    >
                        Learn More About Us
                    </button>
                </div>
            </section>

            {/* ================= CTA ================= */}
            <section style={ctaSectionStyle}>
                <h2 style={ctaTitleStyle}>
                    Build Your Future with Smart Faculty
                </h2>

                <p style={ctaTextStyle}>
                    Better planning. Smoother learning. A
                    smarter faculty experience.
                </p>

                <button
                    type="button"
                    onClick={goToLogin}
                    style={ctaButtonStyle}
                >
                    Login to Smart Faculty
                </button>
            </section>

            {/* ================= FOOTER ================= */}
            <footer style={footerStyle}>
                <div
                    style={footerInnerStyle}
                    className="smart-home-footer-inner"
                >
                    <div>
                        <strong style={footerBrandStyle}>
                            Smart Faculty
                        </strong>

                        <div style={footerSystemStyle}>
                            Timetable Management System
                        </div>

                        <div style={footerFacultyStyle}>
                            Faculty of Social Sciences and
                            Languages
                        </div>

                        <div style={footerUniversityStyle}>
                            Sabaragamuwa University of Sri Lanka
                        </div>
                    </div>

                    <div
                        style={footerLinksStyle}
                        className="smart-home-footer-links"
                    >
                        <button
                            type="button"
                            onClick={() =>
                                window.scrollTo({
                                    top: 0,
                                    behavior: "smooth"
                                })
                            }
                            style={footerLinkStyle}
                        >
                            Home
                        </button>

                        <button
                            type="button"
                            onClick={goToAbout}
                            style={footerLinkStyle}
                        >
                            About Us
                        </button>

                        <button
                            type="button"
                            onClick={goToContact}
                            style={footerLinkStyle}
                        >
                            Contact Us
                        </button>

                        <button
                            type="button"
                            onClick={goToLogin}
                            style={footerLinkStyle}
                        >
                            Login
                        </button>
                    </div>

                    <div style={copyrightStyle}>
                        © 2026 Sabaragamuwa University of Sri
                        Lanka.
                        <br />
                        All rights reserved.
                    </div>
                </div>
            </footer>
        </div>
    );
}

function FeatureCard({ title, text }) {
    return (
        <div style={featureCardStyle}>
            <div style={featureIconStyle}>✓</div>

            <div>
                <h3 style={featureTitleStyle}>
                    {title}
                </h3>

                <p style={featureTextStyle}>
                    {text}
                </p>
            </div>
        </div>
    );
}

const pageStyle = {
    minHeight: "100vh",
    backgroundColor: "#ffffff",
    fontFamily: "Arial, sans-serif",
    color: "#23415f",
    overflowX: "hidden"
};

const headerStyle = {
    width: "100%",
    backgroundColor: "#ffffff",
    borderBottom: "1px solid #dce5ed",
    position: "sticky",
    top: 0,
    zIndex: 1000,
    boxShadow: "0 2px 8px rgba(35,65,95,0.08)"
};

const headerInnerStyle = {
    maxWidth: "1350px",
    margin: "0 auto",
    padding: "12px 35px",
    boxSizing: "border-box",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "25px"
};

const brandButtonStyle = {
    display: "flex",
    alignItems: "center",
    gap: "13px",
    border: "none",
    background: "transparent",
    padding: 0,
    cursor: "pointer",
    textAlign: "left"
};

const logoStyle = {
    width: "68px",
    height: "68px",
    objectFit: "contain",
    flexShrink: 0
};

const brandTextStyle = {
    display: "flex",
    flexDirection: "column",
    gap: "3px"
};

const facultyNameHeaderStyle = {
    color: "#23415f",
    fontSize: "22px",
    lineHeight: "1.08",
    fontWeight: "800"
};

const universityNameHeaderStyle = {
    color: "#55728d",
    fontSize: "13px",
    lineHeight: "1.3",
    fontWeight: "600"
};

const navStyle = {
    display: "flex",
    alignItems: "center",
    gap: "5px",
    flexWrap: "wrap",
    justifyContent: "flex-end"
};

const navButtonStyle = {
    border: "none",
    backgroundColor: "transparent",
    color: "#23415f",
    padding: "10px 13px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "600"
};

const loginButtonStyle = {
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#2563eb",
    color: "#ffffff",
    padding: "10px 20px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700",
    marginLeft: "5px"
};

const heroStyle = {
    backgroundColor: "#eef6ff",
    borderBottom: "1px solid #dbeafe"
};

const heroContentStyle = {
    maxWidth: "1350px",
    minHeight: "420px",
    margin: "0 auto",
    display: "flex",
    alignItems: "stretch"
};

const heroLeftStyle = {
    width: "54%",
    padding: "55px 35px 55px 60px",
    boxSizing: "border-box",
    display: "flex",
    flexDirection: "column",
    justifyContent: "center"
};

const heroImageWrapperStyle = {
    width: "46%",
    minHeight: "420px",
    backgroundColor: "#eef6ff"
};

const heroImageStyle = {
    width: "100%",
    height: "100%",
    objectFit: "cover",
    display: "block",

    WebkitMaskImage:
        "linear-gradient(to right, transparent 0%, rgba(0,0,0,0.15) 15%, rgba(0,0,0,0.55) 40%, black 75%)",

    maskImage:
        "linear-gradient(to right, transparent 0%, rgba(0,0,0,0.15) 15%, rgba(0,0,0,0.55) 40%, black 75%)"
};

const welcomeLabelStyle = {
    color: "#2563eb",
    fontSize: "14px",
    letterSpacing: "1.5px",
    fontWeight: "800",
    marginBottom: "8px"
};

const heroTitleStyle = {
    margin: 0,
    color: "#123f70",
    fontSize: "46px",
    lineHeight: "1.05",
    fontWeight: "800"
};

const heroSystemTitleStyle = {
    margin: "5px 0 18px",
    color: "#2563eb",
    fontSize: "24px",
    lineHeight: "1.2",
    fontWeight: "700"
};


const facultyHeroTitleLargeStyle = {
    margin: 0,
    color: "#123f70",
    fontSize: "42px",
    lineHeight: "1.08",
    fontWeight: "800"
};

const facultyHeroTitleStyle = {
    color: "#23415f",
    fontSize: "24px",
    lineHeight: "1.2",
    fontWeight: "800"
};

const universityHeroTitleStyle = {
    marginTop: "6px",
    color: "#55728d",
    fontSize: "16px",
    fontWeight: "600"
};

const heroDescriptionStyle = {
    maxWidth: "620px",
    margin: "17px 0 24px",
    color: "#58718a",
    fontSize: "15px",
    lineHeight: "1.6"
};

const heroButtonStyle = {
    alignSelf: "flex-start",
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#23415f",
    color: "#ffffff",
    padding: "12px 21px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700"
};

const arrowStyle = {
    marginLeft: "10px",
    fontSize: "17px"
};

const featuresSectionStyle = {
    backgroundColor: "#ffffff",
    padding: "27px 30px"
};

const featuresGridStyle = {
    maxWidth: "1250px",
    margin: "0 auto",
    display: "grid",
    gridTemplateColumns: "repeat(4, minmax(0, 1fr))",
    gap: "25px"
};

const featureCardStyle = {
    display: "flex",
    alignItems: "center",
    gap: "12px",
    padding: "10px"
};

const featureIconStyle = {
    width: "45px",
    height: "45px",
    borderRadius: "50%",
    backgroundColor: "#e7f1ff",
    color: "#2563eb",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "800",
    flexShrink: 0
};

const featureTitleStyle = {
    margin: "0 0 4px",
    color: "#23415f",
    fontSize: "15px"
};

const featureTextStyle = {
    margin: 0,
    color: "#607990",
    fontSize: "12px",
    lineHeight: "1.45"
};

const timetableSectionStyle = {
    maxWidth: "1270px",
    margin: "8px auto 35px",
    padding: "28px",
    boxSizing: "border-box",
    backgroundColor: "#f4f9ff",
    border: "1px solid #d6e6f5",
    borderRadius: "10px",
    boxShadow: "0 2px 8px rgba(35,65,95,0.05)"
};

const sectionHeaderStyle = {
    marginBottom: "20px"
};

const sectionLabelStyle = {
    color: "#2563eb",
    fontSize: "12px",
    fontWeight: "800",
    letterSpacing: "1.2px",
    marginBottom: "6px"
};

const sectionTitleStyle = {
    margin: 0,
    color: "#123f70",
    fontSize: "27px"
};

const sectionSubtitleStyle = {
    margin: "5px 0 0",
    color: "#5c748c",
    fontSize: "14px"
};

const filterRowStyle = {
    display: "flex",
    alignItems: "flex-end",
    gap: "14px",
    flexWrap: "wrap",
    marginBottom: "10px"
};

const filterGroupStyle = {
    minWidth: "220px",
    flex: "1 1 220px"
};

const filterLabelStyle = {
    display: "block",
    marginBottom: "6px",
    color: "#23415f",
    fontSize: "13px",
    fontWeight: "700"
};

const selectStyle = {
    width: "100%",
    boxSizing: "border-box",
    padding: "11px 12px",
    border: "1px solid #8fb9df",
    borderRadius: "7px",
    backgroundColor: "#ffffff",
    color: "#1f4668",
    fontSize: "14px"
};

const viewButtonStyle = {
    minHeight: "42px",
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#2563eb",
    color: "#ffffff",
    padding: "10px 20px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700",
    whiteSpace: "nowrap"
};

const clearButtonStyle = {
    minHeight: "42px",
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#2563eb",
    color: "#ffffff",
    padding: "10px 20px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700",
    whiteSpace: "nowrap"
};

const printButtonStyle = {
    minHeight: "42px",
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#23415f",
    color: "#ffffff",
    padding: "10px 20px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700",
    whiteSpace: "nowrap"
};

const selectedScheduleStyle = {
    color: "#456a89",
    fontSize: "13px",
    margin: "6px 0 14px"
};

const errorStyle = {
    backgroundColor: "#fff0f0",
    color: "#b42318",
    border: "1px solid #fecaca",
    padding: "12px",
    borderRadius: "7px",
    marginBottom: "14px",
    textAlign: "center",
    fontSize: "13px"
};

const loadingBoxStyle = {
    backgroundColor: "#ffffff",
    border: "1px solid #dbe7f1",
    borderRadius: "8px",
    padding: "35px",
    textAlign: "center",
    color: "#56728c"
};

const emptyBoxStyle = {
    backgroundColor: "#ffffff",
    border: "1px solid #dbe7f1",
    borderRadius: "8px",
    padding: "35px",
    textAlign: "center",
    color: "#64748b"
};

const tableScrollStyle = {
    overflowX: "auto",
    width: "100%"
};

const tableStyle = {
    width: "100%",
    minWidth: "950px",
    borderCollapse: "collapse",
    backgroundColor: "#ffffff",
    textAlign: "center"
};

const headerCellStyle = {
    border: "1px solid #cdddea",
    padding: "13px 10px",
    backgroundColor: "#e5f1fb",
    color: "#123f70",
    fontSize: "13px",
    fontWeight: "700"
};

const timeCellStyle = {
    border: "1px solid #cdddea",
    padding: "12px 10px",
    backgroundColor: "#f7fbff",
    color: "#23415f",
    fontSize: "13px",
    fontWeight: "700",
    whiteSpace: "nowrap"
};

const tableCellStyle = {
    border: "1px solid #cdddea",
    padding: "10px 8px",
    minWidth: "150px",
    minHeight: "72px",
    height: "72px",
    color: "#23415f",
    verticalAlign: "middle"
};

const entryListStyle = {
    display: "flex",
    flexDirection: "column",
    gap: "5px",
    alignItems: "center"
};

const compactEntryStyle = {
    fontSize: "13px",
    lineHeight: "1.35",
    fontWeight: "700",
    color: "#1f5c91",
    whiteSpace: "nowrap"
};

const formatNoteStyle = {
    marginTop: "14px",
    color: "#607990",
    fontSize: "12px",
    lineHeight: "1.5"
};

const aboutPreviewStyle = {
    backgroundColor: "#ffffff",
    padding: "55px 30px",
    borderTop: "1px solid #e1eaf2"
};

const aboutPreviewInnerStyle = {
    maxWidth: "900px",
    margin: "0 auto",
    textAlign: "center"
};

const aboutPreviewTitleStyle = {
    margin: "0 0 14px",
    color: "#123f70",
    fontSize: "29px"
};

const aboutPreviewTextStyle = {
    maxWidth: "820px",
    margin: "0 auto 20px",
    color: "#5d748a",
    fontSize: "15px",
    lineHeight: "1.65"
};

const secondaryButtonStyle = {
    border: "1px solid #2563eb",
    borderRadius: "7px",
    backgroundColor: "#ffffff",
    color: "#2563eb",
    padding: "10px 18px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700"
};

const ctaSectionStyle = {
    backgroundColor: "#eef6ff",
    textAlign: "center",
    padding: "35px 30px"
};

const ctaTitleStyle = {
    margin: 0,
    color: "#123f70",
    fontSize: "25px"
};

const ctaTextStyle = {
    margin: "7px 0 17px",
    color: "#607990",
    fontSize: "14px"
};

const ctaButtonStyle = {
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#2563eb",
    color: "#ffffff",
    padding: "11px 20px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700"
};

const footerStyle = {
    backgroundColor: "#23415f",
    color: "#ffffff",
    padding: "25px 30px"
};

const footerInnerStyle = {
    maxWidth: "1270px",
    margin: "0 auto",
    display: "grid",
    gridTemplateColumns: "1fr auto 1fr",
    alignItems: "center",
    gap: "25px"
};

const footerBrandStyle = {
    fontSize: "17px"
};

const footerSystemStyle = {
    marginTop: "4px",
    fontSize: "13px"
};

const footerFacultyStyle = {
    marginTop: "7px",
    fontSize: "12px",
    opacity: 0.9
};

const footerUniversityStyle = {
    marginTop: "2px",
    fontSize: "12px",
    opacity: 0.85
};

const footerLinksStyle = {
    display: "flex",
    alignItems: "center",
    gap: "14px"
};

const footerLinkStyle = {
    border: "none",
    background: "transparent",
    color: "#ffffff",
    cursor: "pointer",
    padding: "4px",
    fontSize: "12px"
};

const copyrightStyle = {
    textAlign: "right",
    fontSize: "11px",
    lineHeight: "1.5",
    opacity: 0.85
};

const responsiveStyles = `
    @media (max-width: 1050px) {
        .smart-home-header-inner {
            flex-direction: column;
            align-items: stretch !important;
        }

        .smart-home-nav {
            justify-content: flex-start !important;
        }

        .smart-home-hero-content {
            flex-direction: column !important;
        }

        .smart-home-hero-left,
        .smart-home-hero-image {
            width: 100% !important;
        }

        .smart-home-hero-image {
            min-height: 300px !important;
        }

        .smart-home-features-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
        }

        .smart-home-footer-inner {
            grid-template-columns: 1fr !important;
            text-align: left !important;
        }

        .smart-home-copyright {
            text-align: left !important;
        }
    }

    @media (max-width: 650px) {
        .smart-home-features-grid {
            grid-template-columns: 1fr !important;
        }
    }
            .smart-home-print-only {
        display: none;
    }

    @media print {

        @page {
            size: landscape;
            margin: 10mm;
        }

        html,
        body {
            margin: 0 !important;
            padding: 0 !important;
            background: #ffffff !important;
        }

        /*
         * HIDE THE ENTIRE WEBSITE.
         */
        body * {
            visibility: hidden !important;
        }

        /*
         * SHOW ONLY OUR PRINT TIMETABLE.
         */
        .smart-home-print-only,
        .smart-home-print-only * {
            visibility: visible !important;
        }

        .smart-home-print-only {
            display: block !important;
            position: absolute !important;
            left: 0 !important;
            top: 0 !important;
            width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;
        }

        /*
         * ONE PRINT PAGE PER ACTUAL TIMETABLE GROUP.
         *
         * If Year 1 + Semester 1 is selected,
         * there is only ONE group,
         * therefore only ONE page.
         */
        .smart-home-print-page {
            display: block !important;
            width: 100% !important;
            margin: 0 !important;
            padding: 0 !important;

            page-break-after: always !important;
            break-after: page !important;
        }

        .smart-home-print-page:last-child {
            page-break-after: auto !important;
            break-after: auto !important;
        }

        /*
         * NEVER PRINT THE NORMAL WEBSITE TIMETABLE.
         */
        .smart-home-web-table,
        .smart-home-timetable-filter-row,
        .smart-home-timetable-web-header,
        .smart-home-selected-schedule,
        .smart-home-format-note {
            display: none !important;
        }

        /*
         * PRINT HEADER
         */
        .smart-home-print-header {
            display: block !important;
            visibility: visible !important;

            color: #000000 !important;

            font-size: 15px !important;
            font-weight: 700 !important;

            text-align: center !important;

            white-space: nowrap !important;

            margin: 0 0 10px !important;
            padding: 0 0 6px !important;

            border-bottom: 1px solid #000000 !important;
        }

        /*
         * PRINT TABLE
         */
        .smart-home-print-table {
            display: table !important;
            visibility: visible !important;

            width: 100% !important;
            min-width: 0 !important;

            border-collapse: collapse !important;
            table-layout: fixed !important;

            background: #ffffff !important;
            color: #000000 !important;
        }

        .smart-home-print-table th,
        .smart-home-print-table td {
            border: 1px solid #000000 !important;

            color: #000000 !important;

            text-align: center !important;
            vertical-align: middle !important;

            padding: 7px 5px !important;

            font-size: 11px !important;
        }

        .smart-home-print-table th {
            font-weight: 700 !important;
            background: #ffffff !important;
        }

        .smart-home-print-table th:first-child,
        .smart-home-print-table td:first-child {
            width: 11% !important;

            font-weight: 700 !important;

            white-space: nowrap !important;
        }

        .smart-home-print-table tr {
            page-break-inside: avoid !important;
            break-inside: avoid !important;
        }
    }
`;

export default Home;