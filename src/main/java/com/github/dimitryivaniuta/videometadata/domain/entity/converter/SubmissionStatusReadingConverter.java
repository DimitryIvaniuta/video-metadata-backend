package com.github.dimitryivaniuta.videometadata.domain.entity.converter;

import com.github.dimitryivaniuta.videometadata.domain.model.SubmissionStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class SubmissionStatusReadingConverter implements Converter<Integer, SubmissionStatus> {
    @Override
    public SubmissionStatus convert(Integer source) {
        return SubmissionStatus.values()[source];
    }
}
