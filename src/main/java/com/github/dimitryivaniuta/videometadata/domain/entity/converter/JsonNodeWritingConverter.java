package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

@RequiredArgsConstructor
@WritingConverter
public class JsonNodeWritingConverter implements Converter<JsonNode, String> {
    private final ObjectMapper mapper;
    @Override
    public String convert(JsonNode source) {
        return source == null ? null : source.toString();
    }
}

