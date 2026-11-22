import { useEffect, useMemo, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

function StudentSubjectRegistration() {
    const navigate = useNavigate();

    const [student, setStudent] = useState(null);
    const [subjects, setSubjects] = useState([]);
    const [specializations, setSpecializations] = useState([]);
    const [selectedSubjects, setSelectedSubjects] = useState([]);
    const [majorSpecializationId, setMajorSpecializationId] = useState("");
    const [minorSpecializationId, setMinorSpecializationId] = useState("");
    const [alreadyComplete, setAlreadyComplete] = useState(false);

    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [registeredNow, setRegisteredNow] = useState([]);

    const token = localStorage.getItem("token");
    const username = localStorage.getItem("username");

    const config = useMemo(
        () => ({
            headers: {
                Authorization: `Bearer ${token}`
            }
        }),
        [token]
    );

    useEffect(() => {
        loadRegistrationData();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    // ============================================================
    // LOAD REGISTRATION DATA
    // ============================================================

    const loadRegistrationData = async () => {
        try {
            setLoading(true);
            setError("");
            setSuccess("");

            if (!token || !username) {
                setError("You are not logged in.");
                return;
            }

            // ----------------------------------------------------
            // GET CURRENT STUDENT
            // ----------------------------------------------------

            const studentResponse = await axios.get(
                "http://localhost:8080/api/students",
                config
            );

            const students = Array.isArray(studentResponse.data)
                ? studentResponse.data
                : [];

            const currentStudent = students.find(
                (item) =>
                    item.registrationNumber === username ||
                    item.user?.username === username ||
                    item.username === username ||
                    item.email === username
            );

            if (!currentStudent) {
                setError("Student information could not be found.");
                return;
            }

            setStudent(currentStudent);

            const studentId = currentStudent.studentId;

            if (!studentId) {
                setError("Student ID could not be found.");
                return;
            }

            // ----------------------------------------------------
            // CHECK REGISTRATION STATUS
            // ----------------------------------------------------

            const statusResponse = await axios.get(
                "http://localhost:8080/api/student-subject-registrations/status/me",
                config
            );

            const status = statusResponse.data || {};

            if (status.complete === true) {
                setAlreadyComplete(true);

                setSuccess(
                    "Your registration for the current academic period is already complete."
                );

                return;
            }

            // ----------------------------------------------------
            // LOAD SPECIALIZATIONS
            // ----------------------------------------------------

            if (Number(currentStudent.yearLevel) >= 2) {
                const specializationResponse = await axios.get(
                    "http://localhost:8080/api/student-subject-registrations/specializations",
                    config
                );

                const specializationData =
                    Array.isArray(specializationResponse.data)
                        ? specializationResponse.data
                        : [];

                setSpecializations(specializationData);

                if (currentStudent.majorSpecializationId) {
                    setMajorSpecializationId(
                        String(currentStudent.majorSpecializationId)
                    );
                }

                if (currentStudent.minorSpecializationId) {
                    setMinorSpecializationId(
                        String(currentStudent.minorSpecializationId)
                    );
                }
            }

            // ----------------------------------------------------
            // LOAD AVAILABLE SUBJECTS
            // ----------------------------------------------------

            const availableResponse = await axios.get(
                `http://localhost:8080/api/student-subject-registrations/available/student/${studentId}`,
                config
            );

            setSubjects(
                Array.isArray(availableResponse.data)
                    ? availableResponse.data
                    : []
            );
        } catch (err) {
            console.error("Registration loading error:", err);

            setError(
                err.response?.data?.message ||
                    err.response?.data ||
                    "Unable to load your registration information."
            );
        } finally {
            setLoading(false);
        }
    };

    // ============================================================
    // STUDENT INFORMATION
    // ============================================================

    const year = Number(student?.yearLevel || 0);
    const semester = Number(student?.semester || 0);

    const isYearOne = year === 1;
    const isYearTwoOrThree = year === 2 || year === 3;
    const isYearFour = year === 4;

    // ============================================================
    // YEAR 1 SUBJECT SELECTION
    // ============================================================

    const togglePreferredSubject = (subjectId) => {
        setError("");
        setSuccess("");

        setSelectedSubjects((current) => {
            if (current.includes(subjectId)) {
                return current.filter((id) => id !== subjectId);
            }

            if (current.length >= 3) {
                setError(
                    "You can select only 3 preferred subjects. Remove one selected subject before choosing another."
                );

                return current;
            }

            return [...current, subjectId];
        });
    };

    // ============================================================
    // COMPLETE REGISTRATION
    // ============================================================

    const handleCompleteRegistration = async () => {
        setError("");
        setSuccess("");
        setRegisteredNow([]);

        if (!student?.studentId) {
            setError("Student information is not available.");
            return;
        }

        // --------------------------------------------------------
        // YEAR 4 SEMESTER VALIDATION
        // --------------------------------------------------------

        if (year === 4 && semester !== 1) {
            setError(
                "Year 4 students can only register for Semester 1."
            );

            return;
        }

        // --------------------------------------------------------
        // YEAR 1 VALIDATION
        // --------------------------------------------------------

        if (isYearOne && selectedSubjects.length !== 3) {
            setError(
                "Please select exactly 3 preferred subjects before completing registration."
            );

            return;
        }

        // --------------------------------------------------------
        // YEAR 2 / YEAR 3 VALIDATION
        // --------------------------------------------------------

        if (isYearTwoOrThree) {
            if (!majorSpecializationId || !minorSpecializationId) {
                setError(
                    `Year ${year} students must select both a major and a minor specialization.`
                );

                return;
            }

            if (
                String(majorSpecializationId) ===
                String(minorSpecializationId)
            ) {
                setError(
                    "Major and minor specializations must be different."
                );

                return;
            }
        }

        // --------------------------------------------------------
        // YEAR 4 VALIDATION
        // --------------------------------------------------------

        if (isYearFour && !majorSpecializationId) {
            setError(
                "Please select your major specialization before completing registration."
            );

            return;
        }

        try {
            setSubmitting(true);

            const response = await axios.post(
                "http://localhost:8080/api/student-subject-registrations/complete",
                {
                    majorSpecializationId: majorSpecializationId
                        ? Number(majorSpecializationId)
                        : null,

                    minorSpecializationId: minorSpecializationId
                        ? Number(minorSpecializationId)
                        : null,

                    preferredSubjectIds: isYearOne
                        ? selectedSubjects.map(Number)
                        : []
                },
                config
            );

            const registrations = Array.isArray(response.data)
                ? response.data
                : [];

            setRegisteredNow(registrations);

            setAlreadyComplete(true);

            setSuccess(
                `Your registration for Year ${year}, Semester ${semester} has been completed successfully.`
            );
        } catch (err) {
            console.error("Registration completion error:", err);

            setError(
                err.response?.data?.message ||
                    err.response?.data ||
                    "Unable to complete registration."
            );
        } finally {
            setSubmitting(false);
        }
    };

    // ============================================================
    // LOADING SCREEN
    // ============================================================

    if (loading) {
        return (
            <div style={pageStyle}>
                <style>{responsiveStyles}</style>

                <div style={loadingCardStyle}>
                    <div style={loadingIconStyle}>⟳</div>

                    <h2 style={loadingTitleStyle}>
                        Preparing Your Registration
                    </h2>

                    <p style={mutedTextStyle}>
                        Checking your academic information and available
                        registration options...
                    </p>
                </div>
            </div>
        );
    }

    // ============================================================
    // REGISTRATION COMPLETED SCREEN
    // ============================================================

    if (alreadyComplete) {
        return (
            <div style={pageStyle}>
                <style>{responsiveStyles}</style>

                <div style={completedCardStyle}>
                    <div style={completedIconStyle}>✓</div>

                    <div style={completedLabelStyle}>
                        REGISTRATION COMPLETE
                    </div>

                    <h1 style={completedTitleStyle}>
                        Your Registration is Complete
                    </h1>

                    <p style={completedTextStyle}>
                        {success ||
                            "Your subjects for the current academic period have been registered successfully."}
                    </p>

                    <div style={academicSummaryStyle}>
                        <AcademicItem
                            label="Academic Year"
                            value={`Year ${year}`}
                        />

                        <AcademicItem
                            label="Semester"
                            value={`Semester ${semester}`}
                        />

                        <AcademicItem
                            label="Registration Number"
                            value={
                                student?.registrationNumber ||
                                username ||
                                "-"
                            }
                        />
                    </div>

                    {registeredNow.length > 0 && (
                        <div style={registeredCardStyle}>
                            <div style={sectionHeaderRowStyle}>
                                <div>
                                    <h3 style={sectionTitleStyle}>
                                        Subjects Registered
                                    </h3>

                                    <p style={sectionSubtitleStyle}>
                                        The following subjects have been
                                        registered for you.
                                    </p>
                                </div>

                                <div style={countBadgeStyle}>
                                    {registeredNow.length} Subjects
                                </div>
                            </div>

                            <div style={subjectGridStyle}>
                                {registeredNow.map((registration) => (
                                    <RegisteredSubjectCard
                                        key={registration.registrationId}
                                        registration={registration}
                                    />
                                ))}
                            </div>
                        </div>
                    )}

                    <button
                        type="button"
                        onClick={() =>
                            navigate("/student-dashboard")
                        }
                        style={primaryButtonStyle}
                    >
                        Continue to Student Dashboard
                        <span style={buttonArrowStyle}>→</span>
                    </button>
                </div>
            </div>
        );
    }

    // ============================================================
    // MAIN REGISTRATION PAGE
    // ============================================================

    return (
        <div style={pageStyle}>
            <style>{responsiveStyles}</style>

            <div style={containerStyle}>
                {/* =================================================
                    PAGE HEADER
                ================================================= */}

                <div style={headerCardStyle}>
                    <div>
                        <div style={eyebrowStyle}>
                            SMART FACULTY
                        </div>

                        <h1 style={pageTitleStyle}>
                            Student Subject Registration
                        </h1>

                        <p style={pageSubtitleStyle}>
                            Complete your subject registration for the
                            current academic period.
                        </p>
                    </div>

                    <div style={headerAcademicBadgeStyle}>
                        <div style={headerBadgeSmallStyle}>
                            CURRENT PERIOD
                        </div>

                        <div style={headerBadgeMainStyle}>
                            Year {year} · Semester {semester}
                        </div>
                    </div>
                </div>

                {/* =================================================
                    ERROR
                ================================================= */}

                {error && (
                    <div style={errorStyle}>
                        <div style={messageIconStyle}>!</div>

                        <div>
                            <strong style={{ display: "block" }}>
                                Registration Notice
                            </strong>

                            <span>{error}</span>
                        </div>
                    </div>
                )}

                {/* =================================================
                    SUCCESS
                ================================================= */}

                {success && (
                    <div style={successBoxStyle}>
                        <div style={messageSuccessIconStyle}>✓</div>

                        <div>
                            <strong style={{ display: "block" }}>
                                Registration Update
                            </strong>

                            <span>{success}</span>
                        </div>
                    </div>
                )}

                {/* =================================================
                    STUDENT ACADEMIC INFORMATION
                ================================================= */}

                <div style={cardStyle}>
                    <div style={sectionHeaderRowStyle}>
                        <div>
                            <h2 style={sectionTitleStyle}>
                                My Academic Information
                            </h2>

                            <p style={sectionSubtitleStyle}>
                                Your current academic details
                            </p>
                        </div>
                    </div>

                    <div style={academicGridStyle}>
                        <AcademicItem
                            label="Student Name"
                            value={
                                student?.studentName ||
                                student?.student_name ||
                                username ||
                                "-"
                            }
                        />

                        <AcademicItem
                            label="Registration Number"
                            value={
                                student?.registrationNumber ||
                                student?.registration_number ||
                                "-"
                            }
                        />

                        <AcademicItem
                            label="Academic Year"
                            value={`Year ${year}`}
                        />

                        <AcademicItem
                            label="Semester"
                            value={`Semester ${semester}`}
                        />
                    </div>
                </div>

                {/* =================================================
                    REGISTRATION RULE
                ================================================= */}

                <RegistrationRule
                    year={year}
                    semester={semester}
                    isYearOne={isYearOne}
                    isYearTwoOrThree={isYearTwoOrThree}
                    isYearFour={isYearFour}
                />

                {/* =================================================
                    YEAR 1
                ================================================= */}

                {isYearOne && (
                    <div style={cardStyle}>
                        <div style={sectionHeaderRowStyle}>
                            <div>
                                <h2 style={sectionTitleStyle}>
                                    Choose Your Preferred Subjects
                                </h2>

                                <p style={sectionSubtitleStyle}>
                                    Select exactly 3 subjects from the
                                    available options below.
                                </p>
                            </div>

                            <div
                                style={{
                                    ...selectionCounterStyle,
                                    ...(selectedSubjects.length === 3
                                        ? selectionCounterCompleteStyle
                                        : {})
                                }}
                            >
                                <span style={counterNumberStyle}>
                                    {selectedSubjects.length}
                                </span>

                                <span style={counterSlashStyle}>
                                    /
                                </span>

                                <span style={counterTotalStyle}>
                                    3
                                </span>

                                <span style={counterLabelStyle}>
                                    Selected
                                </span>
                            </div>
                        </div>

                        {/* Progress */}

                        <div style={progressContainerStyle}>
                            <div
                                style={{
                                    ...progressBarStyle,
                                    width: `${
                                        Math.min(
                                            selectedSubjects.length,
                                            3
                                        ) * 33.3333
                                    }%`
                                }}
                            />
                        </div>

                        {subjects.length === 0 ? (
                            <EmptyState
                                title="No Subjects Available"
                                message="There are currently no preferred subjects available for your academic year and semester."
                            />
                        ) : (
                            <div style={subjectGridStyle}>
                                {subjects.map((subject) => {
                                    const isSelected =
                                        selectedSubjects.includes(
                                            subject.subjectId
                                        );

                                    return (
                                        <SubjectSelectionCard
                                            key={subject.subjectId}
                                            subject={subject}
                                            selected={isSelected}
                                            onClick={() =>
                                                togglePreferredSubject(
                                                    subject.subjectId
                                                )
                                            }
                                        />
                                    );
                                })}
                            </div>
                        )}

                        <div style={selectionHintStyle}>
                            <span style={hintIconStyle}>✓</span>

                            <span>
                                {selectedSubjects.length === 3
                                    ? "You have selected all 3 preferred subjects. You can now complete your registration."
                                    : `Select ${
                                          3 -
                                          selectedSubjects.length
                                      } more subject${
                                          3 -
                                              selectedSubjects.length !==
                                          1
                                              ? "s"
                                              : ""
                                      }.`}
                            </span>
                        </div>
                    </div>
                )}

                {/* =================================================
                    YEAR 2 / 3
                ================================================= */}

                {isYearTwoOrThree && (
                    <div style={cardStyle}>
                        <div style={sectionHeaderRowStyle}>
                            <div>
                                <h2 style={sectionTitleStyle}>
                                    Choose Your Specializations
                                </h2>

                                <p style={sectionSubtitleStyle}>
                                    Select one major and one different
                                    minor specialization.
                                </p>
                            </div>
                        </div>

                        <div style={specializationGridStyle}>
                            <SpecializationCard
                                number="01"
                                title="Major Specialization"
                                description="Your primary field of specialization."
                                value={majorSpecializationId}
                                onChange={setMajorSpecializationId}
                                options={specializations}
                            />

                            <SpecializationCard
                                number="02"
                                title="Minor Specialization"
                                description="Your secondary field of specialization."
                                value={minorSpecializationId}
                                onChange={setMinorSpecializationId}
                                options={specializations.filter(
                                    (item) =>
                                        String(
                                            item.specializationId
                                        ) !==
                                        String(
                                            majorSpecializationId
                                        )
                                )}
                            />
                        </div>

                        <AutomaticRegistrationInfo
                            subjects={subjects}
                            year={year}
                            majorSelected={majorSpecializationId}
                            minorSelected={minorSpecializationId}
                        />
                    </div>
                )}

                {/* =================================================
                    YEAR 4
                ================================================= */}

                {isYearFour && (
                    <div style={cardStyle}>
                        <div style={sectionHeaderRowStyle}>
                            <div>
                                <h2 style={sectionTitleStyle}>
                                    Choose Your Major Specialization
                                </h2>

                                <p style={sectionSubtitleStyle}>
                                    Year 4 students select one major
                                    specialization for Semester 1.
                                </p>
                            </div>
                        </div>

                        <SpecializationCard
                            number="01"
                            title="Major Specialization"
                            description="Your primary field of specialization for Year 4."
                            value={majorSpecializationId}
                            onChange={setMajorSpecializationId}
                            options={specializations}
                        />

                        <AutomaticRegistrationInfo
                            subjects={subjects}
                            year={year}
                            majorSelected={majorSpecializationId}
                            minorSelected={null}
                        />
                    </div>
                )}

                {/* =================================================
                    COMPLETE BUTTON
                ================================================= */}

                <div style={submitAreaStyle}>
                    <div style={submitInfoStyle}>
                        <div style={submitIconStyle}>✓</div>

                        <div>
                            <strong style={submitTitleStyle}>
                                Ready to complete your registration?
                            </strong>

                            <p style={submitDescriptionStyle}>
                                Your compulsory and specialization
                                subjects will be registered automatically
                                according to your academic year.
                            </p>
                        </div>
                    </div>

                    <button
                        type="button"
                        onClick={handleCompleteRegistration}
                        disabled={submitting}
                        style={{
                            ...completeButtonStyle,
                            ...(submitting
                                ? disabledButtonStyle
                                : {})
                        }}
                    >
                        {submitting
                            ? "Completing Registration..."
                            : "Complete Registration"}

                        {!submitting && (
                            <span style={buttonArrowStyle}>
                                →
                            </span>
                        )}
                    </button>
                </div>

                <div style={footerHintStyle}>
                    Smart Faculty · Timetable Management System
                </div>
            </div>
        </div>
    );
}

// ================================================================
// ACADEMIC INFORMATION ITEM
// ================================================================

function AcademicItem({ label, value }) {
    return (
        <div style={academicItemStyle}>
            <div style={academicLabelStyle}>
                {label}
            </div>

            <div style={academicValueStyle}>
                {value || "-"}
            </div>
        </div>
    );
}

// ================================================================
// REGISTRATION RULE CARD
// ================================================================

function RegistrationRule({
    year,
    semester,
    isYearOne,
    isYearTwoOrThree,
    isYearFour
}) {
    let title = "";
    let description = "";
    let points = [];

    if (isYearOne) {
        title = "Year 1 Registration";
        description =
            "Your compulsory subjects will be registered automatically.";

        points = [
            "Select exactly 3 preferred subjects.",
            "Only your 3 selected preferred subjects will be registered.",
            "Compulsory subjects are added automatically."
        ];
    } else if (isYearTwoOrThree) {
        title = `Year ${year} Registration`;
        description =
            "Choose your major and minor specializations.";

        points = [
            "Select one major specialization.",
            "Select one different minor specialization.",
            "Relevant specialization subjects are added automatically.",
            "Compulsory subjects are added automatically."
        ];
    } else if (isYearFour) {
        title = "Year 4 Registration";
        description =
            "Year 4 registration is available for Semester 1 only.";

        points = [
            "Select one major specialization.",
            "Relevant major specialization subjects are added automatically.",
            "Compulsory subjects are added automatically.",
            "Year 4 has Semester 1 only."
        ];
    } else {
        title = "Registration";
        description =
            "Complete the required registration for your academic period.";
    }

    return (
        <div style={ruleCardStyle}>
            <div style={ruleIconStyle}>i</div>

            <div style={{ flex: 1 }}>
                <div style={ruleTopRowStyle}>
                    <div>
                        <div style={ruleEyebrowStyle}>
                            REGISTRATION GUIDE
                        </div>

                        <h2 style={ruleTitleStyle}>
                            {title}
                        </h2>

                        <p style={ruleDescriptionStyle}>
                            {description}
                        </p>
                    </div>

                    <div style={periodBadgeStyle}>
                        Year {year} · Semester {semester}
                    </div>
                </div>

                {points.length > 0 && (
                    <div style={rulePointsStyle}>
                        {points.map((point, index) => (
                            <div
                                key={index}
                                style={rulePointStyle}
                            >
                                <span style={ruleCheckStyle}>
                                    ✓
                                </span>

                                <span>{point}</span>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

// ================================================================
// SUBJECT SELECTION CARD
// ================================================================

function SubjectSelectionCard({
    subject,
    selected,
    onClick
}) {
    return (
        <button
            type="button"
            onClick={onClick}
            style={{
                ...subjectCardStyle,
                ...(selected
                    ? subjectCardSelectedStyle
                    : {})
            }}
        >
            <div style={subjectCardTopStyle}>
                <div
                    style={{
                        ...checkboxStyle,
                        ...(selected
                            ? checkboxSelectedStyle
                            : {})
                    }}
                >
                    {selected ? "✓" : ""}
                </div>

                <div
                    style={{
                        ...subjectCodeStyle,
                        ...(selected
                            ? subjectCodeSelectedStyle
                            : {})
                    }}
                >
                    {subject.subjectCode || "SUBJECT"}
                </div>
            </div>

            <div style={subjectNameStyle}>
                {subject.subjectName ||
                    "Subject Name"}
            </div>

            <div style={subjectMetaRowStyle}>
                <span>
                    Credits
                </span>

                <strong>
                    {subject.credits ?? "-"}
                </strong>
            </div>
        </button>
    );
}

// ================================================================
// SPECIALIZATION CARD
// ================================================================

function SpecializationCard({
    number,
    title,
    description,
    value,
    onChange,
    options
}) {
    return (
        <div style={specializationCardStyle}>
            <div style={specializationNumberStyle}>
                {number}
            </div>

            <div style={specializationCardContentStyle}>
                <h3 style={specializationTitleStyle}>
                    {title}
                </h3>

                <p style={specializationDescriptionStyle}>
                    {description}
                </p>

                <select
                    value={value}
                    onChange={(e) =>
                        onChange(e.target.value)
                    }
                    style={specializationSelectStyle}
                >
                    <option value="">
                        Select {title}
                    </option>

                    {options.map((specialization) => (
                        <option
                            key={
                                specialization.specializationId
                            }
                            value={
                                specialization.specializationId
                            }
                        >
                            {specialization.specializationCode}{" "}
                            -{" "}
                            {specialization.specializationName}
                        </option>
                    ))}
                </select>

                {value && (
                    <div style={selectedSpecializationStyle}>
                        <span>✓</span>
                        <span>
                            Specialization selected
                        </span>
                    </div>
                )}
            </div>
        </div>
    );
}

// ================================================================
// AUTOMATIC REGISTRATION INFORMATION
// ================================================================

function AutomaticRegistrationInfo({
    subjects,
    year,
    majorSelected,
    minorSelected
}) {
    return (
        <div style={automaticCardStyle}>
            <div style={automaticHeaderStyle}>
                <div style={automaticIconStyle}>↻</div>

                <div>
                    <h3 style={automaticTitleStyle}>
                        Subjects Registered Automatically
                    </h3>

                    <p style={automaticSubtitleStyle}>
                        After completing registration, the system
                        will automatically register the required
                        subjects for Year {year}.
                    </p>
                </div>
            </div>

            {subjects.length > 0 ? (
                <div style={automaticSubjectsGridStyle}>
                    {subjects.map((subject) => (
                        <div
                            key={subject.subjectId}
                            style={automaticSubjectStyle}
                        >
                            <div style={automaticSubjectCodeStyle}>
                                {subject.subjectCode}
                            </div>

                            <div style={automaticSubjectNameStyle}>
                                {subject.subjectName}
                            </div>

                            <div style={automaticSubjectCreditsStyle}>
                                {subject.credits ?? "-"} Credits
                            </div>
                        </div>
                    ))}
                </div>
            ) : (
                <div style={automaticEmptyStyle}>
                    Subject details will be shown according to
                    your selected specialization.
                </div>
            )}

            {!majorSelected && (
                <div style={automaticHintStyle}>
                    Select your specialization above to continue.
                </div>
            )}
        </div>
    );
}

// ================================================================
// REGISTERED SUBJECT CARD
// ================================================================

function RegisteredSubjectCard({
    registration
}) {
    const subject = registration?.subject;

    return (
        <div style={registeredSubjectStyle}>
            <div style={registeredCheckStyle}>
                ✓
            </div>

            <div style={{ flex: 1 }}>
                <div style={registeredCodeStyle}>
                    {subject?.subjectCode || "-"}
                </div>

                <div style={registeredNameStyle}>
                    {subject?.subjectName ||
                        "Subject"}
                </div>

                <div style={registeredCreditsStyle}>
                    {subject?.credits ?? "-"} Credits
                </div>
            </div>
        </div>
    );
}

// ================================================================
// EMPTY STATE
// ================================================================

function EmptyState({
    title,
    message
}) {
    return (
        <div style={emptyStateStyle}>
            <div style={emptyStateIconStyle}>
                📚
            </div>

            <h3 style={emptyStateTitleStyle}>
                {title}
            </h3>

            <p style={emptyStateTextStyle}>
                {message}
            </p>
        </div>
    );
}

// ================================================================
// PAGE STYLES
// ================================================================

const pageStyle = {
    minHeight: "100vh",
    backgroundColor: "#f4f7fa",
    padding: "32px 20px 50px",
    boxSizing: "border-box",
    fontFamily:
        "Arial, Helvetica, sans-serif",
    color: "#23415f"
};

const containerStyle = {
    width: "100%",
    maxWidth: "1250px",
    margin: "0 auto"
};

const headerCardStyle = {
    backgroundColor: "#ffffff",
    border: "1px solid #d5e0e8",
    borderRadius: "10px",
    padding: "25px 30px",
    marginBottom: "22px",
    boxShadow:
        "0 2px 8px rgba(0,0,0,0.05)",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "20px"
};

const eyebrowStyle = {
    color: "#55718b",
    fontSize: "11px",
    fontWeight: "700",
    letterSpacing: "1.5px",
    marginBottom: "7px"
};

const pageTitleStyle = {
    margin: "0 0 7px",
    fontSize: "28px",
    color: "#23415f",
    fontWeight: "700"
};

const pageSubtitleStyle = {
    margin: 0,
    color: "#687b8d",
    fontSize: "14px",
    lineHeight: 1.5
};

const headerAcademicBadgeStyle = {
    minWidth: "190px",
    backgroundColor: "#eef5fa",
    border: "1px solid #cbdce8",
    borderRadius: "8px",
    padding: "13px 16px",
    textAlign: "center"
};

const headerBadgeSmallStyle = {
    fontSize: "10px",
    fontWeight: "700",
    letterSpacing: "1px",
    color: "#71869a",
    marginBottom: "5px"
};

const headerBadgeMainStyle = {
    fontSize: "14px",
    fontWeight: "700",
    color: "#23415f"
};

const cardStyle = {
    backgroundColor: "#ffffff",
    border: "1px solid #d5e0e8",
    borderRadius: "10px",
    padding: "25px 30px",
    marginBottom: "22px",
    boxShadow:
        "0 2px 8px rgba(0,0,0,0.05)"
};

const sectionHeaderRowStyle = {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
    marginBottom: "20px"
};

const sectionTitleStyle = {
    margin: 0,
    color: "#23415f",
    fontSize: "19px",
    fontWeight: "700"
};

const sectionSubtitleStyle = {
    margin: "5px 0 0",
    color: "#7b8b99",
    fontSize: "13px",
    lineHeight: 1.5
};

const academicGridStyle = {
    display: "grid",
    gridTemplateColumns:
        "repeat(4, minmax(0, 1fr))",
    gap: "20px"
};

const academicItemStyle = {
    borderLeft: "3px solid #d5e5ef",
    paddingLeft: "13px"
};

const academicLabelStyle = {
    fontSize: "11px",
    color: "#8797a5",
    marginBottom: "6px",
    textTransform: "uppercase",
    letterSpacing: "0.5px"
};

const academicValueStyle = {
    fontWeight: "600",
    fontSize: "15px",
    color: "#263f55",
    lineHeight: 1.4
};

const ruleCardStyle = {
    backgroundColor: "#eef5fa",
    border: "1px solid #cbdce8",
    borderRadius: "10px",
    padding: "20px 22px",
    marginBottom: "22px",
    display: "flex",
    gap: "16px"
};

const ruleIconStyle = {
    width: "34px",
    height: "34px",
    minWidth: "34px",
    borderRadius: "50%",
    backgroundColor: "#23415f",
    color: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "700",
    fontSize: "16px"
};

const ruleTopRowStyle = {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "flex-start",
    gap: "20px"
};

const ruleEyebrowStyle = {
    color: "#71869a",
    fontSize: "10px",
    fontWeight: "700",
    letterSpacing: "1.2px",
    marginBottom: "4px"
};

const ruleTitleStyle = {
    margin: 0,
    fontSize: "18px",
    color: "#23415f"
};

const ruleDescriptionStyle = {
    margin: "5px 0 0",
    color: "#687b8d",
    fontSize: "13px"
};

const periodBadgeStyle = {
    backgroundColor: "#ffffff",
    border: "1px solid #cbdce8",
    borderRadius: "20px",
    padding: "7px 12px",
    fontSize: "11px",
    fontWeight: "700",
    color: "#23415f",
    whiteSpace: "nowrap"
};

const rulePointsStyle = {
    marginTop: "15px",
    display: "grid",
    gridTemplateColumns:
        "repeat(2, minmax(0, 1fr))",
    gap: "9px 20px"
};

const rulePointStyle = {
    display: "flex",
    alignItems: "flex-start",
    gap: "8px",
    color: "#526b7e",
    fontSize: "13px",
    lineHeight: 1.5
};

const ruleCheckStyle = {
    color: "#18794e",
    fontWeight: "700"
};

const selectionCounterStyle = {
    display: "flex",
    alignItems: "baseline",
    gap: "2px",
    backgroundColor: "#f4f7fa",
    border: "1px solid #d5e0e8",
    borderRadius: "8px",
    padding: "8px 13px",
    whiteSpace: "nowrap"
};

const selectionCounterCompleteStyle = {
    backgroundColor: "#eaf7ee",
    borderColor: "#bde5c8"
};

const counterNumberStyle = {
    fontSize: "21px",
    fontWeight: "700",
    color: "#23415f"
};

const counterSlashStyle = {
    color: "#9aa8b3",
    fontWeight: "600"
};

const counterTotalStyle = {
    fontSize: "17px",
    fontWeight: "600",
    color: "#687b8d"
};

const counterLabelStyle = {
    marginLeft: "5px",
    fontSize: "11px",
    color: "#7b8b99"
};

const progressContainerStyle = {
    height: "6px",
    backgroundColor: "#e4ebf0",
    borderRadius: "20px",
    overflow: "hidden",
    marginBottom: "22px"
};

const progressBarStyle = {
    height: "100%",
    backgroundColor: "#23415f",
    borderRadius: "20px",
    transition: "width 0.25s ease"
};

const subjectGridStyle = {
    display: "grid",
    gridTemplateColumns:
        "repeat(3, minmax(0, 1fr))",
    gap: "15px"
};

const subjectCardStyle = {
    width: "100%",
    boxSizing: "border-box",
    border: "1px solid #d5e0e8",
    borderRadius: "9px",
    backgroundColor: "#ffffff",
    padding: "17px",
    textAlign: "left",
    cursor: "pointer",
    transition:
        "border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease",
    color: "#23415f"
};

const subjectCardSelectedStyle = {
    border: "2px solid #23415f",
    backgroundColor: "#f3f7fa",
    boxShadow:
        "0 3px 10px rgba(35,65,95,0.10)"
};

const subjectCardTopStyle = {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    marginBottom: "15px"
};

const checkboxStyle = {
    width: "22px",
    height: "22px",
    minWidth: "22px",
    border: "1px solid #b9c8d3",
    borderRadius: "5px",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#ffffff",
    fontWeight: "700",
    fontSize: "13px",
    boxSizing: "border-box"
};

const checkboxSelectedStyle = {
    backgroundColor: "#23415f",
    borderColor: "#23415f"
};

const subjectCodeStyle = {
    fontSize: "12px",
    fontWeight: "700",
    letterSpacing: "0.4px",
    color: "#55718b"
};

const subjectCodeSelectedStyle = {
    color: "#23415f"
};

const subjectNameStyle = {
    minHeight: "42px",
    fontSize: "14px",
    fontWeight: "600",
    color: "#263f55",
    lineHeight: 1.5,
    marginBottom: "15px"
};

const subjectMetaRowStyle = {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    borderTop: "1px solid #edf1f4",
    paddingTop: "11px",
    fontSize: "11px",
    color: "#8797a5"
};

const selectionHintStyle = {
    display: "flex",
    alignItems: "center",
    gap: "8px",
    marginTop: "18px",
    backgroundColor: "#f7f9fb",
    border: "1px solid #e1e8ed",
    borderRadius: "7px",
    padding: "11px 13px",
    color: "#627789",
    fontSize: "12px"
};

const hintIconStyle = {
    color: "#18794e",
    fontWeight: "700"
};

const specializationGridStyle = {
    display: "grid",
    gridTemplateColumns:
        "repeat(2, minmax(0, 1fr))",
    gap: "20px",
    marginBottom: "20px"
};

const specializationCardStyle = {
    display: "flex",
    gap: "16px",
    border: "1px solid #d5e0e8",
    borderRadius: "9px",
    padding: "20px",
    backgroundColor: "#ffffff"
};

const specializationNumberStyle = {
    width: "35px",
    height: "35px",
    minWidth: "35px",
    borderRadius: "50%",
    backgroundColor: "#eef5fa",
    color: "#23415f",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "11px",
    fontWeight: "700"
};

const specializationCardContentStyle = {
    flex: 1
};

const specializationTitleStyle = {
    margin: "0 0 5px",
    color: "#23415f",
    fontSize: "16px"
};

const specializationDescriptionStyle = {
    margin: "0 0 14px",
    color: "#7b8b99",
    fontSize: "12px",
    lineHeight: 1.5
};

const specializationSelectStyle = {
    width: "100%",
    padding: "11px 12px",
    border: "1px solid #cbd5df",
    borderRadius: "6px",
    fontSize: "13px",
    color: "#263f55",
    backgroundColor: "#ffffff",
    outline: "none",
    boxSizing: "border-box"
};

const selectedSpecializationStyle = {
    display: "flex",
    alignItems: "center",
    gap: "6px",
    marginTop: "9px",
    fontSize: "11px",
    color: "#18794e",
    fontWeight: "600"
};

const automaticCardStyle = {
    backgroundColor: "#f7f9fb",
    border: "1px solid #e0e7ec",
    borderRadius: "9px",
    padding: "18px"
};

const automaticHeaderStyle = {
    display: "flex",
    gap: "12px",
    alignItems: "flex-start",
    marginBottom: "15px"
};

const automaticIconStyle = {
    width: "32px",
    height: "32px",
    minWidth: "32px",
    borderRadius: "50%",
    backgroundColor: "#23415f",
    color: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "15px",
    fontWeight: "700"
};

const automaticTitleStyle = {
    margin: "0 0 4px",
    fontSize: "15px",
    color: "#23415f"
};

const automaticSubtitleStyle = {
    margin: 0,
    fontSize: "12px",
    color: "#7b8b99",
    lineHeight: 1.5
};

const automaticSubjectsGridStyle = {
    display: "grid",
    gridTemplateColumns:
        "repeat(3, minmax(0, 1fr))",
    gap: "10px"
};

const automaticSubjectStyle = {
    backgroundColor: "#ffffff",
    border: "1px solid #dbe3e8",
    borderRadius: "7px",
    padding: "12px"
};

const automaticSubjectCodeStyle = {
    fontSize: "11px",
    fontWeight: "700",
    color: "#55718b",
    marginBottom: "5px"
};

const automaticSubjectNameStyle = {
    fontSize: "12px",
    fontWeight: "600",
    color: "#263f55",
    lineHeight: 1.4,
    minHeight: "34px"
};

const automaticSubjectCreditsStyle = {
    fontSize: "10px",
    color: "#8797a5",
    marginTop: "8px"
};

const automaticEmptyStyle = {
    padding: "13px",
    borderRadius: "6px",
    backgroundColor: "#ffffff",
    border: "1px dashed #cbd5df",
    color: "#7b8b99",
    fontSize: "12px"
};

const automaticHintStyle = {
    marginTop: "12px",
    color: "#8a6d3b",
    fontSize: "11px"
};

const submitAreaStyle = {
    backgroundColor: "#ffffff",
    border: "1px solid #d5e0e8",
    borderRadius: "10px",
    padding: "20px 22px",
    boxShadow:
        "0 2px 8px rgba(0,0,0,0.05)",
    display: "flex",
    alignItems: "center",
    justifyContent: "space-between",
    gap: "20px"
};

const submitInfoStyle = {
    display: "flex",
    alignItems: "center",
    gap: "12px"
};

const submitIconStyle = {
    width: "35px",
    height: "35px",
    minWidth: "35px",
    borderRadius: "50%",
    backgroundColor: "#eaf7ee",
    color: "#18794e",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "700"
};

const submitTitleStyle = {
    display: "block",
    color: "#23415f",
    fontSize: "14px"
};

const submitDescriptionStyle = {
    margin: "4px 0 0",
    color: "#7b8b99",
    fontSize: "11px",
    lineHeight: 1.5
};

const completeButtonStyle = {
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#23415f",
    color: "#ffffff",
    padding: "13px 20px",
    fontSize: "13px",
    fontWeight: "700",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "10px",
    minWidth: "190px"
};

const disabledButtonStyle = {
    opacity: 0.65,
    cursor: "not-allowed"
};

const primaryButtonStyle = {
    marginTop: "24px",
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#23415f",
    color: "#ffffff",
    padding: "13px 20px",
    fontSize: "13px",
    fontWeight: "700",
    cursor: "pointer",
    display: "inline-flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "10px"
};

const buttonArrowStyle = {
    fontSize: "16px"
};

const footerHintStyle = {
    textAlign: "center",
    color: "#9aa8b3",
    fontSize: "11px",
    marginTop: "20px"
};

// ================================================================
// MESSAGE STYLES
// ================================================================

const errorStyle = {
    backgroundColor: "#fdecec",
    color: "#a93226",
    border: "1px solid #f2c5c2",
    borderRadius: "8px",
    padding: "13px 16px",
    marginBottom: "18px",
    display: "flex",
    alignItems: "flex-start",
    gap: "10px",
    fontSize: "13px",
    lineHeight: 1.5
};

const messageIconStyle = {
    width: "22px",
    height: "22px",
    minWidth: "22px",
    borderRadius: "50%",
    backgroundColor: "#c0392b",
    color: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "700",
    fontSize: "12px"
};

const successBoxStyle = {
    backgroundColor: "#eaf7ee",
    color: "#18794e",
    border: "1px solid #bde5c8",
    borderRadius: "8px",
    padding: "13px 16px",
    marginBottom: "18px",
    display: "flex",
    alignItems: "flex-start",
    gap: "10px",
    fontSize: "13px",
    lineHeight: 1.5
};

const messageSuccessIconStyle = {
    width: "22px",
    height: "22px",
    minWidth: "22px",
    borderRadius: "50%",
    backgroundColor: "#18794e",
    color: "#ffffff",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontWeight: "700",
    fontSize: "12px"
};

const mutedTextStyle = {
    color: "#687b8d",
    lineHeight: 1.6,
    fontSize: "13px"
};

// ================================================================
// COMPLETED PAGE
// ================================================================

const completedCardStyle = {
    width: "100%",
    maxWidth: "1000px",
    margin: "30px auto",
    backgroundColor: "#ffffff",
    border: "1px solid #d5e0e8",
    borderRadius: "10px",
    padding: "35px",
    boxShadow:
        "0 3px 12px rgba(0,0,0,0.06)",
    textAlign: "center",
    boxSizing: "border-box"
};

const completedIconStyle = {
    width: "64px",
    height: "64px",
    margin: "0 auto 15px",
    borderRadius: "50%",
    backgroundColor: "#eaf7ee",
    color: "#18794e",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "30px",
    fontWeight: "700"
};

const completedLabelStyle = {
    color: "#18794e",
    fontSize: "10px",
    fontWeight: "700",
    letterSpacing: "1.5px",
    marginBottom: "8px"
};

const completedTitleStyle = {
    margin: "0 0 8px",
    color: "#23415f",
    fontSize: "27px"
};

const completedTextStyle = {
    maxWidth: "650px",
    margin: "0 auto",
    color: "#687b8d",
    fontSize: "13px",
    lineHeight: 1.6
};

const academicSummaryStyle = {
    display: "grid",
    gridTemplateColumns:
        "repeat(3, minmax(0, 1fr))",
    gap: "15px",
    marginTop: "25px",
    marginBottom: "25px"
};

const registeredCardStyle = {
    textAlign: "left",
    backgroundColor: "#f7f9fb",
    border: "1px solid #e0e7ec",
    borderRadius: "9px",
    padding: "20px"
};

const countBadgeStyle = {
    backgroundColor: "#eef5fa",
    color: "#23415f",
    border: "1px solid #cbdce8",
    borderRadius: "20px",
    padding: "6px 11px",
    fontSize: "10px",
    fontWeight: "700",
    whiteSpace: "nowrap"
};

const registeredSubjectStyle = {
    display: "flex",
    alignItems: "flex-start",
    gap: "10px",
    backgroundColor: "#ffffff",
    border: "1px solid #dbe3e8",
    borderRadius: "7px",
    padding: "12px"
};

const registeredCheckStyle = {
    width: "22px",
    height: "22px",
    minWidth: "22px",
    borderRadius: "50%",
    backgroundColor: "#eaf7ee",
    color: "#18794e",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "11px",
    fontWeight: "700"
};

const registeredCodeStyle = {
    fontSize: "11px",
    fontWeight: "700",
    color: "#55718b"
};

const registeredNameStyle = {
    marginTop: "4px",
    color: "#263f55",
    fontSize: "12px",
    fontWeight: "600",
    lineHeight: 1.4
};

const registeredCreditsStyle = {
    marginTop: "6px",
    color: "#8797a5",
    fontSize: "10px"
};

// ================================================================
// LOADING
// ================================================================

const loadingCardStyle = {
    width: "100%",
    maxWidth: "600px",
    margin: "100px auto",
    backgroundColor: "#ffffff",
    border: "1px solid #d5e0e8",
    borderRadius: "10px",
    padding: "35px",
    textAlign: "center",
    boxShadow:
        "0 3px 12px rgba(0,0,0,0.05)",
    boxSizing: "border-box"
};

const loadingIconStyle = {
    width: "50px",
    height: "50px",
    margin: "0 auto 15px",
    borderRadius: "50%",
    backgroundColor: "#eef5fa",
    color: "#23415f",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontSize: "23px",
    fontWeight: "700"
};

const loadingTitleStyle = {
    margin: "0 0 7px",
    color: "#23415f",
    fontSize: "20px"
};

// ================================================================
// EMPTY STATE
// ================================================================

const emptyStateStyle = {
    border: "1px dashed #cbd5df",
    borderRadius: "9px",
    padding: "35px 20px",
    textAlign: "center",
    backgroundColor: "#f9fafb"
};

const emptyStateIconStyle = {
    fontSize: "30px",
    marginBottom: "8px"
};

const emptyStateTitleStyle = {
    margin: "0 0 6px",
    color: "#23415f",
    fontSize: "16px"
};

const emptyStateTextStyle = {
    maxWidth: "500px",
    margin: "0 auto",
    color: "#7b8b99",
    fontSize: "12px",
    lineHeight: 1.6
};

// ================================================================
// RESPONSIVE STYLES
// ================================================================

const responsiveStyles = `
    @media (max-width: 900px) {
        .smart-registration-responsive {
            width: 100%;
        }
    }

    @media (max-width: 760px) {
        body {
            overflow-x: hidden;
        }
    }
`;

export default StudentSubjectRegistration;