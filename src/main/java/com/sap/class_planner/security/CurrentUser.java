package com.sap.class_planner.security;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Identitas user dari JWT, untuk dipakai di controller lain.
 * schoolId SELALU diambil dari sini, jangan dari request body/path.
 *
 * Contoh:
 *   public X myEndpoint(@AuthenticationPrincipal Jwt jwt) {
 *       CurrentUser me = CurrentUser.from(jwt);
 *       service.doSomething(me.schoolId(), ...);
 *   }
 */
public record CurrentUser(Long userId, String role, Long schoolId, Long teacherId, Long studentId) {

    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString("role"),
                asLong(jwt.getClaim("schoolId")),
                asLong(jwt.getClaim("teacherId")),
                asLong(jwt.getClaim("studentId")));
    }

    private static Long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}
