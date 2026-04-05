package com.lms.aipractice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async executor for @Async event listeners (e.g. AiPracticeProgressEventListener).
 * Separate thread pool to avoid blocking the scheduler or HTTP request threads.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "aiPracticeEventExecutor")
    public Executor aiPracticeEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-practice-event-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
