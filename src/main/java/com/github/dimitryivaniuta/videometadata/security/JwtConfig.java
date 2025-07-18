package com.github.dimitryivaniuta.videometadata.security;

import java.util.Base64;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtConfig {

    private static final String HMAC_ALGO = "HmacSHA256";

    /**
     * Decode incoming JWTs signed with our HS256 secret.
     */
    @Bean
    @Primary
    public JwtDecoder customJwtDecoder(@Value("${JWT_SECRET}") String base64Secret) {
        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);
        SecretKey key   = new SecretKeySpec(keyBytes, HMAC_ALGO);
        return NimbusJwtDecoder
                .withSecretKey(key)              // static HS‑256 builder :contentReference[oaicite:0]{index=0}
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    /**
     * Encode JWTs using HS256 and a JWKSource that holds our shared secret.
     * We must provide a JWKSource with keyUse=SIGNATURE and alg=HS256,
     * so NimbusJwtEncoder.selectJwk(...) can actually find it.
     */
    @Bean
    public JwtEncoder jwtEncoder(@Value("${JWT_SECRET}") String base64Secret) {
        byte[] keyBytes     = Base64.getDecoder().decode(base64Secret);
        SecretKey secretKey = new SecretKeySpec(keyBytes, HMAC_ALGO);

        // Build a single OctetSequence JWK with proper metadata
        OctetSequenceKey jwk = new OctetSequenceKey.Builder(secretKey)   // builder from Nimbus JOSE+JWT :contentReference[oaicite:1]{index=1}
                .algorithm(JWSAlgorithm.HS256)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSource<SecurityContext> jwkSource = (jwkSelector, context) ->
                jwkSelector.select(new JWKSet(jwk));

        // This ctor is the standard way to get an HS‑256 encoder :contentReference[oaicite:2]{index=2}
        return new NimbusJwtEncoder(jwkSource);
    }
}

