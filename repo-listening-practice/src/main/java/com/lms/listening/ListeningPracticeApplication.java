package com.lms.listening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.lms.listening",
        "com.lms.common",
        "com.lms.content.common"
})
@EntityScan(basePackages = {
        "com.lms.listening.entity",
        "com.lms.common.jpa",
        "com.lms.content.common.entity"
})
@EnableJpaRepositories(basePackages = {
        "com.lms.listening.repository"

})
public class ListeningPracticeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListeningPracticeApplication.class, args);
    }
}
