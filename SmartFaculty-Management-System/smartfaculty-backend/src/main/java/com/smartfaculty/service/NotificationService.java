package com.smartfaculty.service;

import com.smartfaculty.entity.Notification;

import java.util.List;

public interface NotificationService {

    // ==========================================
    // SAVE NOTIFICATION
    // ==========================================

    Notification saveNotification(
            Notification notification
    );


    // ==========================================
    // STUDENT
    // ==========================================

    List<Notification> getStudentNotifications(
            Long studentId
    );

    List<Notification> getUnreadNotifications(
            Long studentId
    );

    void markAllAsRead(
            Long studentId
    );


    // ==========================================
    // LECTURER
    // ==========================================

    List<Notification> getLecturerNotifications(
            Long lecturerId
    );

    List<Notification> getUnreadLecturerNotifications(
            Long lecturerId
    );

    void markAllLecturerNotificationsAsRead(
            Long lecturerId
    );


    // ==========================================
    // HOD
    // ==========================================

    List<Notification> getHODNotifications(
            Long lecturerId
    );

    List<Notification> getUnreadHODNotifications(
            Long lecturerId
    );

    void markAllHODNotificationsAsRead(
            Long lecturerId
    );

    Notification notifyHOD(
            Long lecturerId,
            String title,
            String message,
            String notificationType
    );


    // ==========================================
    // COMMON
    // ==========================================

    Notification getNotificationById(
            Long notificationId
    );

    Notification markAsRead(
            Long notificationId
    );

    void deleteNotification(
            Long notificationId
    );


    // ==========================================
    // STUDENT SUBJECT NOTIFICATION
    // ==========================================

    List<Notification> notifyAllStudentsForSubject(
            Long subjectId,
            String title,
            String message,
            String notificationType
    );


    // ==========================================
    // LECTURER NOTIFICATION
    // ==========================================

    Notification notifyLecturer(
            Long lecturerId,
            String title,
            String message,
            String notificationType
    );
}