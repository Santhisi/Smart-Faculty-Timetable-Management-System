import { useNavigate } from "react-router-dom";
import universityLogo from "../assets/university-logo.png";

function ContactUs() {
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
                            onClick={() => navigate("/about")}
                            style={navButtonStyle}
                        >
                            About Us
                        </button>

                        <button
                            type="button"
                            style={activeNavButtonStyle}
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
                <section style={pageHeaderStyle}>
                    

                    <h1 style={titleStyle}>
                        Faculty of Social Sciences and Languages
                    </h1>

                    <p style={subtitleStyle}>
                        Sabaragamuwa University of Sri Lanka
                    </p>
                </section>

                <section style={contentSectionStyle}>
                    <div style={contentGridStyle}>
                        <div>
                            <h2 style={sectionTitleStyle}>
                                Contact Information
                            </h2>

                            <ContactGroup
                                title="Telephone"
                                lines={[
                                    "+94 45-2280087 (General)",
                                    "+94 45-2280021 (Dean/ Faculty of Social Sciences and Languages)",
                                    "+94 45-2280048 (Head/ Department of Economics and Statistics)",
                                    "+94 45-2280310 (Head/ Department of English Language Teaching)",
                                    "+94 45-2280017 (Head/ Department of Languages)",
                                    "+94 45-2280022 (Head/ Department of Social Sciences)",
                                    "+94 45-3453186 (Assistant Registrar/ Faculty of Social Sciences and Languages)"
                                ]}
                            />

                            <ContactGroup
                                title="Fax"
                                lines={[
                                    "+94 45-2280034 (Faculty)"
                                ]}
                            />

                            <ContactGroup
                                title="Email"
                                lines={[
                                    "dean@ssl.sab.ac.lk",
                                    "deanoffice@ssl.sab.ac.lk"
                                ]}
                            />

                            <ContactGroup
                                title="Address"
                                lines={[
                                    "Faculty of Social Sciences and Languages,",
                                    "Sabaragamuwa University of Sri Lanka,",
                                    "P.O. Box 02, Belihuloya 70140,",
                                    "Sri Lanka."
                                ]}
                            />
                        </div>

                        <div>
                            <h2 style={sectionTitleStyle}>
                                Find Us
                            </h2>

                            <div style={mapBoxStyle}>
                                <iframe
                                    title="Faculty of Social Sciences and Languages location"
                                    src="https://www.google.com/maps?q=Faculty%20of%20Social%20Sciences%20and%20Languages%20Sabaragamuwa%20University%20of%20Sri%20Lanka&output=embed"
                                    style={mapStyle}
                                    loading="lazy"
                                    referrerPolicy="no-referrer-when-downgrade"
                                />
                            </div>

                            <a
                                href="https://www.google.com/maps/search/?api=1&query=Faculty+of+Social+Sciences+and+Languages+Sabaragamuwa+University+of+Sri+Lanka"
                                target="_blank"
                                rel="noreferrer"
                                style={mapLinkStyle}
                            >
                                Open location in Google Maps
                            </a>
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

function ContactGroup({ title, lines }) {
    return (
        <div style={groupStyle}>
            <h3 style={groupTitleStyle}>
                {title}
            </h3>

            <div style={linesStyle}>
                {lines.map((line, index) => (
                    <div key={`${title}-${index}`}>
                        {line}
                    </div>
                ))}
            </div>
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

const pageHeaderStyle = {
    backgroundColor: "#eef6ff",
    padding: "55px 30px",
    textAlign: "center"
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
    fontSize: "35px"
};

const subtitleStyle = {
    margin: "10px 0 0",
    color: "#55728d",
    fontSize: "17px"
};

const contentSectionStyle = {
    padding: "45px 30px"
};

const contentGridStyle = {
    maxWidth: "1150px",
    margin: "0 auto",
    display: "grid",
    gridTemplateColumns: "1fr 1fr",
    gap: "45px"
};

const sectionTitleStyle = {
    margin: "0 0 18px",
    color: "#123f70",
    fontSize: "24px"
};

const groupStyle = {
    marginBottom: "24px"
};

const groupTitleStyle = {
    margin: "0 0 7px",
    color: "#23415f",
    fontSize: "17px"
};

const linesStyle = {
    color: "#5d748a",
    fontSize: "14px",
    lineHeight: "1.7"
};

const mapBoxStyle = {
    width: "100%",
    height: "390px",
    border: "1px solid #d6e4ef",
    borderRadius: "10px",
    overflow: "hidden",
    backgroundColor: "#f5f9fd"
};

const mapStyle = {
    width: "100%",
    height: "100%",
    border: 0
};

const mapLinkStyle = {
    display: "inline-block",
    marginTop: "12px",
    color: "#2563eb",
    fontSize: "14px",
    fontWeight: "700",
    textDecoration: "none"
};

const footerStyle = {
    backgroundColor: "#23415f",
    color: "#ffffff",
    textAlign: "center",
    padding: "25px",
    fontSize: "13px",
    lineHeight: "1.6"
};

export default ContactUs;
