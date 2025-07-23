package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

/**
 * Converts a {@link VideoProvider} enum to its ordinal (int) for persistence.
 */
@WritingConverter
public final class VideoProviderWritingConverter implements Converter<VideoProvider, Integer> {

    @Override
    public Integer convert(VideoProvider source) {
        return source.ordinal();
    }
}
