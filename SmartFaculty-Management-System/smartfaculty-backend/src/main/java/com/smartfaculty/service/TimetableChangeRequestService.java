package com.smartfaculty.service;

import com.smartfaculty.entity.TimetableChangeRequest;

import java.util.List;

public interface TimetableChangeRequestService {

    TimetableChangeRequest createRequest(
            TimetableChangeRequest request
    );


    List<TimetableChangeRequest> getAllRequests();


    List<TimetableChangeRequest> getLecturerRequests(
            Long lecturerId
    );


    TimetableChangeRequest getRequestById(
            Long requestId
    );


    List<TimetableChangeRequest> getRequestsByStatus(
            String status
    );


    TimetableChangeRequest approveRequest(
            Long requestId,
            String adminResponse
    );


    TimetableChangeRequest rejectRequest(
            Long requestId,
            String adminResponse
    );

    TimetableChangeRequest updateRequest(
            Long requestId,
            TimetableChangeRequest updatedRequest
    );


    void deleteRequest(
            Long requestId
    );
}