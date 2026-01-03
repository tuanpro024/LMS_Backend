package com.lms.identity.service;

import com.lms.identity.dto.response.AuthResponse;

public interface GoogleOAuthService {
    AuthResponse authenticateWithGoogle(String idToken);
}
