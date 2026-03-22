package com.lms.multimedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = { "com.lms.multimedia", "com.lms.content.common", "com.lms.common" })
@EnableDiscoveryClient
@EnableMethodSecurity
@EnableScheduling
@EntityScan(basePackages = { "com.lms.multimedia.entity", "com.lms.content.common.entity" })
@EnableJpaRepositories(basePackages = { "com.lms.multimedia.repository" })
public class MultimediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultimediaApplication.class, args);
    }
}
