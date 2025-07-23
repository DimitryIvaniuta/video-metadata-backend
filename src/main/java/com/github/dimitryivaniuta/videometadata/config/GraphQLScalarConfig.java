package com.github.dimitryivaniuta.videometadata.config;

import com.github.dimitryivaniuta.videometadata.graphql.scalar.ZonedDateTimeCoercing;
import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import com.github.dimitryivaniuta.videometadata.graphql.scalar.DurationCoercing;

/**
 * Registers custom GraphQL scalars (e.g. Instant, Duration) with Spring GraphQL.
 */
@Configuration
public class GraphQLScalarConfig {

    /**
     * Instant scalar for timestamps.
     */
//    private final GraphQLScalarType instantScalar = ExtendedScalars.Instant;
    private static final GraphQLScalarType ZONED_DATE_TIME = GraphQLScalarType.newScalar()
            .name("ZonedDateTime")
            .description("ISO-8601 date-time with zone")
            .coercing(new ZonedDateTimeCoercing())
            .build();
    /**
     * Duration scalar for Java Duration values.
     */
//    private final GraphQLScalarType durationScalar = ExtendedScalars.Duration;
    private static final GraphQLScalarType DURATION  = GraphQLScalarType.newScalar()
            .name("Duration")
            .description("ISO-8601 duration, e.g. PT1H2M or PT25.345S")
            .coercing(new DurationCoercing())
            .build();

    /**
     * Generic JSON scalar (optional, in case you need dynamic objects).
     */
    private final GraphQLScalarType jsonScalar = ExtendedScalars.Json;
    /**
     * DateTime scalar (ZonedDateTime/OffsetDateTime).
     */
    private final GraphQLScalarType dateTimeScalar = ExtendedScalars.DateTime;

    /**
     * Wire custom scalars into GraphQL runtime.
     *
     * @return RuntimeWiringConfigurer bean
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
//                .scalar(instantScalar)
//                .scalar(durationScalar)
                .scalar(dateTimeScalar)
                .scalar(jsonScalar)
                .scalar(DURATION)
                .scalar(ZONED_DATE_TIME);
    }
}
