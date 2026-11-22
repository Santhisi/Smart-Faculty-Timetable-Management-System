package com.smartfaculty.controller;

import com.smartfaculty.entity.Student;
import com.smartfaculty.entity.StudentSubjectRegistration;
import com.smartfaculty.entity.Subject;
import com.smartfaculty.enums.DegreeType;
import com.smartfaculty.repository.StudentRepository;
import com.smartfaculty.repository.SpecializationRepository;
import com.smartfaculty.service.StudentSubjectRegistrationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-subject-registrations")
public class StudentSubjectRegistrationController {

    @Autowired
    private StudentSubjectRegistrationService registrationService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private SpecializationRepository specializationRepository;


    // =====================================================
    // CREATE REGISTRATION
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @PostMapping
    public ResponseEntity<?> saveRegistration(
            @RequestBody StudentSubjectRegistration registration) {

        try {

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            /*
             * ADMIN can create a registration for any student.
             *
             * STUDENT must always use their own student account.
             */
            if (authentication.getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            authority != null &&
                                    authority.getAuthority()
                                            .equals("ROLE_STUDENT"))) {

                String username =
                        authentication.getName();

                Student loggedInStudent =
                        studentRepository
                                .findByUser_Username(username)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Student account not found."
                                        )
                                );

                /*
                 * Do not trust the student ID sent by React.
                 * Replace it with the authenticated student's ID.
                 */
                registration.setStudent(
                        loggedInStudent
                );
            }


            StudentSubjectRegistration savedRegistration =
                    registrationService.saveRegistration(
                            registration,
                            authentication
                    );

            return ResponseEntity.ok(
                    savedRegistration
            );

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
    // UPDATE REGISTRATION - ADMIN / STUDENT
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRegistration(
            @PathVariable Long id,
            @RequestBody StudentSubjectRegistration registration) {

        try {

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            String username =
                    authentication.getName();


            /*
             * ADMIN can update any registration.
             *
             * STUDENT can update only their own registration.
             */
            if (authentication.getAuthorities()
                    .stream()
                    .anyMatch(authority ->
                            authority != null &&
                                    authority.getAuthority()
                                            .equals("ROLE_STUDENT"))) {

                Student loggedInStudent =
                        studentRepository
                                .findByUser_Username(username)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Student account not found."
                                        )
                                );


                StudentSubjectRegistration existingRegistration =
                        registrationService
                                .getRegistrationById(
                                        id,
                                        authentication
                                );


                if (existingRegistration.getStudent() == null ||
                        !loggedInStudent.getStudentId()
                                .equals(
                                        existingRegistration
                                                .getStudent()
                                                .getStudentId()
                                )) {

                    return ResponseEntity
                            .status(403)
                            .body(
                                    "You are not allowed to update another student's registration."
                            );
                }


                /*
                 * Do not allow the student to change the owner
                 * of the registration.
                 */
                registration.setStudent(
                        loggedInStudent
                );
            }


            StudentSubjectRegistration updatedRegistration =
                    registrationService.updateRegistration(
                            id,
                            registration,
                            authentication
                    );

            return ResponseEntity.ok(
                    updatedRegistration
            );

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
    // GET ALL REGISTRATIONS
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping
    public ResponseEntity<List<StudentSubjectRegistration>>
    getAllRegistrations(
            Authentication authentication) {

        return ResponseEntity.ok(
                registrationService.getAllRegistrations(
                        authentication
                )
        );
    }


    // =====================================================
    // GET REGISTRATION BY ID
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRegistrationById(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            StudentSubjectRegistration registration =
                    registrationService
                            .getRegistrationById(
                                    id,
                                    authentication
                            );

            return ResponseEntity.ok(
                    registration
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }


    // =====================================================
    // GET AVAILABLE SUBJECTS FOR STUDENT
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD','STUDENT')")
    @GetMapping("/available/student/{studentId}")
    public ResponseEntity<?> getAvailableSubjectsForStudent(
            @PathVariable Long studentId,
            Authentication authentication) {

        try {

            List<Subject> subjects =
                    registrationService
                            .getAvailableSubjectsForStudent(
                                    studentId,
                                    authentication
                            );

            return ResponseEntity.ok(
                    subjects
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(403)
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET REGISTRATIONS BY STUDENT
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD','STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getRegistrationsByStudentId(
            @PathVariable Long studentId,
            Authentication authentication) {

        try {

            List<StudentSubjectRegistration>
                    registrations =
                    registrationService
                            .getRegistrationsByStudentId(
                                    studentId,
                                    authentication
                            );

            return ResponseEntity.ok(
                    registrations
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(403)
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET REGISTRATIONS BY STUDENT + YEAR + SEMESTER
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD','STUDENT')")
    @GetMapping(
            "/student/{studentId}/year/{academicYear}/semester/{semester}"
    )
    public ResponseEntity<?> getRegistrationsByStudentAndYearAndSemester(
            @PathVariable Long studentId,
            @PathVariable Integer academicYear,
            @PathVariable Integer semester,
            Authentication authentication) {

        try {

            List<StudentSubjectRegistration>
                    registrations =
                    registrationService
                            .getRegistrationsByStudentAndYearAndSemester(
                                    studentId,
                                    academicYear,
                                    semester,
                                    authentication
                            );

            return ResponseEntity.ok(
                    registrations
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(403)
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET REGISTRATIONS BY SUBJECT
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<?> getRegistrationsBySubjectId(
            @PathVariable Long subjectId,
            Authentication authentication) {

        try {

            List<StudentSubjectRegistration>
                    registrations =
                    registrationService
                            .getRegistrationsBySubjectId(
                                    subjectId,
                                    authentication
                            );

            return ResponseEntity.ok(
                    registrations
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(403)
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // GET ACTIVE SPECIALIZATIONS FOR STUDENT REGISTRATION
    // =====================================================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/specializations")
    public ResponseEntity<?> getRegistrationSpecializations() {
        return ResponseEntity.ok(
                specializationRepository.findByActiveTrue()
        );
    }


    // =====================================================
    // GET CURRENT STUDENT REGISTRATION STATUS
    // =====================================================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/status/me")
    public ResponseEntity<?> getMyRegistrationStatus(
            Authentication authentication) {

        try {
            Student student = studentRepository
                    .findByUser_Username(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Student account not found."
                    ));

            return ResponseEntity.ok(
                    registrationService.getRegistrationStatus(
                            student.getStudentId(),
                            authentication
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }


    // =====================================================
    // COMPLETE CURRENT-SEMESTER STUDENT REGISTRATION
    // =====================================================

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/complete")
    public ResponseEntity<?> completeMyRegistration(
            @RequestBody java.util.Map<String, Object> request,
            Authentication authentication) {

        try {
            Student student = studentRepository
                    .findByUser_Username(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Student account not found."
                    ));

            Long majorId = request.get("majorSpecializationId") == null
                    ? null
                    : ((Number) request.get("majorSpecializationId")).longValue();

            Long minorId = request.get("minorSpecializationId") == null
                    ? null
                    : ((Number) request.get("minorSpecializationId")).longValue();

            DegreeType degreeType = request.get("degreeType") == null
                    ? null
                    : DegreeType.valueOf(request.get("degreeType").toString().trim().toUpperCase());

            java.util.List<Long> preferredIds = new java.util.ArrayList<>();
            Object rawPreferred = request.get("preferredSubjectIds");
            if (rawPreferred instanceof java.util.List<?> rawList) {
                for (Object item : rawList) {
                    if (item instanceof Number number) {
                        preferredIds.add(number.longValue());
                    }
                }
            }

            return ResponseEntity.ok(
                    registrationService.completeRegistration(
                            student.getStudentId(),
                            majorId,
                            minorId,
                            degreeType,
                            preferredIds,
                            authentication
                    )
            );

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }


    // =====================================================
    // DELETE REGISTRATION
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRegistration(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            registrationService.deleteRegistration(
                    id,
                    authentication
            );

            return ResponseEntity.ok(
                    "Student subject registration deleted successfully."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .notFound()
                    .build();

        } catch (IllegalStateException e) {

            return ResponseEntity
                    .status(403)
                    .body(e.getMessage());
        }
    }
}