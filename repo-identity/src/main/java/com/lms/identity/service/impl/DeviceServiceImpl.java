package com.lms.identity.service.impl;

import com.lms.common.exception.ApiException;
import com.lms.common.exception.ErrorCode;
import com.lms.identity.dto.response.DeviceResponse;
import com.lms.identity.entity.*;
import com.lms.identity.repository.UserDeviceRepository;
import com.lms.identity.repository.UserRepository;
import com.lms.identity.service.DeviceService;
import com.lms.identity.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ua_parser.Client;
import ua_parser.Parser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final UserDeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;

    private static final int LIMIT_MOBILE = 2;
    private static final int LIMIT_TABLET = 1;
    private static final int LIMIT_WEBSITE = 2;

    private static Parser uaParser;
    static {
        try {
            uaParser = new Parser();
        } catch (Exception e) {
            System.err.println("Lỗi khởi tạo UA Parser: " + e.getMessage());
        }
    }

    @Override
    public boolean checkDeviceLogin(User user, String deviceId, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        DeviceType type = detectDeviceType(userAgent);

        String[] info = parseUserAgent(userAgent);
        String ipAddress = request.getRemoteAddr();

        var existingDevice = deviceRepository.findByUser_IdAndDeviceId(user.getId(), deviceId);

        if (existingDevice.isPresent()) {
            UserDevice device = existingDevice.get();
            device.setLastLogin(LocalDateTime.now());
            device.setDeviceName(info[0] + " on " + info[1]);
            device.setBrowser(info[0]);
            device.setOs(info[1]);
            device.setLocation(ipAddress);

            deviceRepository.save(device);
            return true;
        }

        long currentCount = deviceRepository.countByUser_IdAndDeviceType(user.getId(), type);
        int limit = getLimitByType(type);

        if (currentCount >= limit) {
            return false;
        }

        saveNewDevice(user, deviceId, userAgent, type, request);
        return true;
    }

    @Override
    public List<DeviceResponse> getMyDevices() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        String currentDeviceId = getCurrentRequestDeviceId();

        return deviceRepository.findAllByUser_Id(user.getId()).stream()
                .map(device -> DeviceResponse.builder()
                        .id(device.getId())
                        .deviceId(device.getDeviceId())
                        .name(device.getDeviceName())
                        .type(device.getDeviceType().name())
                        .os(device.getOs())
                        .browser(device.getBrowser())
                        .location(device.getLocation())
                        .date(device.getLastLogin())
                        .isCurrent(device.getDeviceId().equals(currentDeviceId))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void removeDevice(String deviceId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        UserDevice device = deviceRepository.findByIdAndUser_Id(deviceId, user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        deviceRepository.delete(device);
    }

    @Override
    public String resendDeviceOtp(String deviceId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_EXISTED));

        // Kiểm tra thiết bị có tồn tại không
        UserDevice device = deviceRepository.findByIdAndUser_Id(deviceId, user.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));

        // Gửi OTP xác thực thiết bị
        return otpService.generateAndSendDeviceOtp(user);
    }

    @Override
    public void replaceOldestDevice(User user, String deviceId, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        DeviceType type = detectDeviceType(userAgent);

        UserDevice oldest = deviceRepository.findFirstByUser_IdAndDeviceTypeOrderByLastLoginAsc(user.getId(), type);
        if (oldest != null) {
            deviceRepository.delete(oldest);
            deviceRepository.flush();
        }

        saveNewDevice(user, deviceId, userAgent, type, request);
    }

    private void saveNewDevice(User user, String deviceId, String userAgent, DeviceType type, HttpServletRequest request) {
        String[] info = parseUserAgent(userAgent);
        String ipAddress = request.getRemoteAddr();

        UserDevice newDevice = UserDevice.builder()
                .user(user)
                .deviceId(deviceId)
                .deviceType(type)
                .deviceName(info[0] + " on " + info[1])
                .browser(info[0])
                .os(info[1])
                .location(ipAddress)
                .lastLogin(LocalDateTime.now())
                .build();

        deviceRepository.save(newDevice);
    }

    private String[] parseUserAgent(String userAgent) {
        String browser = "Unknown Browser";
        String os = "Unknown OS";

        if (userAgent != null && uaParser != null) {
            try {
                Client c = uaParser.parse(userAgent);
                if (c.os != null && c.os.family != null) os = c.os.family;
                if (c.userAgent != null && c.userAgent.family != null) browser = c.userAgent.family;
            } catch (Exception ignored) {}
        }
        return new String[]{browser, os};
    }

    private String getCurrentRequestDeviceId() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("X-Device-ID");
            }
        } catch (Exception ignored) {}
        return "";
    }

    private int getLimitByType(DeviceType type) {
        switch (type) {
            case MOBILE: return LIMIT_MOBILE;
            case TABLET: return LIMIT_TABLET;
            default: return LIMIT_WEBSITE;
        }
    }

    private DeviceType detectDeviceType(String userAgent) {
        if (userAgent == null) return DeviceType.WEBSITE;
        String ua = userAgent.toLowerCase();
        if (ua.contains("ipad") || ua.contains("tablet")) return DeviceType.TABLET;
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) return DeviceType.MOBILE;
        return DeviceType.WEBSITE;
    }
}