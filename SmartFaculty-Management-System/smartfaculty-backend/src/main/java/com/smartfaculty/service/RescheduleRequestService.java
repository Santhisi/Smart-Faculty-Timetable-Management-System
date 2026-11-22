package com.smartfaculty.service;

import com.smartfaculty.entity.RescheduleRequest;

import java.util.List;

public interface RescheduleRequestService {

    // Create request
    RescheduleRequest createRequest(
            RescheduleRequest request
    );


    // Get all requests
    List<RescheduleRequest> getAllRequests();


    // Get lecturer requests
    List<RescheduleRequest> getLecturerRequests(
            Long lecturerId
    );


    // Get request by ID
    RescheduleRequest getRequestById(
            Long requestId
    );


    // Get requests by status
    List<RescheduleRequest> getRequestsByStatus(
            String status
    );


    // Approve request
    RescheduleRequest approveRequest(
            Long requestId,
            String adminComment
    );


    // Reject request
    RescheduleRequest rejectRequest(
            Long requestId,
            String adminComment
    );


    // Delete request
    void deleteRequest(
            Long requestId
    );
}