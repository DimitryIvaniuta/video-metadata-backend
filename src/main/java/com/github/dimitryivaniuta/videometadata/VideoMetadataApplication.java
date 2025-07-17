package com.github.dimitryivaniuta.videometadata;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

/**
 * Entry point for the Video Metadata Application.
 * <p>
 * Enables asynchronous method execution and caching, and bootstraps the Spring Boot context.
 * </p>
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@EnableCaching
public class VideoMetadataApplication {

    /**
     * Main method to bootstrap and run the Spring Boot application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(final String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(VideoMetadataApplication.class, args);

        Environment env = context.getEnvironment();
        String[] activeProfiles = env.getActiveProfiles();
        log.info("Video Metadata Application started with active profiles: {}", Arrays.toString(activeProfiles));
    }
}
