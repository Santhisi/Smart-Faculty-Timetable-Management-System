package com.smartfaculty.dto;

import com.smartfaculty.enums.DegreeType;

public class StudentRegistrationRequest {

    private String registrationNumber;
    private String studentName;
    private String email;
    private String password;

    private Integer yearLevel;
    private Integer semester;

    private DegreeType degreeType;

    private Long majorSpecializationId;
    private Long minorSpecializationId;

    public StudentRegistrationRequest() {
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(Integer yearLevel) {
        this.yearLevel = yearLevel;
    }

    public Integer getSemester() {
        return semester;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }

    public DegreeType getDegreeType() {
        return degreeType;
    }

    public void setDegreeType(DegreeType degreeType) {
        this.degreeType = degreeType;
    }

    public Long getMajorSpecializationId() {
        return majorSpecializationId;
    }

    public void setMajorSpecializationId(Long majorSpecializationId) {
        this.majorSpecializationId = majorSpecializationId;
    }

    public Long getMinorSpecializationId() {
        return minorSpecializationId;
    }

    public void setMinorSpecializationId(Long minorSpecializationId) {
        this.minorSpecializationId = minorSpecializationId;
    }
}