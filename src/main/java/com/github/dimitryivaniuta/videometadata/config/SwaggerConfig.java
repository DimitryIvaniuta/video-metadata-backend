package com.github.dimitryivaniuta.videometadata.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI configuration for SpringDoc.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Defines the OpenAPI document metadata.
     *
     * @return OpenAPI model
     */
    @Bean
    public OpenAPI videoMetadataOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Video Metadata API")
                        .version("v1")
                        .description("Reactive backend to ingest and query video metadata")
                        .contact(new Contact()
                                .name("VideoMetadata Team")
                                .email("support@videometadata.example")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project README")
                        .url("https://example.com/docs"));
    }
}
