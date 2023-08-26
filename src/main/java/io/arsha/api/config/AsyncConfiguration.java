package io.arsha.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfiguration {

    @Bean
    public Executor asyncRedisExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("redisAsync-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Executor asyncRequestExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        // Requests can take up to a couple seconds to complete, so im going with:
        // ms to complete = 1000, max. requests = 100
        // (ms to complete / 1000) * max. requests = number of threads
        // this is probably not the best idea but haha async good
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("requestAsync-");
        executor.initialize();
        return executor;
    }
}
