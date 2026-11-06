package com.sap.class_planner.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Verifikasi ID token dari Google Sign-In.
 * Sengaja bukan @Bean JwtDecoder supaya tidak bentrok dengan decoder JWT aplikasi.
 */
@Component
public class GoogleTokenVerifier {

    private static final String GOOGLE_JWKS_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> GOOGLE_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    private final NimbusJwtDecoder decoder;

    public GoogleTokenVerifier(@Value("${app.google.client-id}") String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("app.google.client-id belum diisi (env GOOGLE_CLIENT_ID)");
        }

        this.decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS_URI).build();

        OAuth2TokenValidator<Jwt> issuer = jwt ->
                GOOGLE_ISSUERS.contains(jwt.getClaimAsString("iss"))
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Issuer bukan Google", null));

        // Wajib: token harus dibuat untuk Client ID aplikasi ini, bukan aplikasi lain
        OAuth2TokenValidator<Jwt> audience = jwt ->
                jwt.getAudience() != null && jwt.getAudience().contains(clientId)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Audience tidak sesuai", null));

        this.decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(), issuer, audience));
    }

    /** @throws org.springframework.security.oauth2.jwt.JwtException kalau token tidak valid */
    public Jwt verify(String idToken) {
        return decoder.decode(idToken);
    }
}
