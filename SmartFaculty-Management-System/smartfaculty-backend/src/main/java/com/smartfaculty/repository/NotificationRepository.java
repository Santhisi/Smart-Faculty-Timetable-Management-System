package com.smartfaculty.repository;

import com.smartfaculty.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    // ==========================================
    // STUDENT NOTIFICATIONS
    // ==========================================

    List<Notification>
    findByStudent_StudentIdOrderByCreatedAtDesc(
            Long studentId
    );


    // Delete all notifications belonging to a student
    void deleteByStudent_StudentId(Long studentId);

    List<Notification>
    findByStudent_StudentIdAndIsReadFalseOrderByCreatedAtDesc(
            Long studentId
    );


    // ==========================================
    // LECTURER NOTIFICATIONS
    // ==========================================

    List<Notification>
    findByLecturer_LecturerIdOrderByCreatedAtDesc(
            Long lecturerId
    );

    List<Notification>
    findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
            Long lecturerId
    );
}