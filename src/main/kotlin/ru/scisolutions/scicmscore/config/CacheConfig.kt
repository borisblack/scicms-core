package ru.scisolutions.scicmscore.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
// @EnableCaching
class CacheConfig() {
    // @Bean
    // fun cacheConfiguration(): RedisCacheConfiguration =
    //     RedisCacheConfiguration.defaultCacheConfig()
    //         .entryTtl(Duration.ofMinutes(60))
    //         .disableCachingNullValues()
    //         .serializeValuesWith(SerializationPair.fromSerializer<Any>(GenericJackson2JsonRedisSerializer()))
    //
    // @Bean
    // fun redisCacheManagerBuilderCustomizer(): RedisCacheManagerBuilderCustomizer =
    //     RedisCacheManagerBuilderCustomizer { builder: RedisCacheManagerBuilder ->
    //         builder
    //             .withCacheConfiguration(
    //                 "itemRecCache",
    //                 RedisCacheConfiguration.defaultCacheConfig()
    //                     .entryTtl(Duration.ofMinutes(10))
    //             )
    //             .withCacheConfiguration(
    //                 "methodCache",
    //                 RedisCacheConfiguration.defaultCacheConfig()
    //                     .entryTtl(Duration.ofMinutes(5))
    //             )
    //     }

    @Bean
    fun redissonClient(): RedissonClient =
        Redisson.create()
}