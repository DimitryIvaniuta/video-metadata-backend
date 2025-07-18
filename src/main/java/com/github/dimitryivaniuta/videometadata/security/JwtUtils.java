package com.github.dimitryivaniuta.videometadata.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private final byte[] secret;
    private final long   expirationSeconds;

    public JwtUtils(
            @Value("${JWT_SECRET}") String base64Secret,
            @Value("${JWT_EXPIRATION_SECONDS:3600}") long expirationSeconds
    ) {
        this.secret            = Base64.getDecoder().decode(base64Secret);
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Generate an HS256‑signed JWT.
     */
    public String generateToken(String username) throws JOSEException {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("videometadata-api")
                .subject(username)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        JWSSigner signer = new MACSigner(secret);
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    /**
     * Validate signature & expiry.
     */
    public boolean validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret);
            return jwt.verify(verifier)
                    && jwt.getJWTClaimsSet().getExpirationTime().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract the subject (username).
     */
    public String getUsername(String token) throws java.text.ParseException {
        return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
    }

    /**
     * Extract the token’s JTI.
     */
    public String getJti(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse JTI from JWT", e);
        }
    }

    /**
     * Expose the token time‑to‑live as a Duration, for Redis.
     */
    public Duration getTtl() {
        return Duration.ofSeconds(expirationSeconds);
    }
}
