package com.github.dimitryivaniuta.videometadata.security.token;

import com.github.dimitryivaniuta.videometadata.config.JwtProperties;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * HMAC-based JWT issuer/validator (HS512).
 * Wraps all Nimbus checked exceptions into {@link AuthTokenException}.
 */
@Service
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {

    private final JwtProperties props;

    /** Thread-safe signer/verifier (immutable once created). */
    private volatile JWSSigner signer;
    private volatile JWSVerifier verifier;

    @Override
    public Mono<TokenPair> issueTokens(String principal) {
        return initCryptoIfNeeded()
                .then(Mono.defer(() -> {
                    Instant now = Instant.now();
                    SignedJWT access = buildJwt(principal, now, now.plus(props.getAccessTtl()), Map.of("typ", "access"));
                    SignedJWT refresh = buildJwt(principal, now, now.plus(props.getRefreshTtl()), Map.of("typ", "refresh"));

                    return Mono.zip(sign(access), sign(refresh))
                            .map(t -> new TokenPair(
                                    t.getT1(),
                                    t.getT2(),
                                    now.plus(props.getAccessTtl()),
                                    now.plus(props.getRefreshTtl())
                            ));
                }));
    }

    @Override
    public Mono<TokenPair> refresh(String refreshToken) {
        return validate(refreshToken)
                .flatMap(claims -> {
                    Object typ = claims.additional().get("typ");
                    if (!"refresh".equals(typ)) {
                        return Mono.error(new AuthTokenException("Token is not a refresh token"));
                    }
                    return issueTokens(claims.subject());
                });
    }

    @Override
    public Mono<TokenClaims> validate(String token) {
        return initCryptoIfNeeded()
                .then(Mono.fromCallable(() -> parseJwt(token)))
                .flatMap(jwt -> Mono.fromCallable(() -> verify(jwt))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new AuthTokenException("Signature invalid")))
                        .thenReturn(jwt))
                .map(this::toClaims);
    }

    /* ---------------------- private helpers ---------------------- */

    private Mono<Void> initCryptoIfNeeded() {
        if (signer != null && verifier != null) return Mono.empty();

        return Mono.fromRunnable(() -> {
            if (signer == null || verifier == null) {
                byte[] secret = props.getHmacSecret().getBytes(StandardCharsets.UTF_8);
                try {
                    signer = new MACSigner(secret);
                    verifier = new MACVerifier(secret);
                } catch (com.nimbusds.jose.JOSEException e) {
                    throw new AuthTokenException("Invalid HMAC secret length", e);
                }
            }
        });
    }

    private SignedJWT buildJwt(String sub, Instant iat, Instant exp, Map<String, Object> extra) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(props.getIssuer())
                .subject(sub)
                .issueTime(Date.from(iat))
                .expirationTime(Date.from(exp));
        extra.forEach(builder::claim);

        return new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.HS512).type(JOSEObjectType.JWT).build(),
                builder.build()
        );
    }

    private Mono<String> sign(SignedJWT jwt) {
        return Mono.fromCallable(() -> {
            try {
                jwt.sign(signer);
                return jwt.serialize();
            } catch (JOSEException e) {
                throw new AuthTokenException("Failed to sign JWT", e);
            }
        });
    }

    private SignedJWT parseJwt(String token) {
        try {
            return SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new AuthTokenException("Invalid JWT format", e);
        }
    }

    private boolean verify(SignedJWT jwt) {
        try {
            return jwt.verify(verifier);
        } catch (JOSEException e) {
            throw new AuthTokenException("Failed to verify JWT", e);
        }
    }

    private TokenClaims toClaims(SignedJWT jwt) {
        try {
            JWTClaimsSet cs = jwt.getJWTClaimsSet();
            Instant exp = cs.getExpirationTime().toInstant();
            if (Instant.now().isAfter(exp)) {
                throw new AuthTokenException("Token expired");
            }
            return new TokenClaims(
                    cs.getSubject(),
                    cs.getIssueTime().toInstant(),
                    exp,
                    cs.getClaims()
            );
        } catch (ParseException e) {
            throw new AuthTokenException("Failed to parse JWT claims", e);
        }
    }
}
