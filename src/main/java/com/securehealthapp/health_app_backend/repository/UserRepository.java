package com.securehealthapp.health_app_backend.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.securehealthapp.health_app_backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  Boolean existsByEmail(String email);
  Optional<User> findByUsername(String username);
  Boolean existsByUsername(String username);
}
