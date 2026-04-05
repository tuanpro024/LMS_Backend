package com.lms.onllearning.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${cms.base-url}")
    private String cmsBaseUrl;

    @Value("${cms.webclient.max-in-memory-mb:32}")
    private int cmsWebClientMaxInMemoryMb;

    @Bean
    public WebClient cmsWebClient() {
        int maxInMemory = Math.max(cmsWebClientMaxInMemoryMb, 2) * 1024 * 1024;
        return WebClient.builder()
                .baseUrl(cmsBaseUrl)
                .defaultHeader("Accept", "application/json")
                .codecs(config -> config.defaultCodecs().maxInMemorySize(maxInMemory))
                .build();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
