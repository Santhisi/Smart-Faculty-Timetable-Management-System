package com.smartfaculty.controller;

import com.smartfaculty.dto.StudentRegistrationRequest;
import com.smartfaculty.dto.StudentResponse;
import com.smartfaculty.service.StudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping("/register")
    public ResponseEntity<StudentResponse> registerStudent(
            @RequestBody StudentRegistrationRequest request) {
        return ResponseEntity.ok(studentService.registerStudent(request));
    }

    // ADMIN = all students
    // HOD = own department students
    // STUDENT = only their own student record
    @PreAuthorize("hasAnyRole('ADMIN','HOD','STUDENT')")
    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents(
            Authentication authentication) {
        return ResponseEntity.ok(studentService.getAllStudents(authentication));
    }

    // ADMIN/HOD can inspect an allowed student; STUDENT can inspect only self.
    @PreAuthorize("hasAnyRole('ADMIN','HOD','STUDENT')")
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(studentService.getStudentById(id, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @RequestBody StudentRegistrationRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(studentService.updateStudent(id, request, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable Long id,
            Authentication authentication) {

        try {
            studentService.deleteStudent(id, authentication);

            return ResponseEntity.ok("Student deleted successfully.");

        } catch (ResponseStatusException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
