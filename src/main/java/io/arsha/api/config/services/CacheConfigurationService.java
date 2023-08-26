package io.arsha.api.config.services;

import io.arsha.api.config.properties.CacheProperties;
import io.arsha.api.config.properties.RedisProperties;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CacheConfigurationService {

    private final CacheProperties cacheProperties;

    public RedisProperties getRedisConfig() {
        return cacheProperties.getRedis();
    }

    public Integer getTtl() {
        return cacheProperties.getTtl();
    }


}
