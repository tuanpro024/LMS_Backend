package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.entity.OtpRateLimit;
import com.lms.identity.entity.OtpVerificationToken;
import com.lms.identity.entity.User;
import com.lms.identity.repository.OtpRateLimitRepository;
import com.lms.identity.repository.OtpVerificationTokenRepository;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.service.EmailService;
import com.lms.identity.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationTokenRepository otpTokenRepository;
    private final OtpRateLimitRepository rateLimitRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RATE_LIMIT_WINDOW = Duration.ofMinutes(15);
    private static final int MAX_REQUESTS_PER_WINDOW = 3;
    private static final Random random = new Random();

    @Transactional
    @Override
    public String generateAndSendOtp(User user) {
        // Check rate limiting
        checkRateLimit(user.getEmail());

        // Delete old OTP if exists
        otpTokenRepository.deleteByUserId(user.getId());

        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1000000));

        // Calculate expiration
        Instant expiresAt = Instant.now().plus(OTP_TTL);
        long ttlSeconds = Math.max(1, Duration.between(Instant.now(), expiresAt).getSeconds());

        // Save OTP token
        OtpVerificationToken token = OtpVerificationToken.builder()
                .id(UUID.randomUUID().toString())
                .otp(otp)
                .userId(user.getId())
                .expiresAt(expiresAt)
                .used(false)
                .ttlSeconds(ttlSeconds)
                .build();
        otpTokenRepository.save(token);

        // Send OTP email
        String userName = user.getFullName() != null ? user.getFullName() : user.getEmail();
        emailService.sendOtpEmail(user.getEmail(), userName, otp);

        log.info("OTP sent to user: {}", user.getEmail());
        return otp;
    }

    @Transactional
    @Override
    public User verifyOtp(String email, String otp) {
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.E238, "User not found"));

        // Find OTP token
        OtpVerificationToken token = otpTokenRepository.findByOtp(otp)
                .orElseThrow(() -> new ApiException(ErrorCode.E241, "The OTP is invalid or has expired."));

        // Verify token belongs to user
        if (!token.getUserId().equals(user.getId())) {
            throw new ApiException(ErrorCode.E241, "OTP is invalid for the given email");
        }

        // Check if used
        if (token.isUsed()) {
            throw new ApiException(ErrorCode.E241, "OTP has already been used");
        }

        // Check if expired
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(ErrorCode.E241, "OTP has expired");
        }

        // Mark as used
        token.setUsed(true);
        otpTokenRepository.save(token);

        // Set email verified
        user.setEmailVerified(true);
        User savedUser = userRepository.save(user);

        log.info("OTP verified successfully for user: {}", email);
        return savedUser;
    }

    private void checkRateLimit(String email) {
        var rateLimitOpt = rateLimitRepository.findByEmail(email);

        if (rateLimitOpt.isPresent()) {
            OtpRateLimit rateLimit = rateLimitOpt.get();
            if (rateLimit.getRequestCount() >= MAX_REQUESTS_PER_WINDOW) {
                throw new ApiException(ErrorCode.TOO_MANY_REQUESTS,
                        "Bạn đã vượt quá số lần yêu cầu OTP. Vui lòng thử lại sau 15 phút.");
            }
            // Increment count
            rateLimit.setRequestCount(rateLimit.getRequestCount() + 1);
            rateLimitRepository.save(rateLimit);
        } else {
            // Create new rate limit entry
            long ttlSeconds = RATE_LIMIT_WINDOW.getSeconds();
            OtpRateLimit rateLimit = OtpRateLimit.builder()
                    .id(UUID.randomUUID().toString())
                    .email(email)
                    .requestCount(1)
                    .ttlSeconds(ttlSeconds)
                    .build();
            rateLimitRepository.save(rateLimit);
        }
    }
}
