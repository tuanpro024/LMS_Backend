package com.lms.pronunciation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.lms.pronunciation",
        "com.lms.common"
})
@EntityScan(basePackages = {
        "com.lms.pronunciation.entity",
        "com.lms.content.common.entity",
        "com.lms.common.jpa"
})
@EnableJpaRepositories(basePackages = {
        "com.lms.pronunciation.repository",
        "com.lms.content.common.repository"
})
public class RepoPronunciationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoPronunciationApplication.class, args);
    }
}
