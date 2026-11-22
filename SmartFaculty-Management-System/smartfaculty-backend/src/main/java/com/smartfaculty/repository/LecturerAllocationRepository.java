package com.smartfaculty.repository;

import com.smartfaculty.entity.LecturerAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturerAllocationRepository
        extends JpaRepository<LecturerAllocation, Long> {

    // =====================================================
    // GET ALL ALLOCATIONS FOR A LECTURER
    // =====================================================

    List<LecturerAllocation> findByLecturerLecturerId(
            Long lecturerId
    );


    // =====================================================
    // UNDERSCORE VERSION
    // =====================================================

    List<LecturerAllocation> findByLecturer_LecturerId(
            Long lecturerId
    );
    boolean existsByLecturerLecturerId(Long lecturerId);


    // =====================================================
    // GET ALLOCATIONS BY SUBJECT
    // =====================================================

    List<LecturerAllocation> findBySubjectSubjectId(
            Long subjectId
    );


    // =====================================================
    // UNDERSCORE VERSION
    // =====================================================

    List<LecturerAllocation> findBySubject_SubjectId(
            Long subjectId
    );


    // =====================================================
    // GET BY LECTURER + SUBJECT
    // =====================================================

    List<LecturerAllocation>
    findByLecturerLecturerIdAndSubjectSubjectId(
            Long lecturerId,
            Long subjectId
    );


    // =====================================================
    // GET BY LECTURER + SUBJECT + YEAR + SEMESTER
    // =====================================================
    //
    // IMPORTANT:
    // This is the method used by TimetableEntryServiceImpl.
    //
    // =====================================================

    List<LecturerAllocation>
    findByLecturerLecturerIdAndSubjectSubjectIdAndAcademicYearAndSemester(
            Long lecturerId,
            Long subjectId,
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // UNDERSCORE VERSION
    // =====================================================

    List<LecturerAllocation>
    findByLecturer_LecturerIdAndSubject_SubjectIdAndAcademicYearAndSemester(
            Long lecturerId,
            Long subjectId,
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // CHECK DUPLICATE ALLOCATION
    // =====================================================

    boolean
    existsByLecturerLecturerIdAndSubjectSubjectIdAndAcademicYearAndSemester(
            Long lecturerId,
            Long subjectId,
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // UNDERSCORE VERSION
    // =====================================================

    boolean
    existsByLecturer_LecturerIdAndSubject_SubjectIdAndAcademicYearAndSemester(
            Long lecturerId,
            Long subjectId,
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // CHECK DUPLICATE DURING UPDATE
    // =====================================================

    boolean
    existsByLecturerLecturerIdAndSubjectSubjectIdAndAcademicYearAndSemesterAndAllocationIdNot(
            Long lecturerId,
            Long subjectId,
            Integer academicYear,
            Integer semester,
            Long allocationId
    );


    // =====================================================
    // UNDERSCORE VERSION
    // =====================================================

    boolean
    existsByLecturer_LecturerIdAndSubject_SubjectIdAndAcademicYearAndSemesterAndAllocationIdNot(
            Long lecturerId,
            Long subjectId,
            Integer academicYear,
            Integer semester,
            Long allocationId
    );


    // =====================================================
    // GET ALLOCATIONS BY LECTURER DEPARTMENT
    // =====================================================

    List<LecturerAllocation>
    findByLecturer_Department_DepartmentId(
            Long departmentId
    );


    // =====================================================
    // NON-UNDERSCORE VERSION
    // =====================================================

    List<LecturerAllocation>
    findByLecturerDepartmentDepartmentId(
            Long departmentId
    );

    boolean existsBySubjectSubjectId(Long subjectId);
}