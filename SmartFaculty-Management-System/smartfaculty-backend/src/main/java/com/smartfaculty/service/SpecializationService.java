package com.smartfaculty.service;

import com.smartfaculty.entity.Specialization;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface SpecializationService {

    Specialization saveSpecialization(
            Specialization specialization,
            Authentication authentication
    );

    List<Specialization> getAllSpecializations(
            Authentication authentication
    );

    Specialization getSpecializationById(
            Long id,
            Authentication authentication
    );

    Specialization updateSpecialization(
            Long id,
            Specialization specialization,
            Authentication authentication
    );

    void deleteSpecialization(
            Long id,
            Authentication authentication
    );
}