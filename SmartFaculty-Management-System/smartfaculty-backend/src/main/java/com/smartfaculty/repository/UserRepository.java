package com.smartfaculty.repository;

import com.smartfaculty.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // =====================================================
    // FIND USER BY USERNAME
    // =====================================================

    Optional<User> findByUsername(String username);


    // =====================================================
    // CHECK USERNAME
    // =====================================================

    boolean existsByUsername(String username);


    // =====================================================
    // FIND USERS BY ROLE
    // =====================================================

    List<User> findByRole(
            com.smartfaculty.enums.UserRole role
    );
}