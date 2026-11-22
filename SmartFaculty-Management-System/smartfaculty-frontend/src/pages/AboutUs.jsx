import { useNavigate } from "react-router-dom";
import universityLogo from "../assets/university-logo.png";
import facultyPhoto from "../assets/fac-image.jpg";

function AboutUs() {
    const navigate = useNavigate();

    return (
        <div style={pageStyle}>
            <header style={headerStyle}>
                <div style={headerInnerStyle}>
                    <button
                        type="button"
                        onClick={() => navigate("/")}
                        style={brandButtonStyle}
                    >
                        <img
                            src={universityLogo}
                            alt="Sabaragamuwa University of Sri Lanka"
                            style={logoStyle}
                        />

                        <div>
                            <div style={facultyNameStyle}>
                                Faculty of Social Sciences
                                <br />
                                and Languages
                            </div>

                            <div style={universityNameStyle}>
                                Sabaragamuwa University of Sri Lanka
                            </div>
                        </div>
                    </button>

                    <nav style={navStyle}>
                        <button
                            type="button"
                            onClick={() => navigate("/")}
                            style={navButtonStyle}
                        >
                            Home
                        </button>

                        <button
                            type="button"
                            style={activeNavButtonStyle}
                        >
                            About Us
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate("/contact")}
                            style={navButtonStyle}
                        >
                            Contact Us
                        </button>

                        <button
                            type="button"
                            onClick={() => navigate("/login")}
                            style={loginButtonStyle}
                        >
                            Login
                        </button>
                    </nav>
                </div>
            </header>

            <main>
                <section style={heroSectionStyle}>
                    <div style={heroInnerStyle}>
                        <div>
                            

                            <h1 style={titleStyle}>
                                Faculty of Social Sciences
                                <br />
                                and Languages
                            </h1>

                            <h2 style={universityTitleStyle}>
                                Sabaragamuwa University of Sri Lanka
                            </h2>
                        </div>

                        <img
                            src={facultyPhoto}
                            alt="Faculty of Social Sciences and Languages"
                            style={heroImageStyle}
                        />
                    </div>
                </section>

                <section style={contentSectionStyle}>
                    <div style={contentStyle}>
                        <h2 style={sectionTitleStyle}>
                            Smart Faculty Timetable Management System
                        </h2>

                        <p style={paragraphStyle}>
                            Smart Faculty is a web-based timetable
                            management platform developed for the
                            Faculty of Social Sciences and Languages
                            at Sabaragamuwa University of Sri Lanka.
                        </p>

                        <p style={paragraphStyle}>
                            The system brings timetable information,
                            lecturer allocations, student
                            registrations and timetable changes into
                            one organized platform.
                        </p>

                        <div style={cardsGridStyle}>
                            <InfoCard
                                title="Students"
                                text="View the timetable relevant to your academic year and semester."
                            />

                            <InfoCard
                                title="Lecturers"
                                text="View teaching schedules and allocated classes."
                            />

                            <InfoCard
                                title="HOD"
                                text="Manage lecturer subject allocations within the department."
                            />

                            <InfoCard
                                title="Administration"
                                text="Manage faculty resources and timetables efficiently."
                            />
                        </div>
                    </div>
                </section>
            </main>

            <footer style={footerStyle}>
                Smart Faculty Timetable Management System
                <br />
                Faculty of Social Sciences and Languages
            </footer>
        </div>
    );
}

function InfoCard({ title, text }) {
    return (
        <div style={cardStyle}>
            <h3 style={cardTitleStyle}>
                {title}
            </h3>

            <p style={cardTextStyle}>
                {text}
            </p>
        </div>
    );
}

const pageStyle = {
    minHeight: "100vh",
    backgroundColor: "#ffffff",
    fontFamily: "Arial, sans-serif",
    color: "#23415f"
};

const headerStyle = {
    backgroundColor: "#ffffff",
    borderBottom: "1px solid #dce5ed",
    boxShadow: "0 2px 8px rgba(35,65,95,0.08)"
};

const headerInnerStyle = {
    maxWidth: "1350px",
    margin: "0 auto",
    padding: "12px 35px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    gap: "20px",
    boxSizing: "border-box"
};

const brandButtonStyle = {
    border: "none",
    background: "transparent",
    padding: 0,
    display: "flex",
    alignItems: "center",
    gap: "13px",
    cursor: "pointer",
    textAlign: "left"
};

const logoStyle = {
    width: "68px",
    height: "68px",
    objectFit: "contain"
};

const facultyNameStyle = {
    color: "#23415f",
    fontSize: "22px",
    fontWeight: "800",
    lineHeight: "1.08"
};

const universityNameStyle = {
    color: "#55728d",
    fontSize: "13px",
    fontWeight: "600",
    marginTop: "4px"
};

const navStyle = {
    display: "flex",
    alignItems: "center",
    gap: "5px",
    flexWrap: "wrap"
};

const navButtonStyle = {
    border: "none",
    background: "transparent",
    color: "#23415f",
    padding: "10px 13px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "600"
};

const activeNavButtonStyle = {
    ...navButtonStyle,
    color: "#2563eb"
};

const loginButtonStyle = {
    border: "none",
    borderRadius: "7px",
    backgroundColor: "#2563eb",
    color: "#ffffff",
    padding: "10px 20px",
    cursor: "pointer",
    fontSize: "14px",
    fontWeight: "700"
};

const heroSectionStyle = {
    backgroundColor: "#eef6ff",
    padding: "50px 30px"
};

const heroInnerStyle = {
    maxWidth: "1200px",
    margin: "0 auto",
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "35px",
    alignItems: "center"
};

const labelStyle = {
    color: "#2563eb",
    fontSize: "12px",
    fontWeight: "800",
    letterSpacing: "1.2px",
    marginBottom: "8px"
};

const titleStyle = {
    margin: 0,
    color: "#123f70",
    fontSize: "38px",
    lineHeight: "1.1"
};

const universityTitleStyle = {
    color: "#55728d",
    fontSize: "19px",
    marginTop: "12px"
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
const heroImageWrapperStyle = {
    width: "46%",
    minHeight: "420px",
    backgroundColor: "#eef6ff"
};

const contentSectionStyle = {
    padding: "55px 30px",
    backgroundColor: "#ffffff"
};

const contentStyle = {
    maxWidth: "1050px",
    margin: "0 auto",
    textAlign: "center"
};

const sectionTitleStyle = {
    color: "#123f70",
    fontSize: "28px",
    margin: "0 0 15px"
};

const paragraphStyle = {
    maxWidth: "900px",
    margin: "0 auto 12px",
    color: "#5d748a",
    fontSize: "15px",
    lineHeight: "1.65"
};

const cardsGridStyle = {
    marginTop: "30px",
    display: "grid",
    gridTemplateColumns: "repeat(4, 1fr)",
    gap: "15px"
};

const cardStyle = {
    backgroundColor: "#f5f9fd",
    border: "1px solid #dce8f2",
    borderRadius: "9px",
    padding: "20px",
    textAlign: "left"
};

const cardTitleStyle = {
    margin: "0 0 8px",
    color: "#124a80",
    fontSize: "16px"
};

const cardTextStyle = {
    margin: 0,
    color: "#61778b",
    fontSize: "13px",
    lineHeight: "1.5"
};

const footerStyle = {
    backgroundColor: "#23415f",
    color: "#ffffff",
    textAlign: "center",
    padding: "25px",
    fontSize: "13px",
    lineHeight: "1.6"
};

export default AboutUs;