package com.github.dimitryivaniuta.videometadata.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * Global WebFlux configuration (CORS, base path, etc.).
 */
@Configuration
@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * Whether credentials are allowed in CORS requests.
     */
    private boolean allowCredentials = true;

    /**
     * Allowed headers pattern(s).
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * Allowed HTTP methods.
     */
    private List<String> allowedMethods = Arrays.asList(
            HttpMethod.GET.name(),
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.DELETE.name(),
            HttpMethod.OPTIONS.name()
    );

    /**
     * Allowed origins.
     */
    private List<String> allowedOrigins = List.of("http://localhost:3000");

    /**
     * Exposed headers.
     */
    private List<String> exposedHeaders = List.of("Authorization", "Content-Type");

    /**
     * Max-Age preflight cache (seconds).
     */
    private long maxAge = Duration.ofHours(1).getSeconds();

    /**
     * Base path for all WebFlux controllers.
     */
    @NotBlank
    private String basePath = "/api";

    @Override
    public void configurePathMatching(@NotNull PathMatchConfigurer configurer) {
        if (StringUtils.hasText(basePath) && !"/".equals(basePath)) {
            configurer.addPathPrefix(basePath, c -> true);
        }
    }

    /**
     * Provide a reactive CORS source so Spring Security and WebFlux can use it.
     *
     * @return cors configuration source
     */
    @org.springframework.context.annotation.Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(allowedOrigins);
        cfg.setAllowedMethods(allowedMethods);
        cfg.setAllowedHeaders(allowedHeaders);
        cfg.setExposedHeaders(exposedHeaders);
        cfg.setAllowCredentials(allowCredentials);
        cfg.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
