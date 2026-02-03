package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.response.LoginHistoryResponse;
import com.lms.identity.entity.LoginHistory;
import com.lms.identity.entity.User;
import com.lms.identity.repository.LoginHistoryRepository;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.service.LoginHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    private static final Parser uaParser;
    static {
        Parser tmp = null;
        try { tmp = new Parser(); } catch (Exception ignored) {}
        uaParser = tmp;
    }

    @Override
    @Transactional
    public void logLogin(User user, HttpServletRequest request) {
        try {
            String userAgent = request.getHeader("User-Agent");
            String ip = request.getRemoteAddr();
            String deviceId = request.getHeader("X-Device-ID");

            String browser = "Unknown";
            String os = "Unknown";

            if (uaParser != null && userAgent != null) {
                Client c = uaParser.parse(userAgent);
                if (c.os != null) os = c.os.family;
                if (c.userAgent != null) browser = c.userAgent.family;
            }

            LoginHistory history = LoginHistory.builder()
                    .user(user)
                    .deviceId(deviceId)
                    .deviceName(browser + " on " + os)
                    .os(os)
                    .browser(browser)
                    .ipAddress(ip)
                    .location(ip)
                    .loginTime(LocalDateTime.now())
                    .build();

            loginHistoryRepository.save(history);
        } catch (Exception e) {
            System.err.println("Lỗi lưu history: " + e.getMessage());
        }
    }

    @Override
    public List<LoginHistoryResponse> getMyLoginHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        return loginHistoryRepository.findByUser_IdOrderByLoginTimeDesc(user.getId()).stream()
                .map(h -> LoginHistoryResponse.builder()
                        .id(h.getId())
                        .deviceName(h.getDeviceName())
                        .os(h.getOs())
                        .browser(h.getBrowser())
                        .ipAddress(h.getIpAddress())
                        .loginTime(h.getLoginTime())
                        .build())
                .collect(Collectors.toList());
    }
}