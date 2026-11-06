package com.sap.class_planner.service;

import com.sap.class_planner.entity.AppUser;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

/** Menerbitkan JWT milik aplikasi setelah login berhasil. */
@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${app.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    public long expirationSeconds() {
        return expirationSeconds;
    }

    public String issue(AppUser user, Long teacherId, Long studentId) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("class-planner")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(user.getId().toString())
                .claim("role", user.getRole().name());

        // Klaim dengan nilai null tidak boleh dimasukkan
        if (user.getSchoolId() != null) {
            claims.claim("schoolId", user.getSchoolId());
        }
        if (teacherId != null) {
            claims.claim("teacherId", teacherId);
        }
        if (studentId != null) {
            claims.claim("studentId", studentId);
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }
}
