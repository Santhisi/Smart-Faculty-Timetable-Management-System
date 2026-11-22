package com.smartfaculty.controller;

import com.smartfaculty.entity.Subject;
import com.smartfaculty.repository.SubjectRepository;
import com.smartfaculty.service.SubjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectRepository subjectRepository;


    // =====================================================
    // GET ALL SUBJECTS
    // ADMIN = ALL
    // HOD = OWN DEPARTMENT
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping
    public ResponseEntity<List<Subject>> getAllSubjects(
            Authentication authentication) {

        return ResponseEntity.ok(
                subjectService.getAllSubjects(
                        authentication
                )
        );
    }


    // =====================================================
    // GET SUBJECT BY ID
    // ADMIN = ANY
    // HOD = OWN DEPARTMENT
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping("/{id}")
    public ResponseEntity<Subject> getSubjectById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                subjectService.getSubjectById(
                        id,
                        authentication
                )
        );
    }


    // =====================================================
    // CREATE SUBJECT
    // ADMIN + HOD
    // HOD = OWN DEPARTMENT ONLY
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PostMapping
    public ResponseEntity<Subject> saveSubject(
            @RequestBody Subject subject,
            Authentication authentication) {

        return ResponseEntity.ok(
                subjectService.saveSubject(
                        subject,
                        authentication
                )
        );
    }


    // =====================================================
    // UPDATE SUBJECT
    // ADMIN + HOD
    // HOD = OWN DEPARTMENT ONLY
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PutMapping("/{id}")
    public ResponseEntity<Subject> updateSubject(
            @PathVariable Long id,
            @RequestBody Subject subject,
            Authentication authentication) {

        return ResponseEntity.ok(
                subjectService.updateSubject(
                        id,
                        subject,
                        authentication
                )
        );
    }


    // =====================================================
    // DELETE SUBJECT
    // ADMIN + HOD
    // HOD = OWN DEPARTMENT ONLY
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubject(
            @PathVariable Long id,
            Authentication authentication) {

        String message = subjectService.deleteSubject(
                id,
                authentication
        );

        return ResponseEntity.ok(message);
    }


    // =====================================================
    // GET COMPULSORY SUBJECTS
    // =====================================================

    @GetMapping("/compulsory/{year}/{semester}")
    public ResponseEntity<List<Subject>> getCompulsorySubjects(
            @PathVariable Integer year,
            @PathVariable Integer semester) {

        return ResponseEntity.ok(
                subjectRepository
                        .findByCompulsoryTrueAndAcademicYearAndSemester(
                                year,
                                semester
                        )
        );
    }
}
