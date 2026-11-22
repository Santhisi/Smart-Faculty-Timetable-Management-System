package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Classroom;
import com.smartfaculty.repository.ClassroomRepository;
import com.smartfaculty.service.ClassroomService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClassroomServiceImpl
        implements ClassroomService {

    @Autowired
    private ClassroomRepository classroomRepository;


    // =====================================================
    // CREATE CLASSROOM
    // =====================================================

    @Override
    @Transactional
    public Classroom saveClassroom(
            Classroom classroom,
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        if (classroom == null) {
            throw new IllegalArgumentException(
                    "Classroom data cannot be null."
            );
        }

        validateClassroom(classroom);

        return classroomRepository.save(classroom);
    }


    // =====================================================
    // GET ALL CLASSROOMS
    // ADMIN + HOD
    // =====================================================

    @Override
    public List<Classroom> getAllClassrooms(
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        return classroomRepository.findAll();
    }


    // =====================================================
    // GET CLASSROOM BY ID
    // ADMIN + HOD
    // =====================================================

    @Override
    public Classroom getClassroomById(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Classroom ID is required."
            );
        }

        return classroomRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Classroom not found with id: " + id
                        )
                );
    }


    // =====================================================
    // DELETE / DEACTIVATE CLASSROOM
    // ADMIN + HOD
    // =====================================================

    @Override
    @Transactional
    public void deleteClassroom(
            Long id,
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Classroom ID is required."
            );
        }

        Classroom existingClassroom =
                classroomRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Classroom not found with id: "
                                                + id
                                )
                        );

        /*
         * Deactivate instead of physically deleting.
         * This preserves the existing system behavior.
         */
        existingClassroom.setActive(false);

        classroomRepository.save(existingClassroom);
    }


    // =====================================================
    // UPDATE CLASSROOM
    // =====================================================

    @Transactional
    public Classroom updateClassroom(
            Long id,
            Classroom classroom,
            Authentication authentication) {

        validateAuthentication(authentication);

        validateAdminOrHOD(authentication);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Classroom ID is required."
            );
        }

        if (classroom == null) {
            throw new IllegalArgumentException(
                    "Classroom data cannot be null."
            );
        }

        Classroom existingClassroom =
                classroomRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Classroom not found with id: "
                                                + id
                                )
                        );

        validateClassroom(classroom);

        existingClassroom.setRoomCode(
                classroom.getRoomCode().trim()
        );

        existingClassroom.setRoomName(
                classroom.getRoomName().trim()
        );

        existingClassroom.setCapacity(
                classroom.getCapacity()
        );

        existingClassroom.setRoomType(
                classroom.getRoomType()
        );

        if (classroom.getActive() != null) {
            existingClassroom.setActive(
                    classroom.getActive()
            );
        }

        return classroomRepository.save(
                existingClassroom
        );
    }


    // =====================================================
    // AUTHENTICATION VALIDATION
    // =====================================================

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


    // =====================================================
    // ADMIN / HOD AUTHORIZATION
    // =====================================================

    private void validateAdminOrHOD(
            Authentication authentication) {

        boolean isAdmin =
                hasRole(authentication, "ROLE_ADMIN");

        boolean isHOD =
                hasRole(authentication, "ROLE_HOD");

        if (!isAdmin && !isHOD) {
            throw new IllegalStateException(
                    "Administrator or HOD authorization is required."
            );
        }
    }


    // =====================================================
    // ROLE CHECK
    // =====================================================

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


    // =====================================================
    // CLASSROOM VALIDATION
    // =====================================================

    private void validateClassroom(
            Classroom classroom) {

        if (classroom.getRoomCode() == null ||
                classroom.getRoomCode()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Room code is required."
            );
        }

        if (classroom.getRoomName() == null ||
                classroom.getRoomName()
                        .trim()
                        .isEmpty()) {

            throw new IllegalArgumentException(
                    "Room name is required."
            );
        }

        if (classroom.getCapacity() == null ||
                classroom.getCapacity() <= 0) {

            throw new IllegalArgumentException(
                    "Classroom capacity must be greater than zero."
            );
        }

        if (classroom.getRoomType() == null) {

            throw new IllegalArgumentException(
                    "Room type is required."
            );
        }

        classroom.setRoomCode(
                classroom.getRoomCode().trim()
        );

        classroom.setRoomName(
                classroom.getRoomName().trim()
        );

        if (classroom.getActive() == null) {
            classroom.setActive(true);
        }
    }
}
