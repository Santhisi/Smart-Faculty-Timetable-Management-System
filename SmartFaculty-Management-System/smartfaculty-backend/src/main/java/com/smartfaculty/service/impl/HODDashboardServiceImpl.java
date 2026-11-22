package com.smartfaculty.service.impl;

import com.smartfaculty.dto.HODDashboardResponse;
import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerAllocation;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.repository.LecturerAllocationRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.service.HODDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HODDashboardServiceImpl
        implements HODDashboardService {


    @Autowired
    private LecturerRepository lecturerRepository;


    @Autowired
    private SubjectRepository subjectRepository;


    @Autowired
    private LecturerAllocationRepository
            lecturerAllocationRepository;

    // =====================================================
    // VALIDATE AUTHENTICATED HOD OWNERSHIP
    // =====================================================

    private void validateAuthenticatedHODOwnership(
            String requestedUsername) {

        if (requestedUsername == null ||
                requestedUsername.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

        String authenticatedUsername =
                authentication.getName();

        if (authenticatedUsername == null ||
                authenticatedUsername.trim().isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }

        if (!authenticatedUsername
                .trim()
                .equalsIgnoreCase(
                        requestedUsername.trim()
                )) {

            throw new IllegalStateException(
                    "You are not authorized to access another HOD's data."
            );
        }
    }


    // =====================================================
    // GET HOD'S LECTURER RECORD
    // =====================================================

    private Lecturer getHODLecturer(
            String username) {

        validateAuthenticatedHODOwnership(username);

        if (username == null ||
                username.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Username is required."
            );
        }


        return lecturerRepository
                .findByUser_Username(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No lecturer/HOD record found "
                                        + "for username: "
                                        + username
                        )
                );
    }


    // =====================================================
    // GET HOD'S DEPARTMENT
    // =====================================================

    private Department getHODDepartment(
            String username) {

        Lecturer hod =
                getHODLecturer(username);


        if (hod.getDepartment() == null ||
                hod.getDepartment()
                        .getDepartmentId() == null) {

            throw new IllegalStateException(
                    "The HOD is not assigned to a department."
            );
        }


        return hod.getDepartment();
    }


    // =====================================================
    // GET COMPLETE HOD DASHBOARD
    // =====================================================

    @Override
    public HODDashboardResponse getDashboard(
            String username) {

        Department department =
                getHODDepartment(username);


        Long departmentId =
                department.getDepartmentId();


        List<Lecturer> lecturers =
                lecturerRepository
                        .findByDepartment_DepartmentId(
                                departmentId
                        );


        List<Subject> subjects =
                subjectRepository
                        .findByDepartment_DepartmentId(
                                departmentId
                        );


        List<LecturerAllocation> allocations =
                lecturerAllocationRepository
                        .findByLecturer_Department_DepartmentId(
                                departmentId
                        );


        return new HODDashboardResponse(
                department,
                lecturers,
                subjects,
                allocations
        );
    }


    // =====================================================
    // GET DEPARTMENT LECTURERS
    // =====================================================

    @Override
    public List<Lecturer>
    getDepartmentLecturers(
            String username) {

        Department department =
                getHODDepartment(username);


        return lecturerRepository
                .findByDepartment_DepartmentId(
                        department.getDepartmentId()
                );
    }


    // =====================================================
    // GET DEPARTMENT SUBJECTS
    // =====================================================

    @Override
    public List<Subject>
    getDepartmentSubjects(
            String username) {

        Department department =
                getHODDepartment(username);


        return subjectRepository
                .findByDepartment_DepartmentId(
                        department.getDepartmentId()
                );
    }


    // =====================================================
    // GET SUBJECTS BY YEAR + SEMESTER
    // =====================================================

    @Override
    public List<Subject>
    getDepartmentSubjectsByYearAndSemester(
            String username,
            Integer academicYear,
            Integer semester) {

        if (academicYear == null) {

            throw new IllegalArgumentException(
                    "Academic year is required."
            );
        }


        if (semester == null) {

            throw new IllegalArgumentException(
                    "Semester is required."
            );
        }


        Department department =
                getHODDepartment(username);


        return subjectRepository
                .findByDepartment_DepartmentIdAndAcademicYearAndSemester(
                        department.getDepartmentId(),
                        academicYear,
                        semester
                );
    }
}