package com.sap.class_planner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Login dengan kode sekolah + username + password. */
public record LoginRequest(
        @NotBlank @Size(max = 30) String schoolCode,
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 200) String password) {
}
