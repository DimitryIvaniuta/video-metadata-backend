package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import com.github.dimitryivaniuta.videometadata.domain.model.SubmissionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.convert.ReadingConverter;

/** Write enum as ordinal (smallint). */
@WritingConverter
public class SubmissionStatusWritingConverter implements Converter<SubmissionStatus, Integer> {
    @Override
    public Integer convert(SubmissionStatus source) {
        return source.ordinal();
    }
}
