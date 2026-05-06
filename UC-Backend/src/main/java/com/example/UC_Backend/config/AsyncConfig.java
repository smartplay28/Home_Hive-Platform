package com.example.UC_Backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configures the async thread pool used for order processing.
 * Checkout HTTP response returns 202 immediately while this pool
 * handles agent matching and WebSocket notifications in the background.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${app.async.core-pool-size:10}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:200}")
    private int queueCapacity;

    @Bean("orderProcessingExecutor")
    public Executor orderProcessingExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(corePoolSize);
        exec.setMaxPoolSize(maxPoolSize);
        exec.setQueueCapacity(queueCapacity);
        exec.setThreadNamePrefix("order-async-");
        exec.setRejectedExecutionHandler((r, e) ->
            log.error("Order processing task rejected — queue full. Consider scaling up.")
        );
        exec.initialize();
        log.info("Order processing executor initialized: core={} max={} queue={}",
                corePoolSize, maxPoolSize, queueCapacity);
        return exec;
    }
}
