package com.lms.onllearning.config;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.register.ip-capacity:10}")
    private long ipCapacity;

    @Value("${rate-limit.register.user-capacity:5}")
    private long userCapacity;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisClient lettuceRedisClient() {
        return RedisClient.create("redis://" + redisHost + ":" + redisPort);
    }

    @Bean
    public LettuceBasedProxyManager<String> bucketProxyManager(RedisClient lettuceRedisClient) {
        StatefulRedisConnection<String, byte[]> connection =
                lettuceRedisClient.connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return LettuceBasedProxyManager.builderFor(connection)
                .withExpirationStrategy(
                    ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(2)))
                .build();
    }

    /** BucketConfiguration cho rate-limit theo IP: ipCapacity req/phút */
    @Bean
    public BucketConfiguration ipBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                    .capacity(ipCapacity)
                    .refillGreedy(ipCapacity, Duration.ofMinutes(1))
                    .initialTokens(ipCapacity))
                .build();
    }

    /** BucketConfiguration cho rate-limit theo userId: userCapacity req/phút */
    @Bean
    public BucketConfiguration userBucketConfiguration() {
        return BucketConfiguration.builder()
                .addLimit(limit -> limit
                    .capacity(userCapacity)
                    .refillGreedy(userCapacity, Duration.ofMinutes(1))
                    .initialTokens(userCapacity))
                .build();
    }
}
