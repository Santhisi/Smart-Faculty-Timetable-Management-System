package com.smartfaculty.controller;

import com.smartfaculty.entity.Classroom;
import com.smartfaculty.service.ClassroomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
public class ClassroomController {

    @Autowired
    private ClassroomService classroomService;


    // =====================================================
    // GET ACTIVE CLASSROOMS
    // ADMIN + HOD
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping
    public ResponseEntity<List<Classroom>> getAllClassrooms(
            Authentication authentication) {

        List<Classroom> classrooms =
                classroomService.getAllClassrooms(
                        authentication
                );

        /*
         * The original endpoint returned only active classrooms.
         * Preserve that behavior.
         */
        classrooms.removeIf(
                classroom ->
                        classroom == null ||
                                Boolean.FALSE.equals(
                                        classroom.getActive()
                                )
        );

        return ResponseEntity.ok(classrooms);
    }


    // =====================================================
    // GET CLASSROOM BY ID
    // ADMIN + HOD
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping("/{id}")
    public ResponseEntity<Classroom> getClassroomById(
            @PathVariable Long id,
            Authentication authentication) {

        Classroom classroom =
                classroomService.getClassroomById(
                        id,
                        authentication
                );

        return ResponseEntity.ok(classroom);
    }


    // =====================================================
    // CREATE CLASSROOM
    // ADMIN + HOD
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PostMapping
    public ResponseEntity<Classroom> createClassroom(
            @RequestBody Classroom classroom,
            Authentication authentication) {

        return ResponseEntity.ok(
                classroomService.saveClassroom(
                        classroom,
                        authentication
                )
        );
    }


    // =====================================================
    // UPDATE CLASSROOM
    // ADMIN + HOD
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PutMapping("/{id}")
    public ResponseEntity<Classroom> updateClassroom(
            @PathVariable Long id,
            @RequestBody Classroom classroom,
            Authentication authentication) {

        return ResponseEntity.ok(
                classroomService.updateClassroom(
                        id,
                        classroom,
                        authentication
                )
        );
    }


    // =====================================================
    // DELETE / DEACTIVATE CLASSROOM
    // ADMIN + HOD
    // =====================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteClassroom(
            @PathVariable Long id,
            Authentication authentication) {

        classroomService.deleteClassroom(
                id,
                authentication
        );

        return ResponseEntity.ok(
                "Classroom deactivated successfully."
        );
    }
}