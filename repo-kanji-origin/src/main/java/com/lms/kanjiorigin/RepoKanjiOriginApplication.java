package com.lms.kanjiorigin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.lms.kanjiorigin", "com.lms.common"})
public class RepoKanjiOriginApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoKanjiOriginApplication.class, args);
    }
}
