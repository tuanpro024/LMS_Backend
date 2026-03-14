package com.lms.onllearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OnlLearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlLearningApplication.class, args);
    }
}
