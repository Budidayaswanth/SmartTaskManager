package com.smarttask.smarttask_backend.repository;

import com.smarttask.smarttask_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsernameAndDeletedFalse(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}