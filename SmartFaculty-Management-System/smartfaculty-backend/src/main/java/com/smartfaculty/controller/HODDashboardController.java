package com.smartfaculty.controller;

import com.smartfaculty.dto.HODDashboardResponse;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.service.HODDashboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hod")

public class HODDashboardController {


    @Autowired
    private HODDashboardService hodDashboardService;


    // =====================================================
    // GET AUTHENTICATED HOD USERNAME
    // =====================================================

    private String getAuthenticatedUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return null;
        }


        return authentication.getName();
    }


    // =====================================================
    // CHECK HOD OWNERSHIP
    // =====================================================

    private boolean isHODOwner(
            String requestedUsername) {

        String authenticatedUsername =
                getAuthenticatedUsername();


        if (authenticatedUsername == null ||
                requestedUsername == null) {

            return false;
        }


        return authenticatedUsername
                .equalsIgnoreCase(
                        requestedUsername
                );
    }


    // =====================================================
    // GET COMPLETE HOD DASHBOARD
    // =====================================================

    @PreAuthorize("hasRole('HOD')")
    @GetMapping("/dashboard/{username}")
    public ResponseEntity<?> getDashboard(
            @PathVariable String username) {


        // -------------------------------------------------
        // CHECK USERNAME OWNERSHIP
        // -------------------------------------------------

        if (!isHODOwner(username)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access "
                                    + "another HOD's dashboard."
                    );
        }


        return ResponseEntity.ok(
                hodDashboardService
                        .getDashboard(username)
        );
    }


    // =====================================================
    // GET HOD DEPARTMENT LECTURERS
    // =====================================================

    @PreAuthorize("hasRole('HOD')")
    @GetMapping(
            "/dashboard/{username}/lecturers"
    )
    public ResponseEntity<?> getDepartmentLecturers(
            @PathVariable String username) {


        // -------------------------------------------------
        // CHECK USERNAME OWNERSHIP
        // -------------------------------------------------

        if (!isHODOwner(username)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access "
                                    + "another HOD's department."
                    );
        }


        return ResponseEntity.ok(
                hodDashboardService
                        .getDepartmentLecturers(
                                username
                        )
        );
    }


    // =====================================================
    // GET HOD DEPARTMENT SUBJECTS
    // =====================================================

    @PreAuthorize("hasRole('HOD')")
    @GetMapping(
            "/dashboard/{username}/subjects"
    )
    public ResponseEntity<?> getDepartmentSubjects(
            @PathVariable String username) {


        // -------------------------------------------------
        // CHECK USERNAME OWNERSHIP
        // -------------------------------------------------

        if (!isHODOwner(username)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access "
                                    + "another HOD's department."
                    );
        }


        return ResponseEntity.ok(
                hodDashboardService
                        .getDepartmentSubjects(
                                username
                        )
        );
    }


    // =====================================================
    // GET SUBJECTS BY YEAR + SEMESTER
    // =====================================================

    @PreAuthorize("hasRole('HOD')")
    @GetMapping(
            "/dashboard/{username}/subjects/filter"
    )
    public ResponseEntity<?> getDepartmentSubjectsByYearAndSemester(

            @PathVariable String username,

            @RequestParam Integer academicYear,

            @RequestParam Integer semester) {


        // -------------------------------------------------
        // CHECK USERNAME OWNERSHIP
        // -------------------------------------------------

        if (!isHODOwner(username)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access "
                                    + "another HOD's department."
                    );
        }


        return ResponseEntity.ok(
                hodDashboardService
                        .getDepartmentSubjectsByYearAndSemester(
                                username,
                                academicYear,
                                semester
                        )
        );
    }
}

