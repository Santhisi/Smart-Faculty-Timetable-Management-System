package com.smartfaculty.service.impl;

import com.smartfaculty.entity.Lecturer;
import com.smartfaculty.entity.LecturerNotification;
import com.smartfaculty.repository.LecturerNotificationRepository;
import com.smartfaculty.repository.LecturerRepository;
import com.smartfaculty.service.LecturerNotificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LecturerNotificationServiceImpl
        implements LecturerNotificationService {

    @Autowired
    private LecturerNotificationRepository notificationRepository;

    @Autowired
    private LecturerRepository lecturerRepository;


    // =========================================================
    // SAVE NOTIFICATION
    // =========================================================

    @Override
    public LecturerNotification saveNotification(
            LecturerNotification notification) {

        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification cannot be null"
            );
        }

        validateAuthenticatedUser();

        /*
         * A lecturer notification belongs to exactly one lecturer.
         * Non-admin users cannot save a notification for another
         * lecturer. This protects the lecturer_id relationship from
         * IDOR-style manipulation.
         *
         * Admin is allowed to create/update notifications for any
         * lecturer.
         */
        validateNotificationOwnership(notification);

        if (notification.getLecturer() == null ||
                notification.getLecturer().getLecturerId() == null) {

            throw new IllegalArgumentException(
                    "Lecturer is required for a notification"
            );
        }

        // Reload the lecturer from the database so the notification
        // never relies on a client-supplied detached lecturer object.
        Lecturer lecturer =
                lecturerRepository
                        .findById(
                                notification
                                        .getLecturer()
                                        .getLecturerId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Lecturer account not found"
                                )
                        );

        notification.setLecturer(lecturer);

        if (notification.getTitle() == null ||
                notification.getTitle().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Notification title is required"
            );
        }

        if (notification.getMessage() == null ||
                notification.getMessage().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Notification message is required"
            );
        }

        if (notification.getNotificationType() == null ||
                notification.getNotificationType().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Notification type is required"
            );
        }

        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(
                    LocalDateTime.now()
            );
        }

        if (notification.getIsRead() == null) {
            notification.setIsRead(false);
        }

        return notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // CREATE NOTIFICATION
    // =========================================================

    @Override
    public LecturerNotification createNotification(
            Long lecturerId,
            String title,
            String message,
            String notificationType) {

        validateAuthenticatedUser();

        if (lecturerId == null) {
            throw new IllegalArgumentException(
                    "Lecturer ID is required"
            );
        }

        /*
         * Prevent a lecturer/HOD from using another lecturer's ID
         * when creating a notification.
         *
         * Admin can create notifications for any lecturer.
         */
        validateLecturerTargetAccess(lecturerId);

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElse(null);

        if (lecturer == null) {
            return null;
        }

        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Notification title is required"
            );
        }

        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Notification message is required"
            );
        }

        if (notificationType == null ||
                notificationType.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Notification type is required"
            );
        }

        LecturerNotification notification =
                new LecturerNotification();

        notification.setLecturer(lecturer);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(
                notificationType
        );
        notification.setIsRead(false);
        notification.setCreatedAt(
                LocalDateTime.now()
        );

        return notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // GET ALL LECTURER NOTIFICATIONS
    // =========================================================

    @Override
    public List<LecturerNotification>
    getLecturerNotifications(Long lecturerId) {

        validateAuthenticatedUser();
        validateLecturerTargetAccess(lecturerId);

        return notificationRepository
                .findByLecturer_LecturerIdOrderByCreatedAtDesc(
                        lecturerId
                );
    }


    // =========================================================
    // GET UNREAD NOTIFICATIONS
    // =========================================================

    @Override
    public List<LecturerNotification>
    getUnreadNotifications(Long lecturerId) {

        validateAuthenticatedUser();
        validateLecturerTargetAccess(lecturerId);

        return notificationRepository
                .findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
                        lecturerId
                );
    }


    // =========================================================
    // GET NOTIFICATION BY ID
    // =========================================================

    @Override
    public LecturerNotification
    getNotificationById(Long notificationId) {

        validateAuthenticatedUser();

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "Notification ID is required"
            );
        }

        LecturerNotification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElse(null);

        if (notification == null) {
            return null;
        }

        /*
         * IDOR protection:
         * The notification ID alone is never sufficient.
         * The notification's lecturer must belong to the
         * authenticated user unless the user is ADMIN.
         */
        validateNotificationOwnership(notification);

        return notification;
    }


    // =========================================================
    // MARK ONE AS READ
    // =========================================================

    @Override
    public LecturerNotification
    markAsRead(Long notificationId) {

        validateAuthenticatedUser();

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "Notification ID is required"
            );
        }

        LecturerNotification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElse(null);

        if (notification == null) {
            return null;
        }

        validateNotificationOwnership(notification);

        notification.setIsRead(true);

        return notificationRepository.save(
                notification
        );
    }


    // =========================================================
    // MARK ALL AS READ
    // =========================================================

    @Override
    public void markAllAsRead(Long lecturerId) {

        validateAuthenticatedUser();
        validateLecturerTargetAccess(lecturerId);

        List<LecturerNotification> notifications =
                notificationRepository
                        .findByLecturer_LecturerIdAndIsReadFalseOrderByCreatedAtDesc(
                                lecturerId
                        );

        for (LecturerNotification notification :
                notifications) {

            notification.setIsRead(true);
        }

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(
                    notifications
            );
        }
    }


    // =========================================================
    // DELETE NOTIFICATION
    // =========================================================

    @Override
    public void deleteNotification(
            Long notificationId) {

        validateAuthenticatedUser();

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "Notification ID is required"
            );
        }

        LecturerNotification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElse(null);

        if (notification == null) {
            return;
        }

        /*
         * Service-level ownership protection is required even
         * though the controller also checks ownership.
         */
        validateNotificationOwnership(notification);

        notificationRepository.delete(
                notification
        );
    }


    // =========================================================
    // AUTHENTICATION HELPER
    // =========================================================

    private Authentication getCurrentAuthentication() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null) {

            throw new SecurityException(
                    "Authentication is required"
            );
        }

        return authentication;
    }


    // =========================================================
    // AUTHENTICATION VALIDATION
    // =========================================================

    private void validateAuthenticatedUser() {

        Authentication authentication =
                getCurrentAuthentication();

        String authority =
                authentication
                        .getAuthorities()
                        .stream()
                        .map(a -> a.getAuthority())
                        .filter(a ->
                                "ROLE_ADMIN".equals(a) ||
                                        "ROLE_HOD".equals(a) ||
                                        "ROLE_LECTURER".equals(a))
                        .findFirst()
                        .orElse(null);

        if (authority == null) {
            throw new SecurityException(
                    "You are not authorized to access lecturer notifications"
            );
        }
    }


    // =========================================================
    // CHECK ADMIN
    // =========================================================

    private boolean isAdmin(
            Authentication authentication) {

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(
                        authority ->
                                "ROLE_ADMIN"
                                        .equals(
                                                authority
                                                        .getAuthority()
                                        )
                );
    }


    // =========================================================
    // GET AUTHENTICATED LECTURER
    // =========================================================

    private Lecturer getAuthenticatedLecturer() {

        Authentication authentication =
                getCurrentAuthentication();

        return lecturerRepository
                .findByUser_Username(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new SecurityException(
                                "Lecturer account not found"
                        )
                );
    }


    // =========================================================
    // LECTURER TARGET ACCESS
    // =========================================================

    private void validateLecturerTargetAccess(
            Long lecturerId) {

        if (lecturerId == null) {
            throw new IllegalArgumentException(
                    "Lecturer ID is required"
            );
        }

        Authentication authentication =
                getCurrentAuthentication();

        // ADMIN can access any lecturer notification.
        if (isAdmin(authentication)) {
            return;
        }

        /*
         * HOD is also a lecturer in this system.
         * Therefore HOD notification access is restricted to
         * the HOD's own lecturer account, not every lecturer
         * in the department.
         */
        Lecturer authenticatedLecturer =
                getAuthenticatedLecturer();

        if (!authenticatedLecturer
                .getLecturerId()
                .equals(lecturerId)) {

            throw new SecurityException(
                    "You are not allowed to access another lecturer's notifications"
            );
        }
    }


    // =========================================================
    // NOTIFICATION OWNERSHIP / IDOR PROTECTION
    // =========================================================

    private void validateNotificationOwnership(
            LecturerNotification notification) {

        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification is required"
            );
        }

        if (notification.getLecturer() == null ||
                notification
                        .getLecturer()
                        .getLecturerId() == null) {

            throw new SecurityException(
                    "Notification owner could not be determined"
            );
        }

        Authentication authentication =
                getCurrentAuthentication();

        // ADMIN can access any lecturer notification.
        if (isAdmin(authentication)) {
            return;
        }

        Lecturer authenticatedLecturer =
                getAuthenticatedLecturer();

        Long ownerId =
                notification
                        .getLecturer()
                        .getLecturerId();

        if (!authenticatedLecturer
                .getLecturerId()
                .equals(ownerId)) {

            throw new SecurityException(
                    "You are not allowed to access or modify another lecturer's notification"
            );
        }
    }
}