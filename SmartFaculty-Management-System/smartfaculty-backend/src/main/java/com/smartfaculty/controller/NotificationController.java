package com.smartfaculty.controller;

import com.smartfaculty.entity.Notification;
import com.smartfaculty.service.NotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


    // =========================================================
    // STUDENT NOTIFICATIONS
    // =========================================================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Notification>>
    getStudentNotifications(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                notificationService
                        .getStudentNotifications(
                                studentId
                        )
        );
    }


    // =========================================================
    // STUDENT UNREAD NOTIFICATIONS
    // =========================================================

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student/{studentId}/unread")
    public ResponseEntity<List<Notification>>
    getUnreadStudentNotifications(
            @PathVariable Long studentId) {

        return ResponseEntity.ok(
                notificationService
                        .getUnreadNotifications(
                                studentId
                        )
        );
    }


    // =========================================================
    // MARK ALL STUDENT NOTIFICATIONS AS READ
    // =========================================================

    @PreAuthorize("hasRole('STUDENT')")
    @PutMapping("/student/{studentId}/read-all")
    public ResponseEntity<String>
    markAllStudentNotificationsAsRead(
            @PathVariable Long studentId) {

        notificationService
                .markAllAsRead(
                        studentId
                );

        return ResponseEntity.ok(
                "All student notifications marked as read."
        );
    }


    // =========================================================
    // LECTURER NOTIFICATIONS
    // =========================================================

    @PreAuthorize("hasRole('LECTURER')")
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<List<Notification>>
    getLecturerNotifications(
            @PathVariable Long lecturerId) {

        return ResponseEntity.ok(
                notificationService
                        .getLecturerNotifications(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // LECTURER UNREAD NOTIFICATIONS
    // =========================================================

    @PreAuthorize("hasRole('LECTURER')")
    @GetMapping("/lecturer/{lecturerId}/unread")
    public ResponseEntity<List<Notification>>
    getUnreadLecturerNotifications(
            @PathVariable Long lecturerId) {

        return ResponseEntity.ok(
                notificationService
                        .getUnreadLecturerNotifications(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // MARK ALL LECTURER NOTIFICATIONS AS READ
    // =========================================================

    @PreAuthorize("hasRole('LECTURER')")
    @PutMapping("/lecturer/{lecturerId}/read-all")
    public ResponseEntity<String>
    markAllLecturerNotificationsAsRead(
            @PathVariable Long lecturerId) {

        notificationService
                .markAllLecturerNotificationsAsRead(
                        lecturerId
                );

        return ResponseEntity.ok(
                "All lecturer notifications marked as read."
        );
    }


    // =========================================================
    // HOD NOTIFICATIONS
    // =========================================================
    //
    // HOD is a lecturer with HOD role.
    //
    // The service layer verifies that lecturerId belongs
    // to the authenticated HOD.
    //
    // =========================================================

    @PreAuthorize("hasRole('HOD')")
    @GetMapping("/hod/{lecturerId}")
    public ResponseEntity<List<Notification>>
    getHODNotifications(
            @PathVariable Long lecturerId) {

        return ResponseEntity.ok(
                notificationService
                        .getHODNotifications(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // HOD UNREAD NOTIFICATIONS
    // =========================================================

    @PreAuthorize("hasRole('HOD')")
    @GetMapping("/hod/{lecturerId}/unread")
    public ResponseEntity<List<Notification>>
    getUnreadHODNotifications(
            @PathVariable Long lecturerId) {

        return ResponseEntity.ok(
                notificationService
                        .getUnreadHODNotifications(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // MARK ALL HOD NOTIFICATIONS AS READ
    // =========================================================

    @PreAuthorize("hasRole('HOD')")
    @PutMapping("/hod/{lecturerId}/read-all")
    public ResponseEntity<String>
    markAllHODNotificationsAsRead(
            @PathVariable Long lecturerId) {

        notificationService
                .markAllHODNotificationsAsRead(
                        lecturerId
                );

        return ResponseEntity.ok(
                "All HOD notifications marked as read."
        );
    }


    // =========================================================
    // GET NOTIFICATION BY ID
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','HOD','LECTURER')")
    @GetMapping("/{id}")
    public ResponseEntity<Notification>
    getNotificationById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificationService
                        .getNotificationById(
                                id
                        )
        );
    }


    // =========================================================
    // MARK ONE NOTIFICATION AS READ
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','HOD','LECTURER')")
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification>
    markAsRead(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                notificationService
                        .markAsRead(
                                id
                        )
        );
    }


    // =========================================================
    // DELETE NOTIFICATION
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','STUDENT','HOD','LECTURER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String>
    deleteNotification(
            @PathVariable Long id) {

        notificationService
                .deleteNotification(
                        id
                );

        return ResponseEntity.ok(
                "Notification deleted successfully."
        );
    }
}