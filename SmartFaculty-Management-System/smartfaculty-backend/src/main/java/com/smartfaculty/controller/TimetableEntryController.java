package com.smartfaculty.controller;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.TimetableEntry;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.service.TimetableEntryService;

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
@RequestMapping("/api/timetable")

public class TimetableEntryController {

    @Autowired
    private TimetableEntryService timetableService;

    @Autowired
    private LecturerRepository lecturerRepository;


    // =====================================================
    // GET AUTHENTICATED LECTURER / HOD
    // =====================================================

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


    // =====================================================
    // CHECK WHETHER USER IS ADMIN
    // =====================================================

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
                                "ROLE_ADMIN".equals(
                                        authority.getAuthority()
                                )
                        );
    }


    // =====================================================
    // CHECK WHETHER USER IS HOD
    // =====================================================

    private boolean isHOD() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return authentication != null &&
                authentication.isAuthenticated() &&
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                "ROLE_HOD".equals(
                                        authority.getAuthority()
                                )
                        );
    }


    // =====================================================
    // CHECK HOD DEPARTMENT OWNERSHIP
    // =====================================================

    private boolean isHODDepartmentOwner(
            TimetableEntry timetableEntry) {

        Lecturer hod =
                getLoggedInLecturer();

        if (hod == null ||
                hod.getDepartment() == null ||
                hod.getDepartment().getDepartmentId() == null) {

            return false;
        }

        if (timetableEntry == null ||
                timetableEntry.getLecturer() == null ||
                timetableEntry.getLecturer().getDepartment() == null ||
                timetableEntry.getLecturer()
                        .getDepartment()
                        .getDepartmentId() == null) {

            return false;
        }

        Long hodDepartmentId =
                hod.getDepartment()
                        .getDepartmentId();

        Long timetableDepartmentId =
                timetableEntry
                        .getLecturer()
                        .getDepartment()
                        .getDepartmentId();

        return hodDepartmentId.equals(
                timetableDepartmentId
        );
    }


    // =====================================================
    // CREATE
    // ADMIN ONLY
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createTimetableEntry(
            @RequestBody TimetableEntry timetableEntry) {

        try {

            TimetableEntry saved =
                    timetableService
                            .saveTimetableEntry(
                                    timetableEntry,
                                    SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                            );

            return ResponseEntity.ok(saved);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (SecurityException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET ALL
    // ADMIN / HOD ONLY
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @GetMapping
    public ResponseEntity<?> getAllTimetableEntries() {

        List<TimetableEntry> entries =
                timetableService
                        .getAllTimetableEntries(
                                SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                        );


        // -------------------------------------------------
        // HOD CAN ONLY SEE OWN DEPARTMENT
        // -------------------------------------------------

        if (isHOD()) {

            List<TimetableEntry> filteredEntries =
                    entries.stream()
                            .filter(
                                    this::isHODDepartmentOwner
                            )
                            .collect(Collectors.toList());

            return ResponseEntity.ok(
                    filteredEntries
            );
        }


        // -------------------------------------------------
        // ADMIN CAN SEE ALL
        // -------------------------------------------------

        return ResponseEntity.ok(entries);
    }


    // =====================================================
    // GET BY ID
    // ADMIN / HOD ONLY
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getTimetableEntryById(
            @PathVariable Long id) {

        try {

            TimetableEntry entry =
                    timetableService
                            .getTimetableEntryById(
                                    id,
                                    SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                            );


            // -------------------------------------------------
            // HOD CAN ONLY VIEW OWN DEPARTMENT
            // -------------------------------------------------

            if (isHOD() &&
                    !isHODDepartmentOwner(entry)) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "You are not authorized to access " +
                                        "this timetable entry."
                        );
            }


            return ResponseEntity.ok(entry);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }


    // =====================================================
    // GET BY YEAR + SEMESTER
    // ADMIN / HOD ONLY
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @GetMapping(
            "/year/{academicYear}/semester/{semester}"
    )
    public ResponseEntity<?> getTimetableByYearAndSemester(
            @PathVariable Integer academicYear,
            @PathVariable Integer semester) {

        try {

            List<TimetableEntry> entries =
                    timetableService
                            .getTimetableByYearAndSemester(
                                    academicYear,
                                    semester,
                                    SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                            );


            // -------------------------------------------------
            // HOD CAN ONLY SEE OWN DEPARTMENT
            // -------------------------------------------------

            if (isHOD()) {

                entries =
                        entries.stream()
                                .filter(
                                        this::isHODDepartmentOwner
                                )
                                .collect(Collectors.toList());
            }


            return ResponseEntity.ok(entries);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET BY LECTURER
    // ADMIN / HOD / LECTURER
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'LECTURER')")
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<?> getTimetableByLecturer(
            @PathVariable Long lecturerId) {

        try {

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();


            // -------------------------------------------------
            // HOD DEPARTMENT CHECK
            // -------------------------------------------------

            if (isHOD()) {

                Lecturer hod =
                        getLoggedInLecturer();

                Lecturer requestedLecturer =
                        lecturerRepository
                                .findById(lecturerId)
                                .orElse(null);


                if (hod == null ||
                        hod.getDepartment() == null ||
                        requestedLecturer == null ||
                        requestedLecturer.getDepartment() == null) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                    "You are not authorized to access " +
                                            "this lecturer timetable."
                            );
                }


                Long hodDepartmentId =
                        hod.getDepartment()
                                .getDepartmentId();

                Long requestedDepartmentId =
                        requestedLecturer
                                .getDepartment()
                                .getDepartmentId();


                if (!hodDepartmentId.equals(
                        requestedDepartmentId)) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                    "You can only access timetables " +
                                            "within your department."
                            );
                }
            }


            // -------------------------------------------------
            // LECTURER CAN ONLY SEE OWN TIMETABLE
            // -------------------------------------------------

            if (authentication != null &&
                    authentication.isAuthenticated() &&
                    authentication.getAuthorities()
                            .stream()
                            .anyMatch(authority ->
                                    "ROLE_LECTURER".equals(
                                            authority.getAuthority()
                                    )
                            )) {

                Lecturer loggedInLecturer =
                        getLoggedInLecturer();

                if (loggedInLecturer == null ||
                        loggedInLecturer.getLecturerId() == null ||
                        !loggedInLecturer
                                .getLecturerId()
                                .equals(lecturerId)) {

                    return ResponseEntity
                            .status(HttpStatus.FORBIDDEN)
                            .body(
                                    "You can only access your own timetable."
                            );
                }
            }


            return ResponseEntity.ok(
                    timetableService
                            .getTimetableByLecturer(
                                    lecturerId,
                                    SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                            )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET BY SUBJECT
    // ADMIN / HOD ONLY
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<?> getTimetableBySubject(
            @PathVariable Long subjectId) {

        try {

            List<TimetableEntry> entries =
                    timetableService
                            .getTimetableBySubject(
                                    subjectId,
                                    SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                            );


            // -------------------------------------------------
            // HOD CAN ONLY SEE OWN DEPARTMENT
            // -------------------------------------------------

            if (isHOD()) {

                entries =
                        entries.stream()
                                .filter(
                                        this::isHODDepartmentOwner
                                )
                                .collect(Collectors.toList());
            }


            return ResponseEntity.ok(entries);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // UPDATE
    // ADMIN ONLY
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimetableEntry(
            @PathVariable Long id,
            @RequestBody TimetableEntry timetableEntry) {

        try {

            TimetableEntry updated =
                    timetableService
                            .updateTimetableEntry(
                                    id,
                                    timetableEntry,
                                    SecurityContextHolder
                                            .getContext()
                                            .getAuthentication()
                            );

            return ResponseEntity.ok(updated);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // DELETE
    // ADMIN ONLY
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimetableEntry(
            @PathVariable Long id) {

        try {

            timetableService
                    .deleteTimetableEntry(
                            id,
                            SecurityContextHolder
                                    .getContext()
                                    .getAuthentication()
                    );

            return ResponseEntity.ok(
                    "Timetable entry deleted successfully."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }
}
