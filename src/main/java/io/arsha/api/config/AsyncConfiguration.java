package io.arsha.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class AsyncConfiguration {

    @Bean
    public Executor asyncExecutor() {
        return Executors.newCachedThreadPool();
    }
}
