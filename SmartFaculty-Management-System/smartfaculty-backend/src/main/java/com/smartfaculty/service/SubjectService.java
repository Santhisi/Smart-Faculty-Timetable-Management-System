package com.smartfaculty.service;

import com.smartfaculty.entity.Subject;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SubjectService {

    // =====================================================
    // CREATE
    // =====================================================

    Subject saveSubject(
            Subject subject,
            Authentication authentication
    );


    // =====================================================
    // GET ALL
    // ADMIN = ALL
    // HOD = OWN DEPARTMENT
    // =====================================================

    List<Subject> getAllSubjects(
            Authentication authentication
    );


    // =====================================================
    // GET BY ID
    // ADMIN = ANY
    // HOD = OWN DEPARTMENT
    // =====================================================

    Subject getSubjectById(
            Long id,
            Authentication authentication
    );


    // =====================================================
    // UPDATE
    // ADMIN = ANY
    // HOD = OWN DEPARTMENT
    // =====================================================

    Subject updateSubject(
            Long id,
            Subject subject,
            Authentication authentication
    );


    // =====================================================
    // DELETE
    // ADMIN = ANY
    // HOD = OWN DEPARTMENT
    // =====================================================

    String deleteSubject(
            Long id,
            Authentication authentication
    );
}
