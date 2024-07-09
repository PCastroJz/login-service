package com.backend.sistembjc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.sistembjc.models.ConfirmationToken;
import java.util.Optional;


public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long>{
    Optional<ConfirmationToken> findByToken(String token);
}
