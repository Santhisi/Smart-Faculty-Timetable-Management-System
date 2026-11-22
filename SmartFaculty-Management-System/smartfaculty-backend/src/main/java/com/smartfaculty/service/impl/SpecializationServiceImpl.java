package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Department;
import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.Specialization;
import com.smartfaculty.repository.DepartmentRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.repository.SpecializationRepository;
import com.smartfaculty.service.SpecializationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    // =========================================================
    // CREATE SPECIALIZATION
    // =========================================================

    @Override
    public Specialization saveSpecialization(
            Specialization specialization,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (specialization == null) {
            throw new IllegalArgumentException(
                    "Specialization data is required."
            );
        }

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isHOD = hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {
            throw new IllegalStateException(
                    "You are not authorized to create specializations."
            );
        }

        validateSpecializationData(specialization);

        if (specialization.getDepartment() == null ||
                specialization.getDepartment().getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Department is required."
            );
        }

        Long requestedDepartmentId =
                specialization.getDepartment().getDepartmentId();

        Department department =
                departmentRepository.findById(requestedDepartmentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Department not found with id: "
                                                + requestedDepartmentId
                                )
                        );

        /*
         * HOD can create a specialization only inside
         * the HOD's own department.
         */
        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(authentication);

            if (!hodDepartmentId.equals(
                    department.getDepartmentId())) {

                throw new IllegalStateException(
                        "You are not authorized to create a specialization "
                                + "for another department."
                );
            }
        }

        specialization.setDepartment(department);

        return specializationRepository.save(specialization);
    }

    // =========================================================
    // GET ALL SPECIALIZATIONS
    // =========================================================

    @Override
    public List<Specialization> getAllSpecializations(
            Authentication authentication) {

        validateAuthentication(authentication);

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isHOD = hasRole(authentication, "ROLE_HOD");

        if (isAdmin) {
            return specializationRepository.findAll();
        }

        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(authentication);

            List<Specialization> allSpecializations =
                    specializationRepository.findAll();

            return filterByDepartment(
                    allSpecializations,
                    hodDepartmentId
            );
        }

        throw new IllegalStateException(
                "You are not authorized to access specializations."
        );
    }

    // =========================================================
    // GET SPECIALIZATION BY ID
    // =========================================================

    @Override
    public Specialization getSpecializationById(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Specialization ID is required."
            );
        }

        Specialization specialization =
                specializationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Specialization not found with id: "
                                                + id
                                )
                        );

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isHOD = hasRole(authentication, "ROLE_HOD");

        if (isAdmin) {
            return specialization;
        }

        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(authentication);

            validateHODSpecializationAccess(
                    specialization,
                    hodDepartmentId
            );

            return specialization;
        }

        throw new IllegalStateException(
                "You are not authorized to access this specialization."
        );
    }

    // =========================================================
    // UPDATE SPECIALIZATION
    // =========================================================

    @Override
    public Specialization updateSpecialization(
            Long id,
            Specialization specialization,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Specialization ID is required."
            );
        }

        if (specialization == null) {
            throw new IllegalArgumentException(
                    "Specialization data is required."
            );
        }

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isHOD = hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {
            throw new IllegalStateException(
                    "You are not authorized to update specializations."
            );
        }

        validateSpecializationData(specialization);

        Specialization existingSpecialization =
                specializationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Specialization not found with id: "
                                                + id
                                )
                        );

        /*
         * HOD can update only specializations belonging
         * to the HOD's own department.
         */
        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(authentication);

            validateHODSpecializationAccess(
                    existingSpecialization,
                    hodDepartmentId
            );
        }

        /*
         * Update code and name.
         */
        existingSpecialization.setSpecializationCode(
                specialization.getSpecializationCode()
        );

        existingSpecialization.setSpecializationName(
                specialization.getSpecializationName()
        );

        /*
         * Active status can be updated.
         */
        if (specialization.getActive() != null) {
            existingSpecialization.setActive(
                    specialization.getActive()
            );
        }

        /*
         * Department manipulation protection.
         *
         * ADMIN can change the department.
         * HOD cannot move a specialization to another department.
         */
        if (isAdmin) {

            if (specialization.getDepartment() != null &&
                    specialization.getDepartment().getDepartmentId() != null) {

                Long requestedDepartmentId =
                        specialization.getDepartment()
                                .getDepartmentId();

                Department department =
                        departmentRepository.findById(
                                requestedDepartmentId
                        ).orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Department not found with id: "
                                                + requestedDepartmentId
                                )
                        );

                existingSpecialization.setDepartment(
                        department
                );
            }

        } else if (isHOD) {

            /*
             * HOD must remain in the existing department.
             */
            Long existingDepartmentId =
                    getDepartmentId(existingSpecialization);

            if (specialization.getDepartment() != null &&
                    specialization.getDepartment().getDepartmentId() != null) {

                Long requestedDepartmentId =
                        specialization.getDepartment()
                                .getDepartmentId();

                if (!existingDepartmentId.equals(
                        requestedDepartmentId)) {

                    throw new IllegalStateException(
                            "HOD cannot change a specialization "
                                    + "to another department."
                    );
                }
            }
        }

        return specializationRepository.save(
                existingSpecialization
        );
    }

    // =========================================================
    // DELETE / DEACTIVATE SPECIALIZATION
    // =========================================================

    @Override
    public void deleteSpecialization(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Specialization ID is required."
            );
        }

        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isHOD = hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {
            throw new IllegalStateException(
                    "You are not authorized to deactivate specializations."
            );
        }

        Specialization existingSpecialization =
                specializationRepository.findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Specialization not found with id: "
                                                + id
                                )
                        );

        /*
         * HOD can deactivate only a specialization
         * belonging to their own department.
         */
        if (isHOD) {

            Long hodDepartmentId =
                    getAuthenticatedHODDepartmentId(authentication);

            validateHODSpecializationAccess(
                    existingSpecialization,
                    hodDepartmentId
            );
        }

        existingSpecialization.setActive(false);

        specializationRepository.save(
                existingSpecialization
        );
    }

    // =========================================================
    // AUTHENTICATION VALIDATION
    // =========================================================

    private void validateAuthentication(
            Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "Authentication is required."
            );
        }

        if (authentication.getName() == null ||
                authentication.getName().trim().isEmpty()) {

            throw new IllegalStateException(
                    "Authenticated username is invalid."
            );
        }
    }

    // =========================================================
    // ROLE CHECK
    // =========================================================

    private boolean hasRole(
            Authentication authentication,
            String role) {

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority != null &&
                                role.equals(
                                        authority.getAuthority()
                                )
                );
    }

    // =========================================================
    // GET AUTHENTICATED HOD DEPARTMENT
    // =========================================================

    private Long getAuthenticatedHODDepartmentId(
            Authentication authentication) {

        validateAuthentication(authentication);

        if (!hasRole(authentication, "ROLE_HOD")) {
            throw new IllegalStateException(
                    "HOD authorization is required."
            );
        }

        String username =
                authentication.getName().trim();

        Lecturer hod =
                lecturerRepository
                        .findByUser_Username(username)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "HOD lecturer profile not found."
                                )
                        );

        if (hod.getLecturerId() == null) {
            throw new IllegalStateException(
                    "HOD lecturer profile ID is invalid."
            );
        }

        if (hod.getDepartment() == null ||
                hod.getDepartment().getDepartmentId() == null) {

            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }

        return hod.getDepartment().getDepartmentId();
    }

    // =========================================================
    // HOD SPECIALIZATION ACCESS
    // =========================================================

    private void validateHODSpecializationAccess(
            Specialization specialization,
            Long hodDepartmentId) {

        if (specialization == null) {
            throw new IllegalStateException(
                    "Specialization information is invalid."
            );
        }

        if (hodDepartmentId == null) {
            throw new IllegalStateException(
                    "HOD department information is invalid."
            );
        }

        Long specializationDepartmentId =
                getDepartmentId(specialization);

        if (!hodDepartmentId.equals(
                specializationDepartmentId)) {

            throw new IllegalStateException(
                    "You are not authorized to access "
                            + "a specialization outside your department."
            );
        }
    }

    // =========================================================
    // GET DEPARTMENT ID FROM SPECIALIZATION
    // =========================================================

    private Long getDepartmentId(
            Specialization specialization) {

        if (specialization == null) {
            throw new IllegalStateException(
                    "Specialization information is invalid."
            );
        }

        if (specialization.getDepartment() == null ||
                specialization.getDepartment().getDepartmentId() == null) {

            throw new IllegalStateException(
                    "Specialization department information is invalid."
            );
        }

        return specialization.getDepartment()
                .getDepartmentId();
    }

    // =========================================================
    // FILTER BY DEPARTMENT
    // =========================================================

    private List<Specialization> filterByDepartment(
            List<Specialization> specializations,
            Long departmentId) {

        if (specializations == null ||
                specializations.isEmpty()) {

            return new ArrayList<>();
        }

        if (departmentId == null) {
            throw new IllegalStateException(
                    "Department ID is required."
            );
        }

        List<Specialization> filtered =
                new ArrayList<>();

        for (Specialization specialization : specializations) {

            if (specialization == null) {
                continue;
            }

            if (specialization.getDepartment() == null) {
                continue;
            }

            if (specialization.getDepartment()
                    .getDepartmentId() == null) {

                continue;
            }

            if (departmentId.equals(
                    specialization.getDepartment()
                            .getDepartmentId())) {

                filtered.add(specialization);
            }
        }

        return filtered;
    }

    // =========================================================
    // SPECIALIZATION VALIDATION
    // =========================================================

    private void validateSpecializationData(
            Specialization specialization) {

        if (specialization.getSpecializationCode() == null ||
                specialization.getSpecializationCode()
                        .trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Specialization code is required."
            );
        }

        if (specialization.getSpecializationName() == null ||
                specialization.getSpecializationName()
                        .trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Specialization name is required."
            );
        }

        specialization.setSpecializationCode(
                specialization.getSpecializationCode().trim()
        );

        specialization.setSpecializationName(
                specialization.getSpecializationName().trim()
        );
    }
}