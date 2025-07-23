package com.github.dimitryivaniuta.videometadata.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
//import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Exposes a JwtEncoder backed by your HS256 secret.
 *
 * We intentionally do NOT define a JwtDecoder bean here—letting
 * Spring Boot auto‑configure JwtDecoder based on
 * spring.security.oauth2.resourceserver.jwt.secret-key.
 */
@Configuration
@RequiredArgsConstructor
public class JwtEncoderConfig {

    private final SecurityJwtProperties jwtProps;

    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProps.getSecret());
        var secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        var jwk = new OctetSequenceKey.Builder(secretKey)
                .keyID("video-metadata-hs256-key")
                .build();

        return new NimbusJwtEncoder((jwkSelector, context) ->
                jwkSelector.select(new JWKSet(jwk)));
    }
}