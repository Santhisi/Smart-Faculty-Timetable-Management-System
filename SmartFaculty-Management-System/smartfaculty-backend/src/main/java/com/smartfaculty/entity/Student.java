package com.smartfaculty.entity;

import jakarta.persistence.*;
import com.smartfaculty.enums.DegreeType;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private String studentName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Integer yearLevel;

    @Column(nullable = false)
    private Integer semester;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_type")
    private DegreeType degreeType;

    /*
     * Indicates whether the student has completed the
     * first-time subject registration process.
     *
     * false / null = registration not completed
     * true = registration completed
     */
    @Column(name = "registration_completed")
    private Boolean registrationCompleted = false;

    @ManyToOne
    @JoinColumn(name = "major_specialization_id")
    private Specialization majorSpecialization;

    @ManyToOne
    @JoinColumn(name = "minor_specialization_id")
    private Specialization minorSpecialization;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    public Student() {
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

    public Specialization getMajorSpecialization() {
        return majorSpecialization;
    }

    public void setMajorSpecialization(Specialization majorSpecialization) {
        this.majorSpecialization = majorSpecialization;
    }

    public Specialization getMinorSpecialization() {
        return minorSpecialization;
    }

    public void setMinorSpecialization(Specialization minorSpecialization) {
        this.minorSpecialization = minorSpecialization;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}