package com.smartfaculty.repository;

import com.smartfaculty.entity.LecturerNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LecturerNotificationRepository
        extends JpaRepository<LecturerNotification, Long> {

    List<LecturerNotification>
    findByLecturer_LecturerIdOrderByCreatedAtDesc(
            Long lecturerId
    );

    List<LecturerNotification>
    findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
            Long lecturerId
    );
}