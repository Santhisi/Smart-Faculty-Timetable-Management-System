package com.smartfaculty.repository;

import com.smartfaculty.entity.Specialization;
import com.smartfaculty.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    // =====================================================
    // COMPULSORY SUBJECTS
    // =====================================================

    List<Subject> findByCompulsoryTrueAndAcademicYearAndSemester(
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // SUBJECTS BY SPECIALIZATION
    // =====================================================

    List<Subject> findBySpecializationAndAcademicYearAndSemester(
            Specialization specialization,
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // SUBJECTS BY MULTIPLE SPECIALIZATIONS
    // Used by Student Available Subjects API
    // =====================================================

    List<Subject> findByAcademicYearAndSemesterAndSpecializationIn(
            Integer academicYear,
            Integer semester,
            List<Specialization> specializations
    );


    // =====================================================
    // SUBJECTS BY DEPARTMENT
    // Used by HOD Dashboard
    // =====================================================

    List<Subject> findByDepartment_DepartmentId(
            Long departmentId
    );


    // =====================================================
    // SUBJECTS BY DEPARTMENT + YEAR + SEMESTER
    // =====================================================

    List<Subject> findByDepartment_DepartmentIdAndAcademicYearAndSemester(
            Long departmentId,
            Integer academicYear,
            Integer semester
    );


    // =====================================================
    // ALL SUBJECTS BY YEAR + SEMESTER
    //
    // Used mainly for Year 1 students because Year 1
    // students do not have a major/minor specialization.
    // =====================================================

    List<Subject> findByAcademicYearAndSemester(
            Integer academicYear,
            Integer semester
    );
}