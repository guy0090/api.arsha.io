package io.arsha.api.config.services;

import io.arsha.api.config.properties.CacheProperties;
import io.arsha.api.config.properties.RedisProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheConfigurationService {

    private final CacheProperties cacheProperties;

    public RedisProperties getRedisConfig() {
        return cacheProperties.getRedis();
    }

    public Long getTtl() {
        return cacheProperties.getTtl();
    }


}
