package com.smartfaculty.controller;

import com.smartfaculty.entity.Specialization;
import com.smartfaculty.service.SpecializationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specializations")
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    // =========================================================
    // GET ALL SPECIALIZATIONS
    // ADMIN = ALL
    // HOD = OWN DEPARTMENT
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping
    public ResponseEntity<List<Specialization>>
    getAllSpecializations(
            Authentication authentication) {

        return ResponseEntity.ok(
                specializationService.getAllSpecializations(
                        authentication
                )
        );
    }

    // =========================================================
    // GET SPECIALIZATION BY ID
    // ADMIN = ANY
    // HOD = OWN DEPARTMENT ONLY
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @GetMapping("/{id}")
    public ResponseEntity<Specialization>
    getSpecializationById(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                specializationService.getSpecializationById(
                        id,
                        authentication
                )
        );
    }

    // =========================================================
    // CREATE SPECIALIZATION
    // ADMIN + HOD
    // HOD = OWN DEPARTMENT ONLY
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PostMapping
    public ResponseEntity<Specialization>
    createSpecialization(
            @RequestBody Specialization specialization,
            Authentication authentication) {

        return ResponseEntity.ok(
                specializationService.saveSpecialization(
                        specialization,
                        authentication
                )
        );
    }

    // =========================================================
    // UPDATE SPECIALIZATION
    // ADMIN + HOD
    // HOD = OWN DEPARTMENT ONLY
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @PutMapping("/{id}")
    public ResponseEntity<Specialization>
    updateSpecialization(
            @PathVariable Long id,
            @RequestBody Specialization specialization,
            Authentication authentication) {

        return ResponseEntity.ok(
                specializationService.updateSpecialization(
                        id,
                        specialization,
                        authentication
                )
        );
    }

    // =========================================================
    // DELETE / DEACTIVATE SPECIALIZATION
    // ADMIN + HOD
    // HOD = OWN DEPARTMENT ONLY
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','HOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteSpecialization(
            @PathVariable Long id,
            Authentication authentication) {

        specializationService.deleteSpecialization(
                id,
                authentication
        );

        return ResponseEntity.ok(
                "Specialization deactivated successfully."
        );
    }
}