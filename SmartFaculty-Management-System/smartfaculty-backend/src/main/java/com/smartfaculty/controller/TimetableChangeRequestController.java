package com.smartfaculty.controller;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.TimetableChangeRequest;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.service.TimetableChangeRequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/timetable-change-requests")

public class TimetableChangeRequestController {


    @Autowired
    private TimetableChangeRequestService requestService;


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
            TimetableChangeRequest request) {

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
    // CREATE TIMETABLE CHANGE REQUEST
    // =========================================================

    @PreAuthorize("hasAnyRole('LECTURER','HOD')")
    @PostMapping
    public ResponseEntity<?> createRequest(
            @RequestBody TimetableChangeRequest request) {

        try {

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


            // IMPORTANT:
            // Ignore lecturer information sent by frontend.
            // Always use the authenticated lecturer.
            request.setLecturer(
                    loggedInLecturer
            );


            TimetableChangeRequest savedRequest =
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
    // GET ALL CHANGE REQUESTS
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<TimetableChangeRequest>>
    getAllRequests() {

        return ResponseEntity.ok(
                requestService.getAllRequests()
        );
    }


    // =========================================================
    // GET CHANGE REQUESTS BY LECTURER
    // =========================================================

    @PreAuthorize(
            "hasAnyRole('ADMIN','LECTURER','HOD')"
    )
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<?> getLecturerRequests(
            @PathVariable Long lecturerId) {


        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // HOD
        // -----------------------------------------------------

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


            // HOD can only access lecturers
            // belonging to the HOD's department.
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
                                        + "You can only view timetable "
                                        + "change requests from lecturers "
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


        // -----------------------------------------------------
        // LECTURER
        // -----------------------------------------------------

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
                                        + "timetable change requests."
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
    // GET CHANGE REQUEST BY ID
    // =========================================================

    @PreAuthorize(
            "hasAnyRole('ADMIN','LECTURER','HOD')"
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getRequestById(
            @PathVariable Long id) {


        TimetableChangeRequest request =
                requestService.getRequestById(
                        id
                );


        if (request == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // -----------------------------------------------------
        // ADMIN
        // -----------------------------------------------------

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


        // -----------------------------------------------------
        // HOD
        // -----------------------------------------------------

        if (isHOD()) {

            if (!isHODDepartmentOwner(request)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Access denied. "
                                        + "You can only view timetable "
                                        + "change requests from your "
                                        + "department."
                        );
            }


            return ResponseEntity.ok(
                    request
            );
        }


        // -----------------------------------------------------
        // LECTURER
        // -----------------------------------------------------

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
                                        + "timetable change requests."
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
    // GET CHANGE REQUESTS BY STATUS
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TimetableChangeRequest>>
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
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            @RequestParam(
                    required = false,
                    defaultValue =
                            "Timetable change request approved."
            )
            String adminResponse) {

        try {

            TimetableChangeRequest request =
                    requestService.approveRequest(
                            id,
                            adminResponse
                    );


            if (request == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }


            return ResponseEntity.ok(
                    request
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
    // REJECT REQUEST
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestParam(
                    required = false,
                    defaultValue =
                            "Timetable change request rejected."
            )
            String adminResponse) {

        try {

            TimetableChangeRequest request =
                    requestService.rejectRequest(
                            id,
                            adminResponse
                    );


            if (request == null) {

                return ResponseEntity
                        .notFound()
                        .build();
            }


            return ResponseEntity.ok(
                    request
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
    // UPDATE TIMETABLE CHANGE REQUEST
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequest(
            @PathVariable Long id,
            @RequestBody TimetableChangeRequest updatedRequest) {

        try {

            TimetableChangeRequest savedRequest =
                    requestService.updateRequest(
                            id,
                            updatedRequest
                    );

            return ResponseEntity.ok(savedRequest);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }


    // =========================================================
    // DELETE REQUEST
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRequest(
            @PathVariable Long id) {

        try {

            TimetableChangeRequest request =
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
                    "Timetable change request deleted successfully."
            );

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());

        } catch (RuntimeException e) {

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
