package com.lms.identity.repository;

import org.springframework.data.repository.CrudRepository;
import com.lms.identity.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUserId(String userId);
}
