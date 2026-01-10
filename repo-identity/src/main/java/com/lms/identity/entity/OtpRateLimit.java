package com.lms.identity.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("otp_rate_limit")
public class OtpRateLimit implements Serializable {

    @Id
    private String id;

    @Indexed
    private String email;

    private int requestCount;

    @TimeToLive
    private Long ttlSeconds;
}
