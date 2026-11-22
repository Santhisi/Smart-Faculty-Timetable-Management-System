package com.smartfaculty.service;

import com.smartfaculty.entity.User;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    User saveUser(User user);

    List<User> getAllUsers();

    User getUserById(Long id);

    User updateUser(Long id, User user);

    void deleteUser(
            Long id,
            Authentication authentication
    );
}