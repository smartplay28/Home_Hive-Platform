package com.example.UC_Backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration.
 *
 * Cache namespaces and their TTLs:
 *   "agents"       — individual agent lookups by agentId         (30s TTL)
 *   "agentsByLoc"  — all agents filtered by location+skill       (30s TTL)
 *   "orderHistory" — customer's order list by customerId         (60s TTL)
 *   "availability" — agent availability status lookup            (15s TTL)
 *
 * Why 30s for agents?
 *   Agents update their status frequently (accepting/rejecting orders).
 *   Stale data > 30s risks routing to an already-busy agent.
 *
 * Why not cache forever?
 *   This is a real-time service platform. Agents move, accept orders, change skills.
 *   Short TTLs balance performance vs. correctness.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${app.cache.agents-ttl-seconds:30}")
    private long agentsTtlSeconds;

    @Value("${app.cache.orders-ttl-seconds:60}")
    private long ordersTtlSeconds;

    /**
     * RedisTemplate for manual cache operations (type-safe get/set).
     * Uses String keys and JSON-serialized values for human-readable Redis inspection.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // String keys (e.g., "agent:12345")
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // JSON values — readable via `redis-cli get agent:12345`
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        log.info("cache.redis.template.initialized");
        return template;
    }

    /**
     * CacheManager with per-cache TTL configuration.
     * Powers @Cacheable, @CacheEvict, @CachePut annotations.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default config — JSON serialization + null-value protection
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // don't cache null — prevents negative caching bugs

        // Per-cache TTL overrides
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("agents",
                defaultConfig.entryTtl(Duration.ofSeconds(agentsTtlSeconds)));
        cacheConfigs.put("agentsByLoc",
                defaultConfig.entryTtl(Duration.ofSeconds(agentsTtlSeconds)));
        cacheConfigs.put("orderHistory",
                defaultConfig.entryTtl(Duration.ofSeconds(ordersTtlSeconds)));
        cacheConfigs.put("availability",
                defaultConfig.entryTtl(Duration.ofSeconds(15))); // very short — real-time status

        log.info("cache.manager.initialized agentsTtl={}s ordersTtl={}s",
                agentsTtlSeconds, ordersTtlSeconds);

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofSeconds(60)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
