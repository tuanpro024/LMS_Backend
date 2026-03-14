package com.lms.onllearning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${cms.base-url}")
    private String cmsBaseUrl;

    @Bean
    public WebClient cmsWebClient() {
        return WebClient.builder()
                .baseUrl(cmsBaseUrl)
                .defaultHeader("Accept", "application/json")
                .codecs(config -> config.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();
    }
}
