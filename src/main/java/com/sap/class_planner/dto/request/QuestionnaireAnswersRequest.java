package com.sap.class_planner.dto.request;

import jakarta.validation.constraints.Size;

/** Dipakai untuk simpan draf (boleh belum lengkap) maupun submit (dicek lengkap di service). */
public record QuestionnaireAnswersRequest(
        @Size(max = 100, message = "Cita-cita maksimal 100 karakter") String aspiration,
        @Size(max = 1000, message = "Penunjang cita-cita maksimal 1000 karakter") String supportingActivities,
        Long major1Id,
        Long major2Id) {
}
