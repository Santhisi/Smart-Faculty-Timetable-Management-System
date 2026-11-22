package com.smartfaculty.service;

import com.smartfaculty.entity.Classroom;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface ClassroomService {

    Classroom saveClassroom(
            Classroom classroom,
            Authentication authentication
    );

    List<Classroom> getAllClassrooms(
            Authentication authentication
    );

    Classroom getClassroomById(
            Long id,
            Authentication authentication
    );

    Classroom updateClassroom(
            Long id,
            Classroom classroom,
            Authentication authentication
    );

    void deleteClassroom(
            Long id,
            Authentication authentication
    );
}