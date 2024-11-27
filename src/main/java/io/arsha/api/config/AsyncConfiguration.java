package io.arsha.api.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

@Configuration
public class AsyncConfiguration {

    @Bean
    public Executor asyncExecutor() {
        var factory = Thread.ofVirtual().factory();
        return Executors.newCachedThreadPool(factory);
    }

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster(Executor asyncExecutor) {
        var eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(asyncExecutor);
        return eventMulticaster;
    }
}
