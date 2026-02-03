package com.lms.identity.service;

import com.lms.identity.dto.response.LoginHistoryResponse;
import com.lms.identity.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface LoginHistoryService {
    void logLogin(User user, HttpServletRequest request);
    List<LoginHistoryResponse> getMyLoginHistory();
}