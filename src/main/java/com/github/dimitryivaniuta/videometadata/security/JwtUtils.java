package com.github.dimitryivaniuta.videometadata.security;

import com.github.dimitryivaniuta.videometadata.config.SecurityJwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

/**
 * Utility for issuing and validating JWTs (HS256) using Spring Security OAuth2 JOSE support.
 */
@Component
@RequiredArgsConstructor
public class JwtUtils {

    /**
     * Decoder used to validate incoming tokens.
     */
    private final JwtDecoder jwtDecoder;

    /**
     * Encoder used to create new tokens.
     */
    private final JwtEncoder jwtEncoder;

    /**
     * Configuration properties for JWT (issuer, audience, secret, expiry).
     */
    private final SecurityJwtProperties props;

    /**
     * Generates an HS256‑signed JWT for the given username.
     *
     * @param username the subject (user) claim
     * @return the serialized JWT
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.getIssuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(props.getExpirationSeconds()))
                .subject(username)
                .audience(Collections.singletonList(props.getAudience()))
                .id(UUID.randomUUID().toString())
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    /**
     * Validates a JWT’s signature and expiration.
     *
     * @param token the JWT string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extracts the subject (username) from the JWT.
     *
     * @param token the JWT string
     * @return the subject claim
     */
    public String getUsername(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    /**
     * Extracts the JWT ID (jti) claim from the token.
     *
     * @param token the JWT string
     * @return the JWT ID
     */
    public String getJti(String token) {
        return jwtDecoder.decode(token).getId();
    }

    /**
     * Returns the configured expiration interval (in seconds).
     *
     * @return expiration in seconds
     */
    public long getExpirationSeconds() {
        return props.getExpirationSeconds();
    }

    /**
     * Returns a Duration representing token time‑to‑live.
     *
     * @return TTL as Duration
     */
    public Duration getTtl() {
        return Duration.ofSeconds(props.getExpirationSeconds());
    }
}
