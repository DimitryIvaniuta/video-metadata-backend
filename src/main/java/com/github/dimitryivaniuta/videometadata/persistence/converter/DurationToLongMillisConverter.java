package com.github.dimitryivaniuta.videometadata.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

/**
 * Converts {@link Duration} <-> BIGINT milliseconds for persistence.
 * Ensures millisecond precision (sub-millisecond truncated).
 */
@Converter(autoApply = true)
public class DurationToLongMillisConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(final Duration attribute) {
        return attribute == null ? null : attribute.toMillis();
    }

    @Override
    public Duration convertToEntityAttribute(final Long dbData) {
        return dbData == null ? null : Duration.ofMillis(dbData);
    }
}
