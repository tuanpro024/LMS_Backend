package com.lms.content.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Auto-configuration for Content Common Module
 * Enables component scanning for services, delegates, and utilities
 * Does NOT scan controllers - those are defined in each service
 */
@Configuration
@ComponentScan(basePackages = {
        "com.lms.content.common.service",
        "com.lms.content.common.delegate.api",
    "com.lms.content.common.util",
    "com.lms.content.common.mapper"
})
@EntityScan(basePackages = "com.lms.content.common.entity")
@EnableJpaRepositories(basePackages = "com.lms.content.common.repository")
public class ContentCommonAutoConfiguration {
    // Services, delegates, and repositories will be available to apps that depend
    // on this module
}
