package com.github.dimitryivaniuta.videometadata.security;

import java.time.Instant;
import java.time.Duration;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Helper to encode and decode JWTs using Nimbus under Spring Security.
 */
@Component
public class JwtUtils {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    /**
     * token time‑to‑live as Duration.
     */
    @Getter
    private final Duration   ttl;

    public JwtUtils(
            final JwtEncoder jwtEncoder,
            final JwtDecoder jwtDecoder,
            @Value("${JWT_EXPIRATION_SECONDS:3600}") final long expirationSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.ttl        = Duration.ofSeconds(expirationSeconds);
    }

    /**
     * Generate a signed JWT with subject=username, JTI, issuedAt and expiresAt.
     *
     * @param username the principal name
     * @return the compact JWT string
     */
    public String generateToken(final String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("video-metadata-backend")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(username)
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    /**
     * Decode and validate a JWT, throwing if invalid.
     *
     * @param token the compact JWT
     * @return the JWT object with headers and claims
     */
    public Jwt decode(final String token) {
        return jwtDecoder.decode(token);
    }

    /** @return the subject (username) from a token */
    public String getUsername(final String token) {
        return decode(token).getSubject();
    }

    /** @return the JTI from a token */
    public String getJti(final String token) {
        return decode(token).getId();
    }

    @Configuration
    static class JwtConfig {

        /**
         * Decoder bean using a symmetric HMAC key.
         */
        @Bean
        public JwtDecoder jwtDecoder(@Value("${JWT_SECRET}") final String base64Secret) {
            byte[] keyBytes = java.util.Base64.getDecoder().decode(base64Secret);
            SecretKey key   = new SecretKeySpec(keyBytes, "HmacSHA256");
            return NimbusJwtDecoder.withSecretKey(key).build();
        }

        /**
         * Encoder bean using the same symmetric HMAC key and Nimbus's ImmutableSecret.
         */
        @Bean
        public JwtEncoder jwtEncoder(@Value("${JWT_SECRET}") final String base64Secret) {
            byte[]     keyBytes  = java.util.Base64.getDecoder().decode(base64Secret);
            SecretKey  secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
            ImmutableSecret<SecurityContext> jwkSource = new ImmutableSecret<>(secretKey);
            return new NimbusJwtEncoder(jwkSource);
        }
    }
}
