package com.lms.flashcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
        "com.lms.flashcard",
        "com.lms.common"
})
@EnableDiscoveryClient
@EnableJpaAuditing
public class RepoFlashcardApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoFlashcardApplication.class, args);
    }
}
