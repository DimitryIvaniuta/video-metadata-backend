package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@RequiredArgsConstructor
@ReadingConverter
public class JsonNodeReadingConverter implements Converter<String, JsonNode> {
    private final ObjectMapper mapper;
    @Override
    public JsonNode convert(String source) {
        try {
            return source == null ? null : mapper.readTree(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON", e);
        }
    }
}
