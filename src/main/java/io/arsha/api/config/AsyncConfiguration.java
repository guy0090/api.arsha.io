package io.arsha.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfiguration {

    @Bean
    public Executor asyncExecutor() {
        var factory = Thread.ofVirtual().factory();
        return Executors.newCachedThreadPool(factory);
    }
}
