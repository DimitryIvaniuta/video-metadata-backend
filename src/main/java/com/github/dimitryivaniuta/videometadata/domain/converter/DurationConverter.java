package com.github.dimitryivaniuta.videometadata.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

/**
 * Converts between {@link Duration} and {@link Long} (milliseconds)
 * so Hibernate will map it to BIGINT.
 */
@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(final Duration attribute) {
        return (attribute == null ? null : attribute.toMillis());
    }

    @Override
    public Duration convertToEntityAttribute(final Long dbData) {
        return (dbData == null ? null : Duration.ofMillis(dbData));
    }
}
