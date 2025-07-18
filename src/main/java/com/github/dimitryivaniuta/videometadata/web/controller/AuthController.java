package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.security.JwtUtils;
import com.github.dimitryivaniuta.videometadata.service.RedisTokenService;
import com.nimbusds.jose.JOSEException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Authentication REST API: login issues JWTs, logout revokes them.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ReactiveAuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final RedisTokenService tokenService;

    public AuthController(
            ReactiveAuthenticationManager authManager,
            JwtUtils jwtUtils,
            RedisTokenService tokenService
    ) {
        this.authManager  = authManager;
        this.jwtUtils     = jwtUtils;
        this.tokenService = tokenService;
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token) {}

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(@RequestBody LoginRequest req) {
        return authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(req.username(), req.password())
                )
                .cast(Authentication.class)
                .flatMap(auth -> {
                    try {
                        // 1) generate token
                        String jwt = jwtUtils.generateToken(auth.getName());
                        // 2) extract jti
                        String jti = jwtUtils.getJti(jwt);
                        // 3) store in Redis using the TTL from JwtUtils
                        return tokenService
                                .storeToken(jti, jwtUtils.getTtl())
                                .map(stored -> ResponseEntity.ok(new LoginResponse(jwt)));
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("JWT generation failed", e));
                    }
                })
                .onErrorResume(BadCredentialsException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build())
                );
    }


    /**
     * Revokes the current JWT by removing its JTI from Redis.
     * Returns 200 OK on success, or 400 Bad Request if no valid token found.
     */
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout() {
        // 1) Grab the SecurityContext as a typed Mono
        Mono<org.springframework.security.core.context.SecurityContext> ctxMono =
                ReactiveSecurityContextHolder.getContext();

        // 2) Transform into a Mono<ResponseEntity<Void>>
        Mono<ResponseEntity<Void>> result = ctxMono
                .flatMap(ctx -> {
                    Authentication auth = ctx.getAuthentication();
                    if (auth == null) {
                        // no auth → 400
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    Object creds = auth.getCredentials();
                    if (!(creds instanceof String token)) {
                        // bad credentials → 400
                        return Mono.just(ResponseEntity.badRequest().build());
                    }
                    String jti = jwtUtils.getJti(token);
                    // revoke in Redis
                    return tokenService.revokeToken(jti)
                            .flatMap(revoked -> {
                                if (Boolean.TRUE.equals(revoked)) {
                                    return Mono.just(
                                            ResponseEntity.<Void>ok().build()
                                    );
                                } else {
                                    return Mono.just(
                                            ResponseEntity.<Void>badRequest().build()
                                    );
                                }
                            });
                });

        // 3) If SecurityContext was empty, default to 400
        return result.defaultIfEmpty(ResponseEntity.<Void>badRequest().build());
    }

}

