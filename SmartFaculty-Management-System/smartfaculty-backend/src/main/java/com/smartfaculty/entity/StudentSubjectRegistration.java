package com.smartfaculty.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "student_subject_registrations",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "student_id",
                                "subject_id",
                                "academic_year",
                                "semester"
                        }
                )
        }
)
public class StudentSubjectRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long registrationId;

    @ManyToOne
    @JoinColumn(
            name = "student_id",
            nullable = false
    )
    private Student student;

    @ManyToOne
    @JoinColumn(
            name = "subject_id",
            nullable = false
    )
    private Subject subject;

    @Column(nullable = false)
    private Integer academicYear;

    @Column(nullable = false)
    private Integer semester;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public StudentSubjectRegistration() {
    }

    // =====================================================
    // GETTERS
    // =====================================================

    public Long getRegistrationId() {
        return registrationId;
    }

    public Student getStudent() {
        return student;
    }

    public Subject getSubject() {
        return subject;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public Integer getSemester() {
        return semester;
    }

    // =====================================================
    // SETTERS
    // =====================================================

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public void setSemester(Integer semester) {
        this.semester = semester;
    }
}
