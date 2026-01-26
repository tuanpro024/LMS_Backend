package com.lms.dictionary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.lms.dictionary", "com.lms.common"})
public class RepoDictionaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoDictionaryApplication.class, args);
    }
}
