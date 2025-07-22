package com.github.dimitryivaniuta.videometadata.service;

import com.github.dimitryivaniuta.videometadata.event.VideoImportRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for import requests and delegates to the processor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoImportEventListener {

    private final VideoImportProcessor processor;

    @EventListener
    public void onVideoImportRequested(VideoImportRequestedEvent ev) {
        processor.processImport(ev.getUsername(), ev.getRequest());
    }
}