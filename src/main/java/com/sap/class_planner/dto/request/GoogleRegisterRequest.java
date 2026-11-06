package com.sap.class_planner.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Aktivasi akun murid saat login Google pertama.
 * idToken = token Google yang sama dengan percobaan login sebelumnya.
 * birthDate dikirim dalam format ISO: "2009-05-14".
 */
public record GoogleRegisterRequest(
        @NotBlank String idToken,
        @NotBlank @Size(max = 30) String schoolCode,
        @NotBlank @Size(max = 30) String studentNumber,
        @NotNull @Past LocalDate birthDate) {
}
