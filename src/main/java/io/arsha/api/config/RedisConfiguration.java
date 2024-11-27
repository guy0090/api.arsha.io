package io.arsha.api.config;

import io.arsha.api.config.services.CacheConfigurationService;
import io.arsha.api.data.CacheCompositeKey;
import io.arsha.api.data.market.MarketResponse;
import io.arsha.api.data.scraper.ScrapedItem;
import io.arsha.api.lib.CacheCompositeKeyRedisSerializer;
import io.arsha.api.lib.MarketResponseValueRedisSerializer;
import io.arsha.api.lib.ScrapedItemValueRedisSerializer;
import java.time.Duration;
import java.util.HashMap;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@EnableRedisRepositories(enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfiguration {

    private RedisCacheConfiguration createCacheConfiguration(Long timeoutInMillis) {
        var ttl = timeoutInMillis <= 0 ? Duration.ZERO : Duration.ofMillis(timeoutInMillis);
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(ttl);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(CacheConfigurationService properties) {
        var redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        var redisProperties = properties.getRedisConfig();
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());

        if (redisProperties.isUseAuth()) {
            redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
            redisStandaloneConfiguration.setUsername(redisProperties.getUsername());
        }

        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Bean
    public ReactiveRedisTemplate<CacheCompositeKey, MarketResponse> reactiveMarketRedisTemplate(
        LettuceConnectionFactory lettuceConFactory) {
        var serializationContext = RedisSerializationContext
            .<CacheCompositeKey, MarketResponse>newSerializationContext(
                new CacheCompositeKeyRedisSerializer())
            .key(new CacheCompositeKeyRedisSerializer())
            .hashKey(new CacheCompositeKeyRedisSerializer())
            .value(new MarketResponseValueRedisSerializer())
            .hashValue(new MarketResponseValueRedisSerializer())
            .build();
        return new ReactiveRedisTemplate<>(lettuceConFactory, serializationContext);
    }

    @Bean
    public RedisTemplate<CacheCompositeKey, MarketResponse> marketRedisTemplate(
        RedisConnectionFactory cf) {
        var template = new RedisTemplate<CacheCompositeKey, MarketResponse>();
        template.setConnectionFactory(cf);
        template.setHashKeySerializer(new CacheCompositeKeyRedisSerializer());
        template.setKeySerializer(new CacheCompositeKeyRedisSerializer());
        template.setValueSerializer(new MarketResponseValueRedisSerializer());
        template.setHashValueSerializer(new MarketResponseValueRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, ScrapedItem> dbRedisTemplate(RedisConnectionFactory cf) {
        var template = new RedisTemplate<String, ScrapedItem>();
        template.setConnectionFactory(cf);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new ScrapedItemValueRedisSerializer());
        template.setHashValueSerializer(new ScrapedItemValueRedisSerializer());
        return template;
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
        MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("__keyevent@*__:expired"));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(MessageListener listener) {
        return new MessageListenerAdapter(listener);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory rCf) {
        HashMap<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        return RedisCacheManager.builder(rCf)
            .cacheDefaults(createCacheConfiguration(0L))
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
