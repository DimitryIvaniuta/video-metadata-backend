package com.github.dimitryivaniuta.videometadata.graphql.scalar;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
//import graphql.schema.CoercedVariables;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Coercing for {@link java.time.Duration} using ISO-8601 text form (e.g. "PT1H2M", "PT25.345S").
 * Implements the non-deprecated context-aware methods introduced in graphql-java 20+.
 */
public final class DurationCoercing implements Coercing<Duration, String> {

    @Override
    public String serialize(@NotNull Object dataFetcherResult,
                            @NotNull GraphQLContext graphQLContext,
                            @NotNull Locale locale) throws CoercingSerializeException {
        if (dataFetcherResult instanceof Duration d) {
            return d.toString(); // ISO-8601
        }
        throw new CoercingSerializeException("Expected Duration, got: " + dataFetcherResult);
    }

    @Override
    public Duration parseValue(@NotNull Object input,
                               @NotNull GraphQLContext graphQLContext,
                               @NotNull Locale locale) throws CoercingParseValueException {
        try {
            return Duration.parse(String.valueOf(input));
        } catch (DateTimeParseException e) {
            throw new CoercingParseValueException("Invalid ISO-8601 duration: " + input, e);
        }
    }

    @Override
    public Duration parseLiteral(@NotNull Value<?> input,
                                 @NotNull CoercedVariables variables,
                                 @NotNull GraphQLContext graphQLContext,
                                 @NotNull Locale locale) throws CoercingParseLiteralException {
        if (input instanceof StringValue sv) {
            try {
                return Duration.parse(sv.getValue());
            } catch (DateTimeParseException e) {
                throw new CoercingParseLiteralException("Invalid ISO-8601 duration literal: " + sv.getValue(), e);
            }
        }
        throw new CoercingParseLiteralException("Expected StringValue for Duration literal, got: " + input);
    }
}
