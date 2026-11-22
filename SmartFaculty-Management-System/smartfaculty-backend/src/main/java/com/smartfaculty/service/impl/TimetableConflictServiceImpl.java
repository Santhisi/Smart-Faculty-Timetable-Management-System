package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Timetable;
import com.smartfaculty.entity.TimeSlot;
import com.smartfaculty.repository.TimetableRepository;
import com.smartfaculty.service.TimetableConflictService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class TimetableConflictServiceImpl
        implements TimetableConflictService {

    @Autowired
    private TimetableRepository timetableRepository;


    // =====================================================
    // CHECK ALL TIMETABLE CONFLICTS
    // =====================================================

    @Override
    public String checkConflict(
            Timetable timetable,
            Long classroomId,
            TimeSlot timeSlot) {

        if (timetable == null) {
            return "Timetable is required.";
        }

        if (timeSlot == null) {
            return "Time slot is required.";
        }

        if (classroomId == null) {
            return "Classroom is required.";
        }


        // =================================================
        // TIME INFORMATION
        // =================================================

        LocalTime newStart =
                timeSlot.getStartTime();

        LocalTime newEnd =
                timeSlot.getEndTime();

        if (newStart == null || newEnd == null) {
            return "Invalid time slot.";
        }

        if (!newStart.isBefore(newEnd)) {
            return "Start time must be before end time.";
        }


        // =================================================
        // 1. CLASSROOM CONFLICT
        // =================================================

        List<Timetable> classroomTimetables =
                timetableRepository
                        .findByClassroom_ClassroomIdAndTimeSlot_Day(
                                classroomId,
                                timeSlot.getDay()
                        );


        for (Timetable existing :
                classroomTimetables) {

            // Do not compare the timetable with itself
            if (existing.getTimetableId()
                    .equals(timetable.getTimetableId())) {

                continue;
            }

            // Ignore inactive timetables
            if (Boolean.FALSE.equals(
                    existing.getActive())) {

                continue;
            }

            TimeSlot existingSlot =
                    existing.getTimeSlot();

            if (existingSlot == null) {
                continue;
            }


            if (isOverlapping(
                    newStart,
                    newEnd,
                    existingSlot.getStartTime(),
                    existingSlot.getEndTime()
            )) {

                String roomCode =
                        existing.getClassroom() != null
                                ? existing.getClassroom()
                                .getRoomCode()
                                : "selected classroom";

                return
                        "Classroom conflict: "
                                + roomCode
                                + " is already occupied on "
                                + timeSlot.getDay()
                                + " from "
                                + existingSlot.getStartTime()
                                + " to "
                                + existingSlot.getEndTime()
                                + ".";
            }
        }


        // =================================================
        // 2. LECTURER CONFLICT
        // =================================================

        if (timetable.getLecturerAllocation() != null &&
                timetable.getLecturerAllocation()
                        .getLecturer() != null) {

            Long lecturerId =
                    timetable
                            .getLecturerAllocation()
                            .getLecturer()
                            .getLecturerId();


            List<Timetable> lecturerTimetables =
                    timetableRepository
                            .findByLecturerAllocation_Lecturer_LecturerIdAndTimeSlot_Day(
                                    lecturerId,
                                    timeSlot.getDay()
                            );


            for (Timetable existing :
                    lecturerTimetables) {

                // Do not compare with itself
                if (existing.getTimetableId()
                        .equals(timetable.getTimetableId())) {

                    continue;
                }

                // Ignore inactive timetables
                if (Boolean.FALSE.equals(
                        existing.getActive())) {

                    continue;
                }

                TimeSlot existingSlot =
                        existing.getTimeSlot();

                if (existingSlot == null) {
                    continue;
                }


                if (isOverlapping(
                        newStart,
                        newEnd,
                        existingSlot.getStartTime(),
                        existingSlot.getEndTime()
                )) {

                    return
                            "Lecturer conflict: the lecturer is already assigned "
                                    + "to another class on "
                                    + timeSlot.getDay()
                                    + " from "
                                    + existingSlot.getStartTime()
                                    + " to "
                                    + existingSlot.getEndTime()
                                    + ".";
                }
            }
        }


        // =================================================
        // NO CONFLICT
        // =================================================

        return null;
    }


    // =====================================================
    // CHECK TIME OVERLAP
    // =====================================================

    private boolean isOverlapping(
            LocalTime start1,
            LocalTime end1,
            LocalTime start2,
            LocalTime end2) {

        if (start1 == null ||
                end1 == null ||
                start2 == null ||
                end2 == null) {

            return false;
        }

        /*
         * Example:
         *
         * Existing: 08:00 - 09:30
         * New:      09:00 - 10:00
         *
         * These overlap.
         */

        return start1.isBefore(end2)
                && start2.isBefore(end1);
    }
}