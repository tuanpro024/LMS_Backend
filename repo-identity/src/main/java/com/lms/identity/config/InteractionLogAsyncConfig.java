package com.lms.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Async configuration for interaction logging.
 * Uses a dedicated thread pool to avoid blocking request threads
 * and to isolate logging from other async work (e.g. email).
 */
@Configuration
@EnableAsync
public class InteractionLogAsyncConfig {

    @Bean(name = "interactionLogExecutor")
    public Executor interactionLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("interaction-log-");
        // If the queue is full, the caller thread runs the task to avoid data loss
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
