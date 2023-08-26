package io.arsha.api.controllers;

import io.arsha.api.services.ScraperDataRedisService;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/status")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StatusController {

    private final RedisTemplate<String, String> redisTemplate;
    private final ScraperDataRedisService scraperRedisService;

    @RequestMapping(value = "/scraped-times", method = { RequestMethod.GET, RequestMethod.POST })
    protected Map<String, Instant> scrapedTimes() {
        return scraperRedisService.getLastScrapedTimes();
    }

    @RequestMapping(value = "/cache", method = { RequestMethod.GET, RequestMethod.POST })
    protected Properties cache() {
        return (Properties) redisTemplate.execute(
                (RedisCallback<Object>) connection -> connection.serverCommands().info("memory"));
    }
}
