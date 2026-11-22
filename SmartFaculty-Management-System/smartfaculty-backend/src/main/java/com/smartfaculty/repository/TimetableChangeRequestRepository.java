package com.smartfaculty.repository;

import com.smartfaculty.entity.TimetableChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableChangeRequestRepository
        extends JpaRepository<TimetableChangeRequest, Long> {

    // Get all requests
    List<TimetableChangeRequest>
    findAllByOrderByCreatedAtDesc();

    // Get requests submitted by one lecturer
    List<TimetableChangeRequest>
    findByLecturer_LecturerIdOrderByCreatedAtDesc(
            Long lecturerId
    );

    // Get requests by status
    List<TimetableChangeRequest>
    findByStatusOrderByCreatedAtDesc(
            String status
    );
    // Check whether a timetable entry has linked change requests
    boolean existsByTimetable_TimetableId(Long timetableId);
}