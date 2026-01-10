package com.lms.identity.repository;

import com.lms.identity.entity.OtpVerificationToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationTokenRepository extends CrudRepository<OtpVerificationToken, String> {

    Optional<OtpVerificationToken> findByOtp(String otp);

    void deleteByUserId(String userId);

    Optional<OtpVerificationToken> findByUserId(String userId);
}
