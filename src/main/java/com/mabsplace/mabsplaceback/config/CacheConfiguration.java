package com.mabsplace.mabsplaceback.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration - 5 minutes TTL
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // Specific cache configurations with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Dashboard stats - 5 minutes
        cacheConfigurations.put("dashboardStats", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Trend data - 15 minutes (changes less frequently)
        cacheConfigurations.put("revenueTrend", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("expenseTrends", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("serviceDistribution", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("topServices", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Historical data - 1 hour (rarely changes)
        cacheConfigurations.put("historicalMetrics", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("monthlyPerformance", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Subscription health - 10 minutes
        cacheConfigurations.put("subscriptionHealth", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
