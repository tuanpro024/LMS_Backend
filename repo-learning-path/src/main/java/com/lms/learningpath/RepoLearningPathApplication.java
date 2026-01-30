package com.lms.learningpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
                "com.lms.learningpath",
                "com.lms.content.common",
                "com.lms.common"
})
@EnableJpaRepositories(basePackages = {
                "com.lms.learningpath.repository"
})
@EntityScan(basePackages = {
                "com.lms.learningpath.entity",
                "com.lms.content.common.entity",
                "com.lms.common.jpa"
})
@EnableFeignClients
@EnableDiscoveryClient
public class RepoLearningPathApplication {

        public static void main(String[] args) {
                SpringApplication.run(RepoLearningPathApplication.class, args);
        }

}
