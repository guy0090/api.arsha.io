package io.arsha.api.services;

import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.config.services.CacheConfigurationService;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.lib.CacheCompositeKeyRedisSerializer;
import io.arsha.api.lib.MarketResponseValueRedisSerializer;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class MarketRedisService {

    @NonNull
    private final RedisTemplate<CacheCompositeKey, MarketResponse> marketRedisTemplate;
    @NonNull
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

    @Async("asyncRedisExecutor")
    public CompletableFuture<CacheCompositeKey> setEternalAsync(CacheCompositeKey key, MarketResponse value) {
        setEternal(key, value);
        return CompletableFuture.completedFuture(key);
    }

    @Async("asyncRedisExecutor")
    @SneakyThrows
    public CompletableFuture<CacheCompositeKey> setDefaultExpireAsync(CacheCompositeKey key, MarketResponse value) {
        setDefaultExpire(key, value);
        return CompletableFuture.completedFuture(key);
    }

    @Async("asyncRedisExecutor")
    public CompletableFuture<CacheCompositeKey> setExpireAsync(CacheCompositeKey key, MarketResponse value, Duration ttl) {
        setExpire(key, value, ttl);
        return CompletableFuture.completedFuture(key);
    }

    public void expire(CacheCompositeKey key, Duration ttl) {
        marketRedisTemplate.expire(key, ttl);
    }

    public long ttl(CacheCompositeKey key) {
        return Optional.ofNullable(marketRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS))
                .orElse(-1L);
    }

    public boolean delete(CacheCompositeKey key) {
        return Optional.ofNullable(marketRedisTemplate.delete(key)).orElse(false);
    }

    public Optional<MarketResponse> get(CacheCompositeKey key) {
        return Optional.ofNullable(marketRedisTemplate.opsForValue().get(key));
    }

    public Optional<MarketResponse> getAndDelete(CacheCompositeKey key) {
        var value = get(key);

        var deleted = delete(key);
        if (deleted) return value;
        return Optional.empty();
    }

    public void flushDb() {
        marketRedisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    public void flushDbAsync() {
        marketRedisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.serverCommands().flushDb(RedisServerCommands.FlushOption.ASYNC);
            return null;
        });
    }

    /// Bulk Operations

    public List<Object> putMany(Map<CacheCompositeKey, MarketResponse> values, Duration ttl) {
       var serializedMap = LinkedHashMap.<byte[], byte[]>newLinkedHashMap(values.size());
        values.forEach((k, v) -> {
            var key = serializeKey(k);
            var value = serializeValue(v);
            serializedMap.put(key, value);
        });

        return marketRedisTemplate.executePipelined(((RedisCallback<Object>) connection -> {
            var expiration = Expiration.from(ttl);
            var stringCommands = connection.stringCommands();
            serializedMap.forEach((k, v) ->
                    stringCommands.set(k, v, expiration, RedisStringCommands.SetOption.SET_IF_ABSENT));
            return null;
        }));
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

    private byte[] serializeKey(CacheCompositeKey key) {
        var serializer = (CacheCompositeKeyRedisSerializer) marketRedisTemplate.getKeySerializer();
        return serializer.serialize(key);
    }

    private byte[] serializeValue(MarketResponse value) {
        var serializer = (MarketResponseValueRedisSerializer) marketRedisTemplate.getValueSerializer();
        return serializer.serialize(value);
    }

    private Duration getDefaultTtl() {
        return Duration.ofMinutes(configService.getTtl());
    }
}
