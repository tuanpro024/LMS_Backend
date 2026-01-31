package com.lms.pronunciation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.lms.pronunciation", "com.lms.common"})
public class RepoPronunciationApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoPronunciationApplication.class, args);
    }
}
