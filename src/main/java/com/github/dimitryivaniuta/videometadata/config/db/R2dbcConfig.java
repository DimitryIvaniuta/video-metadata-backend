package com.github.dimitryivaniuta.videometadata.config.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dimitryivaniuta.videometadata.domain.entity.converter.*;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory,
                                                         ObjectMapper mapper) {
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(
                dialect,
                new SubmissionStatusWritingConverter(),
                new SubmissionStatusReadingConverter(),
                new VideoProviderWritingConverter(),
                new VideoProviderReadingConverter(),
                new JsonNodeWritingConverter(mapper),
                new JsonNodeReadingConverter(mapper),
                new StringArrayToRoleSetConverter(),
                new RoleSetToStringArrayConverter()
        );
    }
}