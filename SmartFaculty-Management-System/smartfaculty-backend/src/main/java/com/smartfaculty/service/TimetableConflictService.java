package com.smartfaculty.service;

import com.smartfaculty.entity.Timetable;
import com.smartfaculty.entity.TimeSlot;

public interface TimetableConflictService {

    String checkConflict(
            Timetable timetable,
            Long classroomId,
            TimeSlot timeSlot
    );
}