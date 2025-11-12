package com.smarttask.smarttask_backend.repository;

import com.smarttask.smarttask_backend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);
    void deleteByUser_Id(UUID userId);
}
