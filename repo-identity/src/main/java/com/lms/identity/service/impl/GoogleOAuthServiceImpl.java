package com.lms.identity.service.impl;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.response.AuthResponse;
import com.lms.identity.entity.AuthProvider;
import com.lms.identity.entity.Role;
import com.lms.identity.entity.RoleName;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;
import com.lms.identity.repository.RoleRepository;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.repository.ExternalTeacherRepository;
import com.lms.identity.service.DeviceService;
import com.lms.identity.service.GoogleOAuthService;
import com.lms.identity.service.JwtTokenService;
import com.lms.identity.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.security.GeneralSecurityException;
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenService jwtTokenService;
    private final DeviceService deviceService;
    private final OtpService otpService;
    private final ExternalTeacherRepository externalTeacherRepository;


    @Transactional
    @Override
    public AuthResponse authenticateWithGoogle(String idToken, HttpServletRequest servletRequest) {
        try {
            // 1. Verify Google ID token
            GoogleIdToken googleToken = googleIdTokenVerifier.verify(idToken);
            if (googleToken == null) {
                throw new ApiException(ErrorCode.UNAUTHORIZED, "Invalid Google ID token");
            }
            // 2. Extract user info from token payload
            GoogleIdToken.Payload payload = googleToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            Boolean emailVerified = payload.getEmailVerified();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            log.info("Google login attempt for email: {}, googleId: {}", email, googleId);
            // 3. Check if user exists by googleId or email
            User user = userRepository.findByGoogleId(googleId)
                    .orElse(userRepository.findByEmail(email).orElse(null));
            if (user == null) {
                // 4. Create new user
                user = createGoogleUser(googleId, email, name, pictureUrl, emailVerified);
            } else {
                // 5. Update existing user
                user = updateExistingUser(user, googleId, name, pictureUrl, emailVerified);
            }

            String deviceId = servletRequest.getHeader("X-Device-ID");

            if (deviceId != null && !deviceId.isBlank()) {
                boolean isAllowed = deviceService.checkDeviceLogin(user, deviceId, servletRequest);

                if (!isAllowed) {
                    otpService.generateAndSendDeviceOtp(user);

                    // Chặn đăng nhập, ném lỗi để Frontend hiển thị Popup OTP
                    throw new ApiException(ErrorCode.DEVICE_LIMIT_EXCEEDED);
                }
            }
            // 6. Issue JWT tokens
            return jwtTokenService.issueTokens(user);
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error verifying Google token", e);
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Failed to verify Google token");
        }
    }
    private User createGoogleUser(String googleId, String email, String name, String pictureUrl, Boolean emailVerified) {
        log.info("Creating new Google user for email: {}", email);

        Role assignedRole = roleRepository.findByName(
                        externalTeacherRepository.existsByEmail(email) ? RoleName.ROLE_TEACHER : RoleName.ROLE_USER)
                .orElseThrow(() -> new ApiException(ErrorCode.E221, "Role not found"));

        User user = User.builder()
                .googleId(googleId)
                .email(email)
                .fullName(name)
                .avatarUrl(pictureUrl)
                .authProvider(AuthProvider.GOOGLE)
                .emailVerified(emailVerified != null && emailVerified)
                .status(UserStatus.ACTIVE)
                .password(null) // No password for Google users
                .build();

        user.getRoles().add(assignedRole);

        return userRepository.save(user);
    }
    private User updateExistingUser(User user, String googleId, String name, String pictureUrl, Boolean emailVerified) {
        log.info("Updating existing user: {}", user.getEmail());

        // Link Google account if not already linked
        if (user.getGoogleId() == null) {
            user.setGoogleId(googleId);
        }
        // Update profile info from Google
        if (name != null && !name.equals(user.getFullName())) {
            user.setFullName(name);
        }
        if (pictureUrl != null && !pictureUrl.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(pictureUrl);
        }
        // Mark email as verified if Google says it's verified
        if (emailVerified != null && emailVerified && !user.isEmailVerified()) {
            user.setEmailVerified(true);
        }
        return userRepository.save(user);
    }
}