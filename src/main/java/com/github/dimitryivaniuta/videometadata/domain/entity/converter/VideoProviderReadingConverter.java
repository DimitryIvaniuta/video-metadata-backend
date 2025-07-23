package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import com.github.dimitryivaniuta.videometadata.domain.model.VideoProvider;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converts an integer (ordinal) from the database back to {@link VideoProvider}.
 */
@ReadingConverter
public final class VideoProviderReadingConverter implements Converter<Integer, VideoProvider> {

    @Override
    public VideoProvider convert(Integer source) {
        VideoProvider[] values = VideoProvider.values();
        if (source < 0 || source >= values.length) {
            throw new IllegalArgumentException(
                    "Unknown VideoProvider ordinal: " + source + ". Check DB data / enum order!");
        }
        return values[source];
    }
}