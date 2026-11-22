package com.smartfaculty.repository;

import com.smartfaculty.entity.RescheduleRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RescheduleRequestRepository
        extends JpaRepository<RescheduleRequest, Long> {

    // Get all requests
    List<RescheduleRequest>
    findAllByOrderByRequestedAtDesc();


    // Get requests submitted by one lecturer
    List<RescheduleRequest>
    findByLecturer_LecturerIdOrderByRequestedAtDesc(
            Long lecturerId
    );


    // Get requests by status
    List<RescheduleRequest>
    findByStatusOrderByRequestedAtDesc(
            String status
    );
}