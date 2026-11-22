package com.smartfaculty.service;

import com.smartfaculty.entity.TimetableEntry;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TimetableEntryService {

    TimetableEntry saveTimetableEntry(
            TimetableEntry timetableEntry,
            Authentication authentication
    );

    List<TimetableEntry> getAllTimetableEntries(
            Authentication authentication
    );

    TimetableEntry getTimetableEntryById(
            Long id,
            Authentication authentication
    );

    List<TimetableEntry> getTimetableByYearAndSemester(
            Integer academicYear,
            Integer semester,
            Authentication authentication
    );

    List<TimetableEntry> getTimetableByLecturer(
            Long lecturerId,
            Authentication authentication
    );

    List<TimetableEntry> getTimetableBySubject(
            Long subjectId,
            Authentication authentication
    );

    TimetableEntry updateTimetableEntry(
            Long id,
            TimetableEntry timetableEntry,
            Authentication authentication
    );

    void deleteTimetableEntry(
            Long id,
            Authentication authentication
    );
}