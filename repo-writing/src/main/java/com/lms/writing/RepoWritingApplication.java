package com.lms.writing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
        "com.lms.writing",
        "com.lms.common"
})
@EnableDiscoveryClient
@EnableJpaAuditing
public class RepoWritingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoWritingApplication.class, args);
    }
}
