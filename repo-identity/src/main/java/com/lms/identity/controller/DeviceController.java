package com.lms.identity.controller;

import com.lms.common.dto.ApiResponse;
import com.lms.identity.dto.response.DeviceResponse;
import com.lms.identity.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ApiResponse<List<DeviceResponse>> getMyDevices() {
        return ApiResponse.ok(deviceService.getMyDevices());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> removeDevice(@PathVariable String id) {
        deviceService.removeDevice(id);
        return ApiResponse.ok("Device removed successfully");
    }

    @PostMapping("/{id}/resend-otp")
    public ApiResponse<String> resendDeviceOtp(@PathVariable String id) {
        String otpToken = deviceService.resendDeviceOtp(id);
        return ApiResponse.ok("OTP sent successfully");
    }
}