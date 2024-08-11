package ru.scisolutions.scicmscore.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
import java.net.URI
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheConfiguration(): RedisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(60))
        .disableCachingNullValues()
        .serializeValuesWith(SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer()))

    @Bean
    fun redisCacheManagerBuilderCustomizer(): RedisCacheManagerBuilderCustomizer = RedisCacheManagerBuilderCustomizer { builder: RedisCacheManagerBuilder ->
        builder
            .withCacheConfiguration(
                "itemRecCache",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(10))
            )
            .withCacheConfiguration(
                "methodCache",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(5))
            )
    }

    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config.fromYAML(URI("classpath:redisson.yml").toURL())
//        config.useSingleServer().setAddress("redis://$redisHost:$redisPort")

        return Redisson.create(config)
    }
}
