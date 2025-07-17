package com.github.dimitryivaniuta.videometadata.security;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.*;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * JWT encoder/decoder configuration using Nimbus JOSE.
 */
@Configuration
public class JwtConfig {

    /**
     * Decoder bean: verifies incoming JWTs signed with HS256.
     */
    @Bean
    @Primary
    public JwtDecoder customJwtDecoder(@Value("${JWT_SECRET}") String base64Secret) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        SecretKey key   = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    /**
     * Encoder bean: issues HS256‐signed JWTs.
     * Constructs an OctetSequenceKey with correct algorithm & use,
     * then exposes it via a simple JWKSource.
     */
    @Bean
    public JwtEncoder jwtEncoder(@Value("${JWT_SECRET}") String base64Secret) {
        // 1) Decode the Base64‐encoded secret into a SecretKey
        byte[] keyBytes     = Base64.getDecoder().decode(base64Secret);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        // 2) Build a JWK with algorithm HS256 and use=SIGNATURE
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretKey)
                .algorithm(JWSAlgorithm.HS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();

        // 3) Create a JWKSource that always returns our single JWK
        JWKSource<SecurityContext> jwkSource = (selector, context) ->
                selector.select(new JWKSet(jwk));

        // 4) Construct the NimbusJwtEncoder with that source
        return new NimbusJwtEncoder(jwkSource);
    }
}
