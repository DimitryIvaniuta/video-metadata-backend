package com.github.dimitryivaniuta.videometadata.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * JWT configuration for issuing and validating tokens.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /** HMAC secret (use a long, random value; prefer env var). */
    @NotBlank
    private String hmacSecret;

    /** Token issuer (iss). */
    @NotBlank
    private String issuer = "video-metadata";

    /** Optional audience (aud). */
    private String audience;

    /** Access token time-to-live. */
    @Positive
    private Duration accessTtl = Duration.ofMinutes(15);

    /** Refresh token time-to-live. */
    @Positive
    private Duration refreshTtl = Duration.ofDays(7);

    /** Allowed clock skew when validating tokens. */
    private Duration clockSkew = Duration.ofSeconds(60);
}
