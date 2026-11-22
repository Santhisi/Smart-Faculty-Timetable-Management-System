package com.smartfaculty.controller;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.service.LecturerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerController {

    @Autowired
    private LecturerService lecturerService;

    @Autowired
    private LecturerRepository lecturerRepository;


    // =====================================================
    // CREATE LECTURER
    // ADMIN → ANY DEPARTMENT
    // HOD   → OWN DEPARTMENT ONLY
    // =====================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<Lecturer> saveLecturer(
            @RequestBody Lecturer lecturer,
            Authentication authentication) {

        return ResponseEntity.ok(
                lecturerService.saveLecturer(
                        lecturer,
                        authentication
                )
        );
    }


    // =====================================================
    // GET ALL LECTURERS
    // ADMIN → ALL
    // HOD   → OWN DEPARTMENT
    // =====================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<?> getAllLecturers(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication is required.");
        }

        return ResponseEntity.ok(
                lecturerService.getAllLecturers(
                        authentication
                )
        );
    }


    // =====================================================
    // GET LOGGED-IN LECTURER / HOD PROFILE
    // LECTURER / HOD
    // =====================================================

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('LECTURER', 'HOD')")
    public ResponseEntity<?> getMyProfile(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication is required.");
        }

        String username =
                authentication.getName();

        if (username == null ||
                username.trim().isEmpty()) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Authenticated username is invalid.");
        }

        Lecturer lecturer =
                lecturerRepository
                        .findByUser_Username(
                                username.trim()
                        )
                        .orElse(null);

        if (lecturer == null) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Lecturer profile not found.");
        }

        return ResponseEntity.ok(
                lecturer
        );
    }


    // =====================================================
    // GET LECTURER BY ID
    // ADMIN → ANY
    // HOD   → OWN DEPARTMENT
    // LECTURER → OWN PROFILE
    // =====================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD', 'LECTURER')")
    public ResponseEntity<?> getLecturerById(
            @PathVariable Long id,
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication is required.");
        }

        if (id == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Lecturer ID is required.");
        }

        try {

            return ResponseEntity.ok(
                    lecturerService.getLecturerById(
                            id,
                            authentication
                    )
            );

        } catch (ResponseStatusException e) {

            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // UPDATE LECTURER
    // ADMIN / HOD
    // =====================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<?> updateLecturer(
            @PathVariable Long id,
            @RequestBody Lecturer lecturer,
            Authentication authentication) {

        try {

            return ResponseEntity.ok(
                    lecturerService.updateLecturer(
                            id,
                            lecturer,
                            authentication
                    )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // DELETE LECTURER
    // ADMIN / HOD
    // =====================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOD')")
    public ResponseEntity<?> deleteLecturer(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            lecturerService.deleteLecturer(
                    id,
                    authentication
            );

            return ResponseEntity.ok(
                    "Lecturer deleted successfully."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }
    }
}