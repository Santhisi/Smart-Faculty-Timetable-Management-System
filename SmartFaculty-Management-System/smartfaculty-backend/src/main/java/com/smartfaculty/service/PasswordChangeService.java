package com.smartfaculty.service;

import com.smartfaculty.dto.ChangePasswordRequest;

public interface PasswordChangeService {

    void changePassword(
            ChangePasswordRequest request
    );
}