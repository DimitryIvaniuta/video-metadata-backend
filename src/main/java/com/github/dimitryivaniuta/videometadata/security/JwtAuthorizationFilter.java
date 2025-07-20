package com.github.dimitryivaniuta.videometadata.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * A WebFlux filter that:
 * <ol>
 *   <li>Extracts the Bearer JWT</li>
 *   <li>Validates signature & expiry via {@link JwtUtils#validateToken(String)}</li>
 *   <li>Extracts JTI via {@link JwtUtils#getJti(String)}</li>
 *   <li>Checks Redis for revocation via {@link RedisTokenService#isTokenValid(String)}</li>
 *   <li>Loads user details and writes a SecurityContext</li>
 * </ol>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthorizationFilter implements WebFilter {

    private final JwtUtils jwtUtils;
    private final RedisTokenService tokenService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(
            JwtUtils jwtUtils,
            RedisTokenService tokenService,
            UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtUtils = jwtUtils;
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public @NonNull Mono<Void> filter(
            @NonNull ServerWebExchange exchange,
            @NonNull WebFilterChain chain
    ) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        // 1) No header or not Bearer → continue unauthenticated
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        // 2) Validate signature + expiry
        boolean signatureOk = jwtUtils.validateToken(token);
        if (!signatureOk) {
            return chain.filter(exchange);
        }

        // 3) Extract JTI (safely)
        String jti;
        try {
            jti = jwtUtils.getJti(token);
        } catch (Exception e) {
            // malformed JWT → skip auth
            return chain.filter(exchange);
        }

        // 4) Check revocation/expiry in Redis
        return tokenService.isTokenValid(jti)
                .flatMap(valid -> {
                    if (!valid) {
                        return chain.filter(exchange);
                    }
                    // 5) Extract username & load UserDetails
                    String username;
                    try {
                        username = jwtUtils.getUsername(token);
                    } catch (Exception e) {
                        return chain.filter(exchange);
                    }
                    return userDetailsService.findByUsername(username)
                            .flatMap(userDetails -> {
                                var auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, token, userDetails.getAuthorities()
                                );
                                // 6) Write into Reactor SecurityContext
                                return chain.filter(exchange)
                                        .contextWrite(
                                                ReactiveSecurityContextHolder.withSecurityContext(
                                                        Mono.just(new SecurityContextImpl(auth))
                                                )
                                        );
                            });
                });
    }
}
