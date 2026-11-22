package com.smartfaculty.controller;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.RescheduleRequest;
import com.smartfaculty.service.RescheduleRequestService;
import com.smartfaculty.repository.LecturerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/reschedule-requests")

public class RescheduleRequestController {


    @Autowired
    private RescheduleRequestService requestService;


    @Autowired
    private LecturerRepository lecturerRepository;


    // =========================================================
    // GET AUTHENTICATED USERNAME
    // =========================================================

    private String getAuthenticatedUsername() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null
                || !authentication.isAuthenticated()) {

            return null;
        }


        return authentication.getName();
    }


    // =========================================================
    // GET LOGGED-IN LECTURER / HOD
    // =========================================================

    private Lecturer getLoggedInLecturer() {

        String username =
                getAuthenticatedUsername();


        if (username == null
                || username.trim().isEmpty()) {

            return null;
        }


        return lecturerRepository
                .findByUser_Username(username)
                .orElse(null);
    }


    // =========================================================
    // CHECK ADMIN
    // =========================================================

    private boolean isAdmin() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                );
    }


    // =========================================================
    // CHECK HOD
    // =========================================================

    private boolean isHOD() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                "ROLE_HOD".equals(
                                        authority.getAuthority()
                                )
                );
    }


    // =========================================================
    // CHECK LECTURER
    // =========================================================

    private boolean isLecturer() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                "ROLE_LECTURER".equals(
                                        authority.getAuthority()
                                )
                );
    }


    // =========================================================
    // CHECK WHETHER REQUEST BELONGS TO HOD DEPARTMENT
    // =========================================================

    private boolean isHODDepartmentOwner(
            RescheduleRequest request) {

        if (request == null
                || request.getLecturer() == null) {

            return false;
        }


        Lecturer hod =
                getLoggedInLecturer();


        if (hod == null
                || hod.getDepartment() == null
                || hod.getDepartment().getDepartmentId() == null) {

            return false;
        }


        Lecturer requestLecturer =
                request.getLecturer();


        if (requestLecturer.getDepartment() == null
                || requestLecturer
                .getDepartment()
                .getDepartmentId() == null) {

            return false;
        }


        return hod.getDepartment()
                .getDepartmentId()
                .equals(
                        requestLecturer
                                .getDepartment()
                                .getDepartmentId()
                );
    }


    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @PreAuthorize("hasAnyRole('LECTURER','HOD')")
    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestBody RescheduleRequest request) {

        try {

            // -------------------------------------------------
            // LECTURER / HOD MUST BE LINKED TO LECTURER RECORD
            // -------------------------------------------------

            Lecturer loggedInLecturer =
                    getLoggedInLecturer();


            if (loggedInLecturer == null) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Your account is not linked "
                                        + "to a lecturer record."
                        );
            }


            // -------------------------------------------------
            // FORCE THE REQUEST OWNER
            // -------------------------------------------------

            request.setLecturer(
                    loggedInLecturer
            );


            RescheduleRequest savedRequest =
                    requestService.createRequest(
                            request
                    );


            return ResponseEntity.ok(
                    savedRequest
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            e.getMessage()
                    );
        }
    }


    // =========================================================
    // GET ALL REQUESTS
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<RescheduleRequest>>
    getAllRequests() {

        return ResponseEntity.ok(
                requestService.getAllRequests()
        );
    }


    // =========================================================
    // GET REQUESTS OF ONE LECTURER
    // =========================================================

    @PreAuthorize(
            "hasAnyRole('ADMIN','LECTURER','HOD')"
    )
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<?> getLecturerRequests(
            @PathVariable Long lecturerId) {


        // -------------------------------------------------
        // ADMIN
        // -------------------------------------------------

        if (isAdmin()) {

            return ResponseEntity.ok(
                    requestService
                            .getLecturerRequests(
                                    lecturerId
                            )
            );
        }


        Lecturer loggedInLecturer =
                getLoggedInLecturer();


        if (loggedInLecturer == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Your account is not linked "
                                    + "to a lecturer record."
                    );
        }


        // -------------------------------------------------
        // HOD
        // -------------------------------------------------

        if (isHOD()) {

            if (loggedInLecturer.getDepartment() == null
                    || loggedInLecturer
                    .getDepartment()
                    .getDepartmentId() == null) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "HOD is not assigned to a department."
                        );
            }


            Lecturer requestedLecturer =
                    lecturerRepository
                            .findById(
                                    lecturerId
                            )
                            .orElse(null);


            if (requestedLecturer == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }


            if (requestedLecturer.getDepartment() == null
                    || requestedLecturer
                    .getDepartment()
                    .getDepartmentId() == null
                    || !loggedInLecturer
                    .getDepartment()
                    .getDepartmentId()
                    .equals(
                            requestedLecturer
                                    .getDepartment()
                                    .getDepartmentId()
                    )) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Access denied. "
                                        + "You can only view reschedule "
                                        + "requests from lecturers "
                                        + "in your department."
                        );
            }


            return ResponseEntity.ok(
                    requestService
                            .getLecturerRequests(
                                    lecturerId
                            )
            );
        }


        // -------------------------------------------------
        // LECTURER
        // -------------------------------------------------

        if (isLecturer()) {

            if (loggedInLecturer.getLecturerId() == null
                    || !loggedInLecturer
                    .getLecturerId()
                    .equals(lecturerId)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Access denied. "
                                        + "You can only view your own "
                                        + "reschedule requests."
                        );
            }


            return ResponseEntity.ok(
                    requestService
                            .getLecturerRequests(
                                    lecturerId
                            )
            );
        }


        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Access denied.");
    }


    // =========================================================
    // GET REQUEST BY ID
    // =========================================================

    @PreAuthorize(
            "hasAnyRole('ADMIN','LECTURER','HOD')"
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(
            @PathVariable Long id) {


        RescheduleRequest request =
                requestService.getRequestById(
                        id
                );


        if (request == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // -------------------------------------------------
        // ADMIN
        // -------------------------------------------------

        if (isAdmin()) {

            return ResponseEntity.ok(
                    request
            );
        }


        Lecturer loggedInLecturer =
                getLoggedInLecturer();


        if (loggedInLecturer == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Your account is not linked "
                                    + "to a lecturer record."
                    );
        }


        // -------------------------------------------------
        // HOD
        // -------------------------------------------------

        if (isHOD()) {

            if (!isHODDepartmentOwner(request)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Access denied. "
                                        + "You can only view reschedule "
                                        + "requests from your department."
                        );
            }


            return ResponseEntity.ok(
                    request
            );
        }


        // -------------------------------------------------
        // LECTURER
        // -------------------------------------------------

        if (isLecturer()) {

            if (request.getLecturer() == null
                    || request.getLecturer()
                    .getLecturerId() == null
                    || loggedInLecturer
                    .getLecturerId() == null
                    || !loggedInLecturer
                    .getLecturerId()
                    .equals(
                            request.getLecturer()
                                    .getLecturerId()
                    )) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Access denied. "
                                        + "You can only view your own "
                                        + "reschedule requests."
                        );
            }


            return ResponseEntity.ok(
                    request
            );
        }


        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body("Access denied.");
    }


    // =========================================================
    // GET REQUESTS BY STATUS
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RescheduleRequest>>
    getRequestsByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                requestService
                        .getRequestsByStatus(
                                status
                        )
        );
    }


    // =========================================================
    // APPROVE REQUEST
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<RescheduleRequest>
    approveRequest(
            @PathVariable Long id,
            @RequestParam(
                    required = false,
                    defaultValue =
                            "Reschedule request approved."
            )
            String adminComment) {

        RescheduleRequest request =
                requestService.approveRequest(
                        id,
                        adminComment
                );


        if (request == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        return ResponseEntity.ok(
                request
        );
    }


    // =========================================================
    // REJECT REQUEST
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<RescheduleRequest>
    rejectRequest(
            @PathVariable Long id,
            @RequestParam(
                    required = false,
                    defaultValue =
                            "Reschedule request rejected."
            )
            String adminComment) {

        RescheduleRequest request =
                requestService.rejectRequest(
                        id,
                        adminComment
                );


        if (request == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        return ResponseEntity.ok(
                request
        );
    }


    // =========================================================
    // DELETE REQUEST
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteRequest(
            @PathVariable Long id) {

        RescheduleRequest request =
                requestService.getRequestById(
                        id
                );


        if (request == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        requestService.deleteRequest(
                id
        );


        return ResponseEntity.ok(
                "Reschedule request deleted successfully."
        );
    }
}