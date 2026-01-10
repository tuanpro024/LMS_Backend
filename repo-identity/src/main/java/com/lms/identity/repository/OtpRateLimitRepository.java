package com.lms.identity.repository;

import com.lms.identity.entity.OtpRateLimit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRateLimitRepository extends CrudRepository<OtpRateLimit, String> {

    Optional<OtpRateLimit> findByEmail(String email);
}
