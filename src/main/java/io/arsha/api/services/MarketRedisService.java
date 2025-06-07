package io.arsha.api.services;

import io.arsha.api.config.services.CacheConfigurationService;
import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketRedisService {

    private final RedisTemplate<CacheCompositeKey, MarketResponse> marketRedisTemplate;
    private final CacheConfigurationService configService;

    public void setEternal(CacheCompositeKey key, MarketResponse value) {
        marketRedisTemplate.opsForValue().set(key, value);
    }

    public void setDefaultExpire(CacheCompositeKey key, MarketResponse value) {
        setExpire(key, value, getDefaultTtl());
    }

    public void setExpire(CacheCompositeKey key, MarketResponse value, Duration ttl) {
        marketRedisTemplate.opsForValue().set(key, value, ttl);
    }

    @Async("asyncExecutor")
    @SneakyThrows
    public CompletableFuture<CacheCompositeKey> setDefaultExpireAsync(CacheCompositeKey key, MarketResponse value) {
        setDefaultExpire(key, value);
        return CompletableFuture.completedFuture(key);
    }

    public long ttl(CacheCompositeKey key) {
        return Optional.ofNullable(marketRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS))
            .orElse(-1L);
    }

    public Optional<MarketResponse> get(CacheCompositeKey key) {
        return Optional.ofNullable(marketRedisTemplate.opsForValue().get(key));
    }

    public void flushDb() {
        marketRedisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    public Map<CacheCompositeKey, MarketResponse> getMany(List<CacheCompositeKey> keys) throws IllegalStateException {
        var items = Optional.ofNullable(marketRedisTemplate.opsForValue().multiGet(keys))
            .orElseThrow(() -> new IllegalStateException("Redis returned null for multiGet"));

        var results = LinkedHashMap.<CacheCompositeKey, MarketResponse>newLinkedHashMap(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            var key = keys.get(i);
            var value = items.get(i);
            results.put(key, value);
        }

        return results;
    }

    private Duration getDefaultTtl() {
        return Duration.ofMinutes(configService.getTtl());
    }
}
