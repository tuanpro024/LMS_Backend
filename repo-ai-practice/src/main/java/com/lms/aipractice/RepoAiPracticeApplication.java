package com.lms.aipractice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {
        "com.lms.aipractice",
        "com.lms.content.common",
        "com.lms.common"
})
@EntityScan(basePackages = {
        "com.lms.aipractice.entity",
        "com.lms.content.common.entity",
        "com.lms.common.jpa"
})
@EnableJpaRepositories(basePackages = {
        "com.lms.aipractice.repository"
})
public class RepoAiPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoAiPracticeApplication.class, args);
    }
}
