package com.github.dimitryivaniuta.videometadata.security;

import com.github.dimitryivaniuta.videometadata.service.RedisTokenService;
import com.github.dimitryivaniuta.videometadata.service.impl.UserDetailsServiceImpl;
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
 * Reactive filter that:
 * <ul>
 *   <li>Extracts Bearer JWT from Authorization header</li>
 *   <li>Validates & parses via {@link JwtUtils}</li>
 *   <li>Checks JTI presence in Redis (revocation store)</li>
 *   <li>Loads Spring Security {@code UserDetails}</li>
 *   <li>Populates Reactor SecurityContext</li>
 * </ul>
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
        this.jwtUtils          = jwtUtils;
        this.redisTokenService = redisTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Void> filter(
            final ServerWebExchange exchange,
            final WebFilterChain    chain
    ) {
        String auth = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = auth.substring(7);
        // Decode & validate signature/expiry
        org.springframework.security.oauth2.jwt.Jwt jwt;
        try {
            jwt = jwtUtils.decode(token);
        } catch (Exception ex) {
            return chain.filter(exchange);
        }

        String jti = jwt.getId();
        return redisTokenService.isTokenValid(jti)
                .flatMap(valid -> {
                    if (!valid) {
                        return chain.filter(exchange);
                    }
                    String username = jwt.getSubject();
                    return userDetailsService.findByUsername(username)
                            .flatMap(userDetails -> {
                                // Build Authentication and set it in Reactor Context
                                var authToken = new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        token,
                                        userDetails.getAuthorities()
                                );
                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder
                                                .withSecurityContext(Mono.just(new SecurityContextImpl(authToken))));
                            });
                });
    }
}
