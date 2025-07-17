package com.github.dimitryivaniuta.videometadata.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility for generating and parsing JWTs.
 */
@Component
public class JwtUtils {

    private final Key    signingKey;
    private final long   expirationMs;

    public JwtUtils(
            @Value("${JWT_SECRET}") final String base64Secret,
            @Value("${JWT_EXPIRATION_MS:3600000}") final long expirationMs
    ) {
        byte[] keyBytes   = Decoders.BASE64.decode(base64Secret);
        this.signingKey   = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    /** Generates a signed JWT with a unique JTI and configured TTL. */
    public String generateToken(final String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(username)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Parses and validates a JWT, returning its claims. */
    public Claims parseClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Extracts the username (subject) from a JWT. */
    public String getUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    /** Extracts the unique token ID (JTI) from a JWT. */
    public String getJti(final String token) {
        return parseClaims(token).getId();
    }

    /** Returns the configured token TTL as a Duration. */
    public Duration getExpiration() {
        return Duration.ofMillis(expirationMs);
    }
}
