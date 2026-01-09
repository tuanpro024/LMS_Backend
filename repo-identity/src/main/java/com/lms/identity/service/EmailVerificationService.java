package com.lms.identity.service;

import com.lms.identity.entity.User;

public interface EmailVerificationService {
    String createToken(User user);

    User verify(String tokenValue);
}
