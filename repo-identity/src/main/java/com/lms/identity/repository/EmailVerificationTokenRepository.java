package com.lms.identity.repository;

import org.springframework.data.repository.CrudRepository;
import com.lms.identity.entity.EmailVerificationToken;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends CrudRepository<EmailVerificationToken, String> {
    Optional<EmailVerificationToken> findByToken(String token);
    void deleteByUserId(String userId);
}
