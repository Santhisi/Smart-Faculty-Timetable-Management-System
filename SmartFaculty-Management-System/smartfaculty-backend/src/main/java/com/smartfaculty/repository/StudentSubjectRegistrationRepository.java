package com.smartfaculty.repository;

import com.smartfaculty.entity.StudentSubjectRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentSubjectRegistrationRepository
        extends JpaRepository<StudentSubjectRegistration, Long> {

    // =====================================================
    // GET ALL REGISTRATIONS OF A STUDENT
    // =====================================================

    List<StudentSubjectRegistration> findByStudentStudentId(
            Long studentId
    );

    boolean existsByStudentStudentId(Long studentId);


    // Delete all registrations belonging to a student
    void deleteByStudentStudentId(Long studentId);


    // =====================================================
    // GET ALL STUDENTS REGISTERED FOR A SUBJECT
    // =====================================================

    List<StudentSubjectRegistration> findBySubjectSubjectId(
            Long subjectId
    );


    // =====================================================
    // CHECK DUPLICATE REGISTRATION
    // =====================================================

    boolean existsByStudentStudentIdAndSubjectSubjectIdAndAcademicYearAndSemester(
            Long studentId,
            Long subjectId,
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // GET STUDENT REGISTRATIONS BY YEAR + SEMESTER
    // =====================================================

    List<StudentSubjectRegistration>
    findByStudentStudentIdAndAcademicYearAndSemester(
            Long studentId,
            Integer academicYear,
            Integer semester
    );
    boolean existsBySubjectSubjectId(Long subjectId);
}