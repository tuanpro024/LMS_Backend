package com.lms.identity.service;

import com.lms.identity.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface GoogleOAuthService {
    AuthResponse authenticateWithGoogle(String idToken, HttpServletRequest request);
}
