package com.lms.identity.service;

import com.lms.identity.dto.response.DeviceResponse;
import com.lms.identity.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface DeviceService {

    boolean checkDeviceLogin(User user, String deviceId, HttpServletRequest request);

    void replaceOldestDevice(User user, String deviceId, HttpServletRequest request);
    void removeDevice(String deviceId);
    List<DeviceResponse> getMyDevices();
    
    String resendDeviceOtp(String deviceId);
}
