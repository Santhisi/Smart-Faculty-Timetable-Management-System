package com.smartfaculty.controller;

import com.smartfaculty.entity.AcademicYear;
import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Semester;
import com.smartfaculty.entity.Student;
import com.smartfaculty.entity.Timetable;

import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.StudentRepository;

import com.smartfaculty.service.TimetableService;

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
@RequestMapping("/api/timetables")

public class TimetableController {


    @Autowired
    private TimetableService timetableService;


    @Autowired
    private LecturerRepository lecturerRepository;


    @Autowired
    private StudentRepository studentRepository;


    // =========================================================
    // AUTHENTICATED USERNAME
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
                                "ROLE_ADMIN"
                                        .equals(
                                                authority.getAuthority()
                                        )
                );
    }


    // =========================================================
    // GET LOGGED-IN HOD
    // =========================================================

    private Lecturer getLoggedInHOD() {

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
    // GET HOD DEPARTMENT ID
    // =========================================================

    private Long getLoggedInHODDepartmentId() {

        Lecturer hod =
                getLoggedInHOD();


        if (hod == null
                || hod.getDepartment() == null
                || hod.getDepartment().getDepartmentId() == null) {

            return null;
        }


        return hod
                .getDepartment()
                .getDepartmentId();
    }


    // =========================================================
    // CHECK WHETHER TIMETABLE BELONGS TO HOD DEPARTMENT
    // =========================================================

    private boolean isHODDepartmentOwner(
            Timetable timetable) {

        if (timetable == null
                || timetable.getLecturerAllocation() == null
                || timetable
                .getLecturerAllocation()
                .getLecturer() == null) {

            return false;
        }


        Lecturer timetableLecturer =
                timetable
                        .getLecturerAllocation()
                        .getLecturer();


        if (timetableLecturer.getDepartment() == null
                || timetableLecturer
                .getDepartment()
                .getDepartmentId() == null) {

            return false;
        }


        Long hodDepartmentId =
                getLoggedInHODDepartmentId();


        if (hodDepartmentId == null) {

            return false;
        }


        return hodDepartmentId.equals(
                timetableLecturer
                        .getDepartment()
                        .getDepartmentId()
        );
    }


    // =========================================================
    // FILTER TIMETABLES BY HOD DEPARTMENT
    // =========================================================

    private List<Timetable> filterForHODDepartment(
            List<Timetable> timetables) {

        if (timetables == null) {

            return List.of();
        }


        return timetables
                .stream()
                .filter(this::isHODDepartmentOwner)
                .collect(Collectors.toList());
    }


    // =========================================================
    // CREATE
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createTimetable(
            @RequestBody Timetable timetable) {

        try {

            Timetable saved =
                    timetableService
                            .saveTimetable(
                                    timetable
                            );


            return ResponseEntity.ok(
                    saved
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
    // UPDATE
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimetable(
            @PathVariable Long id,
            @RequestBody Timetable timetable) {

        try {

            timetable.setTimetableId(id);


            Timetable updated =
                    timetableService
                            .saveTimetable(
                                    timetable
                            );


            return ResponseEntity.ok(
                    updated
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
    // GET ALL
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @GetMapping
    public ResponseEntity<List<Timetable>>
    getAllTimetables() {

        List<Timetable> timetables =
                timetableService
                        .getAllTimetables();


        // ADMIN can see everything.
        if (isAdmin()) {

            return ResponseEntity.ok(
                    timetables
            );
        }


        // HOD can see only own department.
        return ResponseEntity.ok(
                filterForHODDepartment(
                        timetables
                )
        );
    }


    // =========================================================
    // GET BY ID
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getTimetableById(
            @PathVariable Long id) {

        Timetable timetable =
                timetableService
                        .getTimetableById(
                                id
                        );


        if (timetable == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        // ADMIN can view any timetable.
        if (isAdmin()) {

            return ResponseEntity.ok(
                    timetable
            );
        }


        // HOD can view only own department.
        if (!isHODDepartmentOwner(timetable)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access denied. "
                                    + "You can only view timetables "
                                    + "belonging to your department."
                    );
        }


        return ResponseEntity.ok(
                timetable
        );
    }


    // =========================================================
    // GET BY YEAR + SEMESTER
    // =========================================================
    //
    // This endpoint remains accessible without @PreAuthorize
    // because the public homepage uses it to display timetables.
    //
    // If the authenticated user is an HOD, the result is filtered
    // to that HOD's department.
    //
    // ADMIN receives all timetables.
    // =========================================================

    @GetMapping(
            "/year/{yearLevel}/semester/{semester}"
    )
    public ResponseEntity<List<Timetable>>
    getByYearAndSemester(
            @PathVariable AcademicYear yearLevel,
            @PathVariable Semester semester) {

        List<Timetable> timetables =
                timetableService
                        .getActiveByYearAndSemester(
                                yearLevel,
                                semester
                        );


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        // Public / unauthenticated request.
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(
                authentication.getName()
        )) {

            return ResponseEntity.ok(
                    timetables
            );
        }


        // ADMIN can see everything.
        if (isAdmin()) {

            return ResponseEntity.ok(
                    timetables
            );
        }


        // HOD can see only own department.
        boolean isHOD =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        "ROLE_HOD".equals(
                                                authority.getAuthority()
                                        )
                        );


        if (isHOD) {

            return ResponseEntity.ok(
                    filterForHODDepartment(
                            timetables
                    )
            );
        }


        return ResponseEntity.ok(
                timetables
        );
    }


    // =========================================================
    // GET BY SPECIALIZATION
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    @GetMapping(
            "/specialization/{specializationId}"
    )
    public ResponseEntity<List<Timetable>>
    getBySpecialization(
            @PathVariable Long specializationId) {

        List<Timetable> timetables =
                timetableService
                        .getBySpecialization(
                                specializationId
                        );


        // ADMIN can see everything.
        if (isAdmin()) {

            return ResponseEntity.ok(
                    timetables
            );
        }


        // HOD can see only own department.
        return ResponseEntity.ok(
                filterForHODDepartment(
                        timetables
                )
        );
    }


    // =========================================================
    // GET STUDENT TIMETABLE
    // =========================================================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping(
            "/student/{studentId}"
    )
    public ResponseEntity<?> getByStudentId(
            @PathVariable Long studentId) {

        String username =
                getAuthenticatedUsername();


        if (username == null
                || username.trim().isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Authentication required."
                    );
        }


        Student loggedInStudent =
                studentRepository
                        .findByUser_Username(username)
                        .orElse(null);


        if (loggedInStudent == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Student account is not linked to a student record."
                    );
        }


        if (loggedInStudent.getStudentId() == null
                || !loggedInStudent
                .getStudentId()
                .equals(studentId)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access denied. "
                                    + "You can only view your own timetable."
                    );
        }


        return ResponseEntity.ok(
                timetableService
                        .getByStudentId(
                                studentId
                        )
        );
    }


    // =========================================================
    // GET LECTURER TIMETABLE
    // =========================================================

    @PreAuthorize(
            "hasAnyRole('ADMIN', 'LECTURER', 'HOD')"
    )
    @GetMapping(
            "/lecturer/{lecturerId}"
    )
    public ResponseEntity<?> getByLecturerId(
            @PathVariable Long lecturerId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        if (authentication == null
                || !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            "Authentication required."
                    );
        }


        // =====================================================
        // ADMIN
        // =====================================================

        if (isAdmin()) {

            return ResponseEntity.ok(
                    timetableService
                            .getByLecturerId(
                                    lecturerId
                            )
            );
        }


        // =====================================================
        // HOD
        // =====================================================

        boolean isHOD =
                authentication
                        .getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        "ROLE_HOD".equals(
                                                authority.getAuthority()
                                        )
                        );


        if (isHOD) {

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


            Long hodDepartmentId =
                    getLoggedInHODDepartmentId();


            if (hodDepartmentId == null
                    || requestedLecturer
                    .getDepartment() == null
                    || requestedLecturer
                    .getDepartment()
                    .getDepartmentId() == null
                    || !hodDepartmentId.equals(
                    requestedLecturer
                            .getDepartment()
                            .getDepartmentId()
            )) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                "Access denied. "
                                        + "You can only view timetables "
                                        + "of lecturers in your department."
                        );
            }


            return ResponseEntity.ok(
                    timetableService
                            .getByLecturerId(
                                    lecturerId
                            )
            );
        }


        // =====================================================
        // LECTURER
        // =====================================================

        String username =
                getAuthenticatedUsername();


        Lecturer loggedInLecturer =
                lecturerRepository
                        .findByUser_Username(username)
                        .orElse(null);


        if (loggedInLecturer == null) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Lecturer account is not linked "
                                    + "to a lecturer record."
                    );
        }


        if (loggedInLecturer.getLecturerId() == null
                || !loggedInLecturer
                .getLecturerId()
                .equals(lecturerId)) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                            "Access denied. "
                                    + "You can only view your own timetable."
                    );
        }


        return ResponseEntity.ok(
                timetableService
                        .getByLecturerId(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // DELETE
    // =========================================================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimetable(@PathVariable Long id) {

        try {

            String message = timetableService.deleteTimetable(id);

            return ResponseEntity.ok(message);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }

    }
}

