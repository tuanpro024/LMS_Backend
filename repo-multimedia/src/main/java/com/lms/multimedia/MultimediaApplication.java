package com.lms.multimedia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.lms.multimedia", "com.lms.common" })
@EnableDiscoveryClient
@EntityScan(basePackages = { "com.lms.content.common.entity", "com.lms.common.jpa" })
@EnableJpaRepositories(basePackages = "com.lms.multimedia.repository")
public class MultimediaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultimediaApplication.class, args);
    }
}
