package com.sap.class_planner.dto.response;

/** Hasil login: JWT aplikasi + data user. */
public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        CurrentUserResponse user) {
}
