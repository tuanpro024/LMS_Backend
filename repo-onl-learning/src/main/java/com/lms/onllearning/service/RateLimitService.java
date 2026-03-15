package com.lms.onllearning.service;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Rate limiting service — kết hợp IP bucket + userId bucket.
 * Cả hai bucket PHẢI pass mới cho phép request tiếp tục.
 * Giải quyết vấn đề NAT/proxy: user khác nhau, dù cùng IP, vẫn được giới hạn riêng biệt.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final ProxyManager<String> bucketProxyManager;
    private final BucketConfiguration ipBucketConfiguration;
    private final BucketConfiguration userBucketConfiguration;

    /**
     * Kiểm tra rate-limit cho endpoint đăng ký lead.
     *
     * @return true nếu request được phép, false nếu vượt quá giới hạn
     */
    public boolean tryConsume(HttpServletRequest request, String userId) {
        String clientIp = resolveClientIp(request);

        Bucket ipBucket   = bucketProxyManager.builder()
                .build("rate:ip:" + clientIp, () -> ipBucketConfiguration);
        Bucket userBucket = bucketProxyManager.builder()
                .build("rate:user:" + userId, () -> userBucketConfiguration);

        boolean ipAllowed   = ipBucket.tryConsume(1);
        boolean userAllowed = userBucket.tryConsume(1);

        if (!ipAllowed) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
        }
        if (!userAllowed) {
            log.warn("Rate limit exceeded for userId: {}", userId);
        }

        return ipAllowed && userAllowed;
    }

    /** Lấy IP thực từ header X-Forwarded-For (proxy/load-balancer) hoặc remote address */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim(); // lấy IP đầu tiên
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
