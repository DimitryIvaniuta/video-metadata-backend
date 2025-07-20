package com.github.dimitryivaniuta.videometadata.config;

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
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

@Configuration
@EnableCaching
@EnableConfigurationProperties(RedisPropertiesExtended.class)
@RequiredArgsConstructor
@Slf4j
public class RedisConfig {

    // Alphabetically sorted
    private final RedisPropertiesExtended redisProps;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public ClientResources lettuceClientResources() {
        return DefaultClientResources.create();
    }

    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory(ClientResources resources) {
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
        standalone.setHostName(redisProps.getHost());
        standalone.setPort(redisProps.getPort());
        standalone.setDatabase(redisProps.getDatabase());
        if (redisProps.getPassword() != null && !redisProps.getPassword().isBlank()) {
            standalone.setPassword(RedisPassword.of(redisProps.getPassword()));
        }

        LettuceClientConfiguration clientCfg = LettuceClientConfiguration.builder()
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

        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, clientCfg);
        factory.afterPropertiesSet();
        log.info("Reactive Redis factory initialized host={} port={} db={}",
                redisProps.getHost(), redisProps.getPort(), redisProps.getDatabase());
        return factory;
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper m = new ObjectMapper();
        m.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        m.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return m;
    }

    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(LettuceConnectionFactory lcf) {
        return new ReactiveStringRedisTemplate(lcf);
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(LettuceConnectionFactory lcf,
                                                                       ObjectMapper redisObjectMapper) {
        Jackson2JsonRedisSerializer<Object> jsonSer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);
        RedisSerializationContext<String, Object> ctx = RedisSerializationContext
                .<String, Object>newSerializationContext(RedisSerializer.string())
                .value(jsonSer)
                .hashKey(RedisSerializer.string())
                .hashValue(jsonSer)
                .build();
        return new ReactiveRedisTemplate<>(lcf, ctx);
    }
}
