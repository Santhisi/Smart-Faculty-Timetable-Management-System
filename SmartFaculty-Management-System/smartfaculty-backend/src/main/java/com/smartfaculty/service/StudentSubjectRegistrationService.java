package com.smartfaculty.service;

import com.smartfaculty.entity.StudentSubjectRegistration;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.enums.DegreeType;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface StudentSubjectRegistrationService {

    // =====================================================
    // CREATE REGISTRATION
    // =====================================================

    StudentSubjectRegistration saveRegistration(
            StudentSubjectRegistration registration,
            Authentication authentication
    );

    // =====================================================
    // UPDATE REGISTRATION
    // =====================================================

    StudentSubjectRegistration updateRegistration(
            Long id,
            StudentSubjectRegistration registration,
            Authentication authentication
    );

    // =====================================================
    // GET ALL REGISTRATIONS
    // =====================================================

    List<StudentSubjectRegistration> getAllRegistrations(
            Authentication authentication
    );

    // =====================================================
    // GET REGISTRATION BY ID
    // =====================================================

    StudentSubjectRegistration getRegistrationById(
            Long id,
            Authentication authentication
    );

    // =====================================================
    // GET REGISTRATIONS BY STUDENT
    // =====================================================

    List<StudentSubjectRegistration> getRegistrationsByStudentId(
            Long studentId,
            Authentication authentication
    );

    // =====================================================
    // GET REGISTRATIONS BY STUDENT + YEAR + SEMESTER
    // =====================================================

    List<StudentSubjectRegistration> getRegistrationsByStudentAndYearAndSemester(
            Long studentId,
            Integer academicYear,
            Integer semester,
            Authentication authentication
    );

    // =====================================================
    // GET REGISTRATIONS BY SUBJECT
    // =====================================================

    List<StudentSubjectRegistration> getRegistrationsBySubjectId(
            Long subjectId,
            Authentication authentication
    );

    // =====================================================
    // GET AVAILABLE SUBJECTS FOR STUDENT
    // =====================================================

    List<Subject> getAvailableSubjectsForStudent(
            Long studentId,
            Authentication authentication
    );

    List<StudentSubjectRegistration> completeRegistration(
            Long studentId,
            Long majorSpecializationId,
            Long minorSpecializationId,
            DegreeType degreeType,
            List<Long> preferredSubjectIds,
            Authentication authentication
    );

    // =====================================================
    // GET REGISTRATION STATUS
    // =====================================================

    Object getRegistrationStatus(
            Long studentId,
            Authentication authentication
    );

    // =====================================================
    // DELETE REGISTRATION
    // =====================================================

    void deleteRegistration(
            Long id,
            Authentication authentication
    );
}
