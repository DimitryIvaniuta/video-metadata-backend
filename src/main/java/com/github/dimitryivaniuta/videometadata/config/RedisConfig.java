package com.github.dimitryivaniuta.videometadata.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configures reactive Redis support:
 * <ul>
 *   <li>Lettuce factory with custom timeouts</li>
 *   <li>Jackson‐based RedisTemplate for object serialization</li>
 *   <li>String‐only template for simple operations</li>
 * </ul>
 */
@Configuration
@EnableCaching
//@EnableConfigurationProperties(RedisPropertiesExtended.class)
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    /**
     * Extended Redis properties (host, port, timeouts, etc).
     */
    private final RedisPropertiesExtended redisProps;

    /**
     * Shared Lettuce {@link ClientResources}, reused by all connection factories.
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    /**
     * Primary reactive Redis connection factory, using Lettuce.
     *
     * @param resources shared {@link ClientResources}
     * @return configured {@link LettuceConnectionFactory}
     */
    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory(ClientResources resources) {
        // Standalone Redis setup
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(redisProps.getHost());
        standalone.setPort(redisProps.getPort());
        standalone.setDatabase(redisProps.getDatabase());
        if (redisProps.getPassword() != null && !redisProps.getPassword().isBlank()) {
            standalone.setPassword(RedisPassword.of(redisProps.getPassword()));
        }

        // Lettuce timeouts & options
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientResources(resources)
                .commandTimeout(redisProps.getTimeout())
                .clientOptions(ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .connectTimeout(redisProps.getConnectTimeout())
                                .build())
                        .timeoutOptions(TimeoutOptions.enabled())
                        .autoReconnect(true)
                        .build())
                .build();

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, clientConfig);
        factory.afterPropertiesSet();
        log.info("Initialized Redis @ {}:{}, db={}",
                redisProps.getHost(), redisProps.getPort(), redisProps.getDatabase());
        return factory;
    }

    /**
     * ObjectMapper for Redis value serialization, with polymorphic type support.
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /**
     * Simple {@link ReactiveStringRedisTemplate} for String‑to‑String operations.
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            LettuceConnectionFactory connectionFactory) {
        return new ReactiveStringRedisTemplate(connectionFactory);
    }

    /**
     * Jackson‐backed {@link ReactiveRedisTemplate} for Object serialization.
     *
     * @param connectionFactory the Lettuce factory
     * @param objectMapper      the Redis‑configured ObjectMapper
     */
    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
            LettuceConnectionFactory connectionFactory,
            ObjectMapper objectMapper) {

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        RedisSerializationContext<String,Object> context =
                RedisSerializationContext.<String,Object>newSerializationContext(new StringRedisSerializer())
                        .value(serializer)
                        .hashKey(new StringRedisSerializer())
                        .hashValue(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
