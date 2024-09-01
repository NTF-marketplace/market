package com.api.market.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisClusterConfiguration
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableRedisRepositories
class RedisConfig {
    @Bean
    fun lettuceConnectionFactory(): LettuceConnectionFactory {
        val redisClusterConfiguration = RedisClusterConfiguration(
            listOf(
                "localhost:6379",
                "localhost:6380",
                "localhost:6381",
                "localhost:6382",
                "localhost:6383",
                "localhost:6384",
                "localhost:6385",
                "localhost:6386",
                "localhost:6387",
                "localhost:6388",
            )
        )
        val clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofSeconds(10))
            .build()

        redisClusterConfiguration.password = RedisPassword.of("bitnami")
        redisClusterConfiguration.maxRedirects = 3
        return LettuceConnectionFactory(redisClusterConfiguration, clientConfig)
    }

    @Bean
    fun redisObjectMapper(): ObjectMapper {
        return jacksonObjectMapper().registerModule(
            KotlinModule.Builder()
            .build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Bean
    fun reactiveRedisTemplate(lettuceConnectionFactory: LettuceConnectionFactory): ReactiveRedisTemplate<String, Any> {
        val keySerializer = StringRedisSerializer()
        val valueSerializer = GenericJackson2JsonRedisSerializer(redisObjectMapper())

        val serializationContext = RedisSerializationContext.newSerializationContext<String, Any>()
            .key(keySerializer)
            .value(valueSerializer)
            .hashKey(keySerializer)
            .hashValue(valueSerializer)
            .build()

        return ReactiveRedisTemplate(lettuceConnectionFactory, serializationContext)
    }
}