package com.sap.class_planner.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Login dengan ID token dari Google Sign-In. */
public record GoogleLoginRequest(@NotBlank String idToken) {
}
