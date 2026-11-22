package com.smartfaculty.repository;

import com.smartfaculty.entity.AcademicYear;
import com.smartfaculty.entity.DayOfWeek;
import com.smartfaculty.entity.Semester;
import com.smartfaculty.entity.Timetable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository
        extends JpaRepository<Timetable, Long> {


    // =========================================================
    // YEAR + SEMESTER
    // =========================================================

    List<Timetable> findByYearLevelAndSemester(
            AcademicYear yearLevel,
            Semester semester
    );

    // =========================================================
    // ACTIVE YEAR + SEMESTER
    // =========================================================

    List<Timetable> findByYearLevelAndSemesterAndActiveTrue(
            AcademicYear yearLevel,
            Semester semester
    );


    // =========================================================
    // SPECIALIZATION
    // =========================================================

    List<Timetable>
    findBySpecialization_SpecializationId(
            Long specializationId
    );


    // =========================================================
    // YEAR + SEMESTER + SPECIALIZATION
    // =========================================================

    List<Timetable>
    findByYearLevelAndSemesterAndSpecialization_SpecializationIdIn(
            AcademicYear yearLevel,
            Semester semester,
            List<Long> specializationIds
    );


    // =========================================================
    // LECTURER
    // =========================================================

    List<Timetable>
    findByLecturerAllocation_Lecturer_LecturerId(
            Long lecturerId
    );


    // =========================================================
    // CLASSROOM + TIME SLOT
    // =========================================================

    boolean
    existsByClassroom_ClassroomIdAndTimeSlot_TimeSlotId(
            Long classroomId,
            Long timeSlotId
    );


    // =========================================================
    // CLASSROOM + TIME SLOT
    // UPDATE
    // =========================================================

    boolean
    existsByClassroom_ClassroomIdAndTimeSlot_TimeSlotIdAndTimetableIdNot(
            Long classroomId,
            Long timeSlotId,
            Long timetableId
    );


    // =========================================================
    // CLASSROOM + DAY
    // =========================================================

    List<Timetable>
    findByClassroom_ClassroomIdAndTimeSlot_Day(
            Long classroomId,
            DayOfWeek day
    );


    // =========================================================
    // LECTURER + DAY
    // =========================================================

    List<Timetable>
    findByLecturerAllocation_Lecturer_LecturerIdAndTimeSlot_Day(
            Long lecturerId,
            DayOfWeek day
    );


    // =========================================================
    // LECTURER + TIME SLOT
    // =========================================================

    boolean
    existsByLecturerAllocation_Lecturer_LecturerIdAndTimeSlot_TimeSlotId(
            Long lecturerId,
            Long timeSlotId
    );


    // =========================================================
    // LECTURER + TIME SLOT
    // UPDATE
    // =========================================================

    boolean
    existsByLecturerAllocation_Lecturer_LecturerIdAndTimeSlot_TimeSlotIdAndTimetableIdNot(
            Long lecturerId,
            Long timeSlotId,
            Long timetableId
    );
}