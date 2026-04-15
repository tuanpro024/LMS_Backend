package com.lms.identity.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.common.event.MembershipExpiryEvent;
import com.lms.identity.entity.User;
import com.lms.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipExpiryScheduler {

    private final UserRepository userRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Chạy hằng ngày lúc 00:00 để quét các học viên sắp hết hạn (còn 3 ngày)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void scanExpiringMemberships() {
        log.info("Starting scan for expiring memberships...");
        Instant now = Instant.now();
        Instant threeDaysFromNow = now.plus(3, ChronoUnit.DAYS);
        
        List<User> expiringUsers = userRepository.findAllByPremiumTrueAndPremiumExpiryDateBetween(
                threeDaysFromNow.minus(12, ChronoUnit.HOURS),
                threeDaysFromNow.plus(12, ChronoUnit.HOURS)
        );

        for (User user : expiringUsers) {
            log.info("User {} is expiring in 3 days, publishing event", user.getEmail());
            MembershipExpiryEvent event = MembershipExpiryEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .expiryDate(user.getPremiumExpiryDate())
                    .daysRemaining(3)
                    .build();
            try {
                String json = objectMapper.writeValueAsString(event);
                kafkaTemplate.send("membership.expiring", json);
            } catch (Exception e) {
                log.error("Failed to serialize MembershipExpiryEvent", e);
            }
        }
        
        List<User> expiredUsers = userRepository.findAllByPremiumTrueAndPremiumExpiryDateBefore(now);
        for (User user : expiredUsers) {
            log.info("User {} membership has expired, deactivating", user.getEmail());
            user.setPremium(false);
            userRepository.save(user);
        }
    }
}
