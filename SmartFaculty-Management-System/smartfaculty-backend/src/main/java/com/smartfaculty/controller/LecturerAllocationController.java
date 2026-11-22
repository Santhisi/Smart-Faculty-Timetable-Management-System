package com.smartfaculty.controller;

import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerAllocation;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.repository.LecturerAllocationRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.service.LecturerAllocationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer-allocations")

public class LecturerAllocationController {


    @Autowired
    private LecturerAllocationRepository
            lecturerAllocationRepository;


    @Autowired
    private LecturerAllocationService
            lecturerAllocationService;


    @Autowired
    private LecturerRepository lecturerRepository;


    @Autowired
    private SubjectRepository subjectRepository;


    // =========================================================
    // GET AUTHENTICATED LECTURER
    // =========================================================

    private Lecturer getLoggedInLecturer() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return null;
        }


        String username =
                authentication.getName();


        return lecturerRepository
                .findByUser_Username(username)
                .orElse(null);
    }


    // =========================================================
    // GET LOGGED-IN USER ROLE
    // =========================================================

    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        );
    }


    // =========================================================
    // CHECK HOD DEPARTMENT OWNERSHIP
    // =========================================================

    private boolean isHODDepartmentOwner(
            LecturerAllocation allocation) {

        Lecturer hod =
                getLoggedInLecturer();


        if (hod == null ||
                hod.getDepartment() == null ||
                hod.getDepartment()
                        .getDepartmentId() == null) {

            return false;
        }


        if (allocation == null ||
                allocation.getLecturer() == null ||
                allocation.getLecturer()
                        .getDepartment() == null ||
                allocation.getLecturer()
                        .getDepartment()
                        .getDepartmentId() == null) {

            return false;
        }


        Long hodDepartmentId =
                hod.getDepartment()
                        .getDepartmentId();


        Long allocationDepartmentId =
                allocation.getLecturer()
                        .getDepartment()
                        .getDepartmentId();


        return hodDepartmentId
                .equals(allocationDepartmentId);
    }


    // =========================================================
    // CHECK LECTURER + SUBJECT BELONG TO HOD DEPARTMENT
    // =========================================================

    private boolean isAllocationWithinHODDepartment(
            Lecturer lecturer,
            Subject subject) {

        Lecturer hod =
                getLoggedInLecturer();


        if (hod == null ||
                hod.getDepartment() == null ||
                hod.getDepartment()
                        .getDepartmentId() == null) {

            return false;
        }


        if (lecturer == null ||
                lecturer.getDepartment() == null ||
                lecturer.getDepartment()
                        .getDepartmentId() == null) {

            return false;
        }


        if (subject == null ||
                subject.getDepartment() == null ||
                subject.getDepartment()
                        .getDepartmentId() == null) {

            return false;
        }


        Long hodDepartmentId =
                hod.getDepartment()
                        .getDepartmentId();


        Long lecturerDepartmentId =
                lecturer.getDepartment()
                        .getDepartmentId();


        Long subjectDepartmentId =
                subject.getDepartment()
                        .getDepartmentId();


        return hodDepartmentId
                .equals(lecturerDepartmentId)
                &&
                hodDepartmentId
                        .equals(subjectDepartmentId);
    }


    // =========================================================
    // GET ALL ALLOCATIONS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping
    public ResponseEntity<?> getAllAllocations() {

        // -----------------------------------------------------
        // ADMIN CAN VIEW ALL ALLOCATIONS
        // -----------------------------------------------------

        if (isAdmin()) {

            return ResponseEntity.ok(
                    lecturerAllocationRepository
                            .findAll()
            );
        }


        // -----------------------------------------------------
        // HOD CAN VIEW ONLY OWN DEPARTMENT ALLOCATIONS
        // -----------------------------------------------------

        Lecturer hod =
                getLoggedInLecturer();

        if (hod == null ||
                hod.getDepartment() == null ||
                hod.getDepartment().getDepartmentId() == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "HOD is not assigned to a department."
                    );
        }


        Long departmentId =
                hod.getDepartment()
                        .getDepartmentId();


        return ResponseEntity.ok(
                lecturerAllocationRepository
                        .findByLecturer_Department_DepartmentId(
                                departmentId
                        )
        );
    }


    // =========================================================
    // GET ALLOCATION BY ID
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD','LECTURER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @PathVariable Long id) {


        LecturerAllocation allocation =
                lecturerAllocationRepository
                        .findById(id)
                        .orElse(null);


        if (allocation == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // -----------------------------------------------------
        // ADMIN CAN ACCESS ANY ALLOCATION
        // -----------------------------------------------------

        if (isAdmin()) {

            return ResponseEntity.ok(
                    allocation
            );
        }


        // -----------------------------------------------------
        // LECTURER CAN ACCESS ONLY OWN ALLOCATION
        // -----------------------------------------------------

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isLecturer =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_LECTURER")
                        );


        if (isLecturer) {

            Lecturer loggedInLecturer =
                    getLoggedInLecturer();


            if (loggedInLecturer == null ||
                    allocation.getLecturer() == null ||
                    !loggedInLecturer
                            .getLecturerId()
                            .equals(
                                    allocation
                                            .getLecturer()
                                            .getLecturerId()
                            )) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "You are not authorized to access "
                                        + "this lecturer allocation."
                        );
            }


            return ResponseEntity.ok(
                    allocation
            );
        }


        // -----------------------------------------------------
        // HOD CAN ACCESS ONLY OWN DEPARTMENT
        // -----------------------------------------------------

        if (!isHODDepartmentOwner(allocation)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access "
                                    + "another department's allocation."
                    );
        }


        return ResponseEntity.ok(
                allocation
        );
    }


    // =========================================================
    // GET ALLOCATIONS BY LECTURER
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD','LECTURER')")
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<?> getByLecturer(
            @PathVariable Long lecturerId) {


        Lecturer requestedLecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElse(null);


        if (requestedLecturer == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // -----------------------------------------------------
        // ADMIN CAN ACCESS ANY LECTURER
        // -----------------------------------------------------

        if (isAdmin()) {

            return ResponseEntity.ok(
                    lecturerAllocationRepository
                            .findByLecturer_LecturerId(
                                    lecturerId
                            )
            );
        }


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isLecturer =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_LECTURER")
                        );


        // -----------------------------------------------------
        // LECTURER CAN ACCESS ONLY OWN ALLOCATIONS
        // -----------------------------------------------------

        if (isLecturer) {

            Lecturer loggedInLecturer =
                    getLoggedInLecturer();


            if (loggedInLecturer == null ||
                    !loggedInLecturer
                            .getLecturerId()
                            .equals(lecturerId)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "You are not authorized to access "
                                        + "another lecturer's allocations."
                        );
            }


            return ResponseEntity.ok(
                    lecturerAllocationRepository
                            .findByLecturer_LecturerId(
                                    lecturerId
                            )
            );
        }


        // -----------------------------------------------------
        // HOD CAN ACCESS ONLY LECTURERS IN OWN DEPARTMENT
        // -----------------------------------------------------

        Lecturer hod =
                getLoggedInLecturer();


        if (hod == null ||
                hod.getDepartment() == null ||
                requestedLecturer.getDepartment() == null ||
                !hod.getDepartment()
                        .getDepartmentId()
                        .equals(
                                requestedLecturer
                                        .getDepartment()
                                        .getDepartmentId()
                        )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to access "
                                    + "another department's allocations."
                    );
        }


        return ResponseEntity.ok(
                lecturerAllocationRepository
                        .findByLecturer_LecturerId(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // CREATE ALLOCATION
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PostMapping
    public ResponseEntity<?> saveAllocation(
            @RequestBody LecturerAllocation allocation,
            Authentication authentication) {


        if (allocation == null ||
                allocation.getLecturer() == null ||
                allocation.getLecturer()
                        .getLecturerId() == null ||
                allocation.getSubject() == null ||
                allocation.getSubject()
                        .getSubjectId() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Lecturer and subject are required."
                    );
        }


        Long lecturerId =
                allocation
                        .getLecturer()
                        .getLecturerId();


        Long subjectId =
                allocation
                        .getSubject()
                        .getSubjectId();


        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElse(null);


        Subject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElse(null);


        if (lecturer == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Lecturer not found."
                    );
        }


        if (subject == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Subject not found."
                    );
        }


        // -----------------------------------------------------
        // HOD DEPARTMENT SECURITY
        // -----------------------------------------------------

        if (!isAdmin() &&
                !isAllocationWithinHODDepartment(
                        lecturer,
                        subject
                )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You can only create allocations "
                                    + "for lecturers and subjects "
                                    + "belonging to your department."
                    );
        }


        // -----------------------------------------------------
        // USE DATABASE ENTITIES
        // -----------------------------------------------------

        allocation.setLecturer(lecturer);
        allocation.setSubject(subject);


        try {

            LecturerAllocation savedAllocation =
                    lecturerAllocationService
                            .saveAllocation(
                                    allocation,
                                    authentication
                            );


            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedAllocation);

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );
        }
    }


    // =========================================================
    // UPDATE ALLOCATION
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAllocation(
            @PathVariable Long id,
            @RequestBody LecturerAllocation allocation,
            Authentication authentication) {


        LecturerAllocation existingAllocation =
                lecturerAllocationRepository
                        .findById(id)
                        .orElse(null);


        if (existingAllocation == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        if (allocation == null ||
                allocation.getLecturer() == null ||
                allocation.getLecturer()
                        .getLecturerId() == null ||
                allocation.getSubject() == null ||
                allocation.getSubject()
                        .getSubjectId() == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Lecturer and subject are required."
                    );
        }


        // -----------------------------------------------------
        // HOD MUST OWN EXISTING ALLOCATION
        // -----------------------------------------------------

        if (!isAdmin() &&
                !isHODDepartmentOwner(
                        existingAllocation
                )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to modify "
                                    + "another department's allocation."
                    );
        }


        Long lecturerId =
                allocation
                        .getLecturer()
                        .getLecturerId();


        Long subjectId =
                allocation
                        .getSubject()
                        .getSubjectId();


        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElse(null);


        Subject subject =
                subjectRepository
                        .findById(subjectId)
                        .orElse(null);


        if (lecturer == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Lecturer not found."
                    );
        }


        if (subject == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Subject not found."
                    );
        }


        // -----------------------------------------------------
        // HOD MUST OWN NEW LECTURER + SUBJECT
        // -----------------------------------------------------

        if (!isAdmin() &&
                !isAllocationWithinHODDepartment(
                        lecturer,
                        subject
                )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You can only assign lecturers and "
                                    + "subjects belonging to your department."
                    );
        }


        allocation.setLecturer(lecturer);
        allocation.setSubject(subject);


        try {

            LecturerAllocation updatedAllocation =
                    lecturerAllocationService
                            .updateAllocation(
                                    id,
                                    allocation,
                                    authentication
                            );


            return ResponseEntity.ok(
                    updatedAllocation
            );

        } catch (IllegalArgumentException |
                 IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );
        }
    }


    // =========================================================
    // DELETE ALLOCATION
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAllocation(
            @PathVariable Long id,
            Authentication authentication) {


        LecturerAllocation allocation =
                lecturerAllocationRepository
                        .findById(id)
                        .orElse(null);


        if (allocation == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // -----------------------------------------------------
        // HOD CAN DELETE ONLY OWN DEPARTMENT ALLOCATION
        // -----------------------------------------------------

        if (!isAdmin() &&
                !isHODDepartmentOwner(
                        allocation
                )) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "You are not authorized to delete "
                                    + "another department's allocation."
                    );
        }


        try {

            lecturerAllocationService
                    .deleteAllocation(
                            id,
                            authentication
                    );


            return ResponseEntity.ok(
                    "Lecturer allocation deleted successfully."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );
        }
    }
}
