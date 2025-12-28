package com.lms.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.lms.identity", "com.lms.common"})
public class RepoIdentityApplication {
    public static void main(String[] args) {
        SpringApplication.run(RepoIdentityApplication.class, args);
    }
}
