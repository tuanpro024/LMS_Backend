package com.lms.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.lms.identity", "com.lms.common"})
@EnableScheduling
public class RepoIdentityApplication {
    public static void main(String[] args) {
        SpringApplication.run(RepoIdentityApplication.class, args);
    }
}
