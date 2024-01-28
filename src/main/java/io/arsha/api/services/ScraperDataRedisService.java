package io.arsha.api.services;

import io.arsha.api.data.scraper.ScrapedItem;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ScraperDataRedisService {

    static final String LAST_SCRAPE_TIME_KEY = "lastScrapedTime";
    static final String LOCK_KEY = "scraperLock";

    private final RedisTemplate<String, ScrapedItem> redisTemplate;
    private final RedisTemplate<String, String> redisStringTemplate;

    public void setLock(String lockOwner) {
        redisStringTemplate.opsForValue().set(LOCK_KEY, lockOwner);
    }

    public void releaseLock() {
        redisStringTemplate.delete(LOCK_KEY);
    }

    public Optional<String> getLockOwner() {
        return Optional.ofNullable(redisStringTemplate.opsForValue().get(LOCK_KEY));
    }

    public void saveLastScrapedTime(String locale) {
        redisStringTemplate.<String, String>opsForHash().put(LAST_SCRAPE_TIME_KEY, locale, String.valueOf(System.currentTimeMillis()));
    }

    public Optional<OffsetDateTime> getLastScrapedTime(String locale) {
        var time = redisStringTemplate.<String, String>opsForHash().get(LAST_SCRAPE_TIME_KEY, locale);
        return Optional.ofNullable(time)
                .map(epoch -> {
                    var instant = Instant.ofEpochMilli(Long.parseLong(epoch));
                    return OffsetDateTime.ofInstant(instant, ZoneId.of("UTC"));
                });
    }

    public Map<String, Instant> getLastScrapedTimes() {
        var times = redisStringTemplate.<String, String>opsForHash().entries(LAST_SCRAPE_TIME_KEY);
        return times.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> Instant.ofEpochMilli(Long.parseLong(entry.getValue())))
                );
    }

    public void put(String locale, Long id, ScrapedItem value) {
        redisTemplate.opsForHash().put(locale, String.valueOf(id), value);
    }

    public void putAll(String locale, List<ScrapedItem> items) {
        var mapped = items.stream().collect(Collectors.toMap(ScrapedItem::stringId, item -> item));
        redisTemplate.opsForHash().putAll(locale, mapped);
    }

    @Async("asyncExecutor")
    public CompletableFuture<?> putAsync(String locale, Long id, ScrapedItem value) {
        put(locale, id, value);
        return CompletableFuture.completedFuture(null);
    }

    @Async("asyncExecutor")
    public CompletableFuture<?> putAllAsync(String locale, List<ScrapedItem> items) {
        putAll(locale, items);
        return CompletableFuture.completedFuture(null);
    }

    public Optional<ScrapedItem> get(String locale, String key) {
        return Optional.ofNullable(redisTemplate.<Long, ScrapedItem>opsForHash().get(locale, key));
    }

    public List<ScrapedItem> getMany(String locale, List<Long> ids) {
        var stringIds = ids.stream().map(String::valueOf).toList();
        return redisTemplate.<String, ScrapedItem>opsForHash().multiGet(locale, stringIds);
    }

    public List<ScrapedItem> getAll(String locale) {
        return redisTemplate.<String, ScrapedItem>opsForHash().values(locale);
    }

}
