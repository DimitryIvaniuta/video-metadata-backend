package com.github.dimitryivaniuta.videometadata.graphql.scalar;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.*;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Coercing for {@link ZonedDateTime} using ISO‑8601 strings
 * (e.g. "2025-07-18T09:31:00Z" or "2025-07-18T11:31:00+02:00[Europe/Warsaw]").
 * Uses the context-aware Coercing API introduced in graphql-java 20+ (tested on 24.1).
 */
public final class ZonedDateTimeCoercing implements Coercing<ZonedDateTime, String> {

    @Override
    public String serialize(@NotNull Object dataFetcherResult,
                            @NotNull GraphQLContext context,
                            @NotNull Locale locale) throws CoercingSerializeException {
        if (dataFetcherResult instanceof ZonedDateTime zdt) {
            return zdt.toString();
        }
        throw new CoercingSerializeException("Expected ZonedDateTime, got: " + dataFetcherResult);
    }

    @Override
    public ZonedDateTime parseValue(@NotNull Object input,
                                    @NotNull GraphQLContext context,
                                    @NotNull Locale locale) throws CoercingParseValueException {
        try {
            return ZonedDateTime.parse(String.valueOf(input));
        } catch (DateTimeParseException e) {
            throw new CoercingParseValueException("Invalid ISO-8601 ZonedDateTime: " + input, e);
        }
    }

    @Override
    public ZonedDateTime parseLiteral(@NotNull Value<?> input,
                                      @NotNull CoercedVariables variables,
                                      @NotNull GraphQLContext context,
                                      @NotNull Locale locale) throws CoercingParseLiteralException {
        if (input instanceof StringValue sv) {
            try {
                return ZonedDateTime.parse(sv.getValue());
            } catch (DateTimeParseException e) {
                throw new CoercingParseLiteralException("Invalid ISO-8601 ZonedDateTime literal: " + sv.getValue(), e);
            }
        }
        throw new CoercingParseLiteralException("Expected StringValue for ZonedDateTime literal, got: " + input);
    }
}
