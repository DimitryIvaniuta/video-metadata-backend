package com.github.dimitryivaniuta.videometadata.security;

import com.github.dimitryivaniuta.videometadata.service.RedisTokenService;
import com.github.dimitryivaniuta.videometadata.service.impl.UserDetailsServiceImpl;
import org.springframework.lang.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * WebFlux filter that:
 * <ol>
 *   <li>Extracts Bearer token from the Authorization header</li>
 *   <li>Validates signature & expiration via {@link JwtUtils#decode(String)} internally</li>
 *   <li>Retrieves the JTI via {@link JwtUtils#getJti(String)}</li>
 *   <li>Checks token validity in Redis via {@link RedisTokenService}</li>
 *   <li>Fetches the username via {@link JwtUtils#getUsername(String)}</li>
 *   <li>Loads UserDetails and writes a SecurityContext</li>
 * </ol>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthorizationFilter implements WebFilter {

    private final JwtUtils              jwtUtils;
    private final RedisTokenService redisTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(
            final JwtUtils jwtUtils,
            final RedisTokenService redisTokenService,
            final UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtUtils           = jwtUtils;
        this.redisTokenService  = redisTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public @NonNull Mono<Void> filter(
            @NonNull final ServerWebExchange exchange,
            @NonNull final WebFilterChain    chain
    ) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        // 1) Validate & decode token (will throw if invalid)
        try {
            jwtUtils.decode(token);
        } catch (Exception ex) {
            return chain.filter(exchange);
        }

        // 2) Extract JTI and check Redis for revocation/expiry
        String jti = jwtUtils.getJti(token);
        return redisTokenService.isTokenValid(jti)
                .flatMap(valid -> {
                    if (!valid) {
                        return chain.filter(exchange);
                    }
                    // 3) Extract username
                    String username = jwtUtils.getUsername(token);
                    // 4) Load UserDetails and populate SecurityContext
                    return userDetailsService.findByUsername(username)
                            .flatMap(userDetails -> {
                                var auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, token, userDetails.getAuthorities()
                                );
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
