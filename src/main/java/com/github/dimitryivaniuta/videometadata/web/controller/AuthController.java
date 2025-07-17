package com.github.dimitryivaniuta.videometadata.web.controller;

import com.github.dimitryivaniuta.videometadata.security.JwtUtils;
import com.github.dimitryivaniuta.videometadata.service.RedisTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Authentication REST API: logs in users and issues JWTs.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ReactiveAuthenticationManager authManager;
    private final JwtUtils jwtUtils;
    private final RedisTokenService tokenService;

    public AuthController(
            final ReactiveAuthenticationManager authManager,
            final JwtUtils jwtUtils,
            final RedisTokenService tokenService
    ) {
        this.authManager   = authManager;
        this.jwtUtils      = jwtUtils;
        this.tokenService  = tokenService;
    }

    record LoginRequest(String username, String password) {}

    record LoginResponse(String token) {}

    /**
     * Authenticates credentials and returns a JWT.
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(
            @RequestBody final LoginRequest req
    ) {
        return Mono.just(new UsernamePasswordAuthenticationToken(
                        req.username(), req.password()))
                .flatMap(authManager::authenticate)
                .cast(Authentication.class)
                .flatMap(auth -> {
                    String jwt = jwtUtils.generateToken(auth.getName());
                    String jti = jwtUtils.getJti(jwt);
                    return tokenService.storeToken(jti, jwtUtils.getExpiration())
                            .thenReturn(ResponseEntity.ok(new LoginResponse(jwt)));
                });
    }
}
