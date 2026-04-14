package com.lms.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Import;
import com.lms.common.notification.NotificationPublisher;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
@Import(NotificationPublisher.class)
public class RepoNotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoNotificationApplication.class, args);
    }

}
