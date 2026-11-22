package com.smartfaculty.dto;

import com.smartfaculty.enums.DegreeType;

public class StudentResponse {

    private Long studentId;
    private String registrationNumber;
    private String studentName;
    private String email;
    private Integer yearLevel;
    private Integer semester;

    private DegreeType degreeType;

    /*
     * Indicates whether the student has completed
     * the first-time subject registration process.
     */
    private Boolean registrationCompleted;

    private Long majorSpecializationId;
    private String majorSpecializationCode;
    private String majorSpecializationName;

    private Long minorSpecializationId;
    private String minorSpecializationCode;
    private String minorSpecializationName;

    public StudentResponse() {
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
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

    public Boolean getRegistrationCompleted() {
        return registrationCompleted;
    }

    public void setRegistrationCompleted(Boolean registrationCompleted) {
        this.registrationCompleted = registrationCompleted;
    }

    public Long getMajorSpecializationId() {
        return majorSpecializationId;
    }

    public void setMajorSpecializationId(Long majorSpecializationId) {
        this.majorSpecializationId = majorSpecializationId;
    }

    public String getMajorSpecializationCode() {
        return majorSpecializationCode;
    }

    public void setMajorSpecializationCode(String majorSpecializationCode) {
        this.majorSpecializationCode = majorSpecializationCode;
    }

    public String getMajorSpecializationName() {
        return majorSpecializationName;
    }

    public void setMajorSpecializationName(String majorSpecializationName) {
        this.majorSpecializationName = majorSpecializationName;
    }

    public Long getMinorSpecializationId() {
        return minorSpecializationId;
    }

    public void setMinorSpecializationId(Long minorSpecializationId) {
        this.minorSpecializationId = minorSpecializationId;
    }

    public String getMinorSpecializationCode() {
        return minorSpecializationCode;
    }

    public void setMinorSpecializationCode(String minorSpecializationCode) {
        this.minorSpecializationCode = minorSpecializationCode;
    }

    public String getMinorSpecializationName() {
        return minorSpecializationName;
    }

    public void setMinorSpecializationName(String minorSpecializationName) {
        this.minorSpecializationName = minorSpecializationName;
    }
}