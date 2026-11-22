package com.smartfaculty.controller;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerNotification;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.service.LecturerNotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturer-notifications")

public class LecturerNotificationController {

    @Autowired
    private LecturerNotificationService notificationService;


    @Autowired
    private LecturerRepository lecturerRepository;


    // =========================================================
    // GET LOGGED-IN LECTURER
    // =========================================================

    private Lecturer getLoggedInLecturer() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String username =
                authentication.getName();

        return lecturerRepository
                .findByUser_Username(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lecturer account not found"
                        )
                );
    }


    // =========================================================
    // CHECK LECTURER OWNERSHIP
    // =========================================================

    private boolean isLecturerOwner(Long lecturerId) {

        Lecturer loggedInLecturer =
                getLoggedInLecturer();

        return loggedInLecturer
                .getLecturerId()
                .equals(lecturerId);
    }


    // =========================================================
    // GET ALL NOTIFICATIONS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @GetMapping("/lecturer/{lecturerId}")
    public ResponseEntity<?> getLecturerNotifications(
            @PathVariable Long lecturerId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN")
                        );


        // ADMIN can view any lecturer's notifications
        if (isAdmin) {

            return ResponseEntity.ok(
                    notificationService
                            .getLecturerNotifications(
                                    lecturerId
                            )
            );
        }


        // Lecturer must own the requested lecturerId
        if (!isLecturerOwner(lecturerId)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not allowed to access another lecturer's notifications."
                    );
        }


        return ResponseEntity.ok(
                notificationService
                        .getLecturerNotifications(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @GetMapping("/lecturer/{lecturerId}/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @PathVariable Long lecturerId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN")
                        );


        // ADMIN can view any lecturer's unread notifications
        if (isAdmin) {

            return ResponseEntity.ok(
                    notificationService
                            .getUnreadNotifications(
                                    lecturerId
                            )
            );
        }


        // Lecturer must own the requested lecturerId
        if (!isLecturerOwner(lecturerId)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not allowed to access another lecturer's notifications."
                    );
        }


        return ResponseEntity.ok(
                notificationService
                        .getUnreadNotifications(
                                lecturerId
                        )
        );
    }


    // =========================================================
    // GET NOTIFICATION BY ID
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getNotificationById(
            @PathVariable Long id) {

        LecturerNotification notification =
                notificationService
                        .getNotificationById(id);


        if (notification == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN")
                        );


        // ADMIN can view any notification
        if (isAdmin) {

            return ResponseEntity.ok(
                    notification
            );
        }


        // =====================================================
        // CHECK NOTIFICATION OWNER
        // =====================================================

        if (notification.getLecturer() == null ||
                !notification
                        .getLecturer()
                        .getLecturerId()
                        .equals(
                                getLoggedInLecturer()
                                        .getLecturerId()
                        )) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not allowed to access another lecturer's notification."
                    );
        }


        return ResponseEntity.ok(
                notification
        );
    }


    // =========================================================
    // MARK AS READ
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long id) {

        LecturerNotification notification =
                notificationService
                        .getNotificationById(id);


        if (notification == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN")
                        );


        // ADMIN can mark any notification as read
        if (!isAdmin) {

            if (notification.getLecturer() == null ||
                    !notification
                            .getLecturer()
                            .getLecturerId()
                            .equals(
                                    getLoggedInLecturer()
                                            .getLecturerId()
                            )) {

                return ResponseEntity
                        .status(403)
                        .body(
                                "You are not allowed to modify another lecturer's notification."
                        );
            }
        }


        LecturerNotification updatedNotification =
                notificationService
                        .markAsRead(id);


        return ResponseEntity.ok(
                updatedNotification
        );
    }


    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @PutMapping("/lecturer/{lecturerId}/read-all")
    public ResponseEntity<?> markAllAsRead(
            @PathVariable Long lecturerId) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN")
                        );


        // =====================================================
        // ADMIN
        // =====================================================

        if (isAdmin) {

            notificationService
                    .markAllAsRead(
                            lecturerId
                    );

            return ResponseEntity.ok(
                    "All lecturer notifications marked as read."
            );
        }


        // =====================================================
        // LECTURER OWNERSHIP CHECK
        // =====================================================

        if (!isLecturerOwner(lecturerId)) {

            return ResponseEntity
                    .status(403)
                    .body(
                            "You are not allowed to modify another lecturer's notifications."
                    );
        }


        notificationService
                .markAllAsRead(
                        lecturerId
                );


        return ResponseEntity.ok(
                "All lecturer notifications marked as read."
        );
    }


    // =========================================================
    // DELETE NOTIFICATION
    // =========================================================

    @PreAuthorize("hasAnyRole('ADMIN','LECTURER','HOD')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id) {

        LecturerNotification notification =
                notificationService
                        .getNotificationById(id);


        if (notification == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();


        boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(
                                authority ->
                                        authority
                                                .getAuthority()
                                                .equals("ROLE_ADMIN")
                        );


        // =====================================================
        // LECTURER OWNERSHIP CHECK
        // =====================================================

        if (!isAdmin) {

            if (notification.getLecturer() == null ||
                    !notification
                            .getLecturer()
                            .getLecturerId()
                            .equals(
                                    getLoggedInLecturer()
                                            .getLecturerId()
                            )) {

                return ResponseEntity
                        .status(403)
                        .body(
                                "You are not allowed to delete another lecturer's notification."
                        );
            }
        }


        notificationService
                .deleteNotification(
                        id
                );


        return ResponseEntity.ok(
                "Lecturer notification deleted successfully."
        );
    }
}

