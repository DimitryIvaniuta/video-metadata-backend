package com.github.dimitryivaniuta.videometadata.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Logs each incoming HTTP request method and URI.
 */
@Configuration
public class LoggingConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LoggingConfiguration.class);

    public WebFilter requestLoggingFilter() {
        return new WebFilter() {
            @NotNull
            @Override
            public Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {
                log.info("Incoming request: {} {}", exchange.getRequest().getMethod(),
                        exchange.getRequest().getURI());
                return chain.filter(exchange);
            }
        };
    }
}
