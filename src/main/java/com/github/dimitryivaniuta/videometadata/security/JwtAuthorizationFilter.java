package com.github.dimitryivaniuta.videometadata.security;

import com.github.dimitryivaniuta.videometadata.service.RedisTokenService;
import com.github.dimitryivaniuta.videometadata.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
 * WebFilter for JWT-based authorization:
 * parses the token, validates it, checks revocation in Redis,
 * loads user details, and populates Reactor SecurityContext.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthorizationFilter implements WebFilter {

    private final JwtUtils             jwtUtils;
    private final RedisTokenService redisTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(
            final JwtUtils jwtUtils,
            final RedisTokenService redisTokenService,
            final UserDetailsServiceImpl userDetailsService
    ) {
        this.jwtUtils            = jwtUtils;
        this.redisTokenService   = redisTokenService;
        this.userDetailsService  = userDetailsService;
    }

    @Override
    public Mono<Void> filter(
            final ServerWebExchange exchange,
            final WebFilterChain    chain
    ) {
        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);
        Claims claims;
        try {
            claims = jwtUtils.parseClaims(token);
        } catch (Exception ex) {
            return chain.filter(exchange); // invalid token
        }

        String jti = jwtUtils.getJti(token);
        return redisTokenService.isTokenValid(jti)
                .flatMap(valid -> {
                    if (!valid) {
                        return chain.filter(exchange);
                    }
                    String username = claims.getSubject();
                    return userDetailsService.findByUsername(username)
                            .flatMap(userDetails -> {
                                var auth = new UsernamePasswordAuthenticationToken(
                                        userDetails, token, userDetails.getAuthorities()
                                );
                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder
                                                .withSecurityContext(Mono.just(new SecurityContextImpl(auth))));
                            });
                });
    }
}
