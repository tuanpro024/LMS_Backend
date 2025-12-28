package com.lms.realtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.lms.realtime", "com.lms.common"})
public class RepoRealtimeApplication {
    public static void main(String[] args) {
        SpringApplication.run(RepoRealtimeApplication.class, args);
    }
}
