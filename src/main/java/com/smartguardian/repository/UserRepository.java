package com.smartguardian.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.smartguardian.model.User;


/* ===================== USER REPOSITORY ===================== */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /* ===================== AUTH ===================== */
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);

    /* ===================== USER LOOKUP ===================== */
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
}
