package com.smartfaculty.service;

import com.smartfaculty.entity.Lecturer;

import org.springframework.security.core.Authentication;

import java.util.List;

public interface LecturerService {

    // =====================================================
    // CREATE
    // ADMIN ONLY
    // =====================================================

    Lecturer saveLecturer(
            Lecturer lecturer,
            Authentication authentication
    );


    // =====================================================
    // GET ALL
    // ADMIN → ALL LECTURERS
    // HOD   → OWN DEPARTMENT LECTURERS
    // =====================================================

    List<Lecturer> getAllLecturers(
            Authentication authentication
    );


    // =====================================================
    // GET BY ID
    // ADMIN → ANY LECTURER
    // HOD   → OWN DEPARTMENT LECTURER
    // =====================================================

    Lecturer getLecturerById(
            Long id,
            Authentication authentication
    );


    // =====================================================
    // UPDATE
    // ADMIN → ANY LECTURER
    // HOD   → OWN DEPARTMENT LECTURER
    // =====================================================

    Lecturer updateLecturer(
            Long id,
            Lecturer lecturer,
            Authentication authentication
    );


    // =====================================================
    // DELETE
    // ADMIN → ANY LECTURER
    // HOD   → OWN DEPARTMENT LECTURER
    // =====================================================

    void deleteLecturer(
            Long id,
            Authentication authentication
    );
}