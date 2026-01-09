package com.lms.identity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class EmailConfig {
    // Enable async execution cho email sending
    // Để email không block main thread
}
