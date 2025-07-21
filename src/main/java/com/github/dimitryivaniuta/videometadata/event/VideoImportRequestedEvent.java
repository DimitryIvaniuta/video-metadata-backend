package com.github.dimitryivaniuta.videometadata.event;

import com.github.dimitryivaniuta.videometadata.web.dto.video.VideoImportRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a user requests a new video‑metadata import.
 */
@Getter
public class VideoImportRequestedEvent extends ApplicationEvent {

    private final String                   submissionId;
    private final String                   username;
    private final VideoImportRequest       request;

    public VideoImportRequestedEvent(Object source,
                                     String submissionId,
                                     String username,
                                     VideoImportRequest request) {
        super(source);
        this.submissionId = submissionId;
        this.username     = username;
        this.request      = request;
    }
}
