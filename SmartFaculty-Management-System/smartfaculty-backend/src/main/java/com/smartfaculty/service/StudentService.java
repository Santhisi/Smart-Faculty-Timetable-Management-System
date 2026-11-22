package com.smartfaculty.service;

import com.smartfaculty.dto.StudentRegistrationRequest;
import com.smartfaculty.dto.StudentResponse;

import org.springframework.security.core.Authentication;

import java.util.List;

public interface StudentService {

    // =====================================================
    // STUDENT REGISTRATION
    // =====================================================

    StudentResponse registerStudent(
            StudentRegistrationRequest request
    );


    // =====================================================
    // GET ALL STUDENTS
    // =====================================================

    List<StudentResponse> getAllStudents(
            Authentication authentication
    );


    // =====================================================
    // GET STUDENT BY ID
    // =====================================================

    StudentResponse getStudentById(
            Long id,
            Authentication authentication
    );


    // =====================================================
    // UPDATE STUDENT
    // =====================================================

    StudentResponse updateStudent(
            Long id,
            StudentRegistrationRequest request,
            Authentication authentication
    );


    // =====================================================
    // DELETE STUDENT
    // =====================================================

    void deleteStudent(
            Long id,
            Authentication authentication
    );
}