package io.arsha.api.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {
    private Integer ttl = 1; // 30; // 30 minutes
    private RedisProperties redis = new RedisProperties();
}
