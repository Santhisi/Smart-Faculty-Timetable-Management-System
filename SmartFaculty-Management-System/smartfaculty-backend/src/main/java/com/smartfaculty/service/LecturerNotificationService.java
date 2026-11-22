package com.smartfaculty.service;

import com.smartfaculty.entity.LecturerNotification;

import java.util.List;

public interface LecturerNotificationService {

    LecturerNotification saveNotification(
            LecturerNotification notification
    );

    List<LecturerNotification>
    getLecturerNotifications(
            Long lecturerId
    );

    List<LecturerNotification>
    getUnreadNotifications(
            Long lecturerId
    );

    LecturerNotification getNotificationById(
            Long notificationId
    );

    LecturerNotification markAsRead(
            Long notificationId
    );

    void markAllAsRead(
            Long lecturerId
    );

    void deleteNotification(
            Long notificationId
    );

    LecturerNotification createNotification(
            Long lecturerId,
            String title,
            String message,
            String notificationType
    );
}