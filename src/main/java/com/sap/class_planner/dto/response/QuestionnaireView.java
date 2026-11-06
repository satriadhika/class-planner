package com.sap.class_planner.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Isi halaman kuesioner murid.
 * status: NOT_STARTED | DRAFT | SUBMITTED. editable = false kalau sudah dikirim atau periode kuesioner tutup.
 */
public record QuestionnaireView(
        String status,
        boolean editable,
        OffsetDateTime closeAt,
        OffsetDateTime submittedAt,
        StudentSummary student,
        String aspiration,
        String supportingActivities,
        List<AttachmentResponse> attachments,
        Long major1Id,
        Long major2Id) {

    public record StudentSummary(String fullName, String studentNumber, String classLabel) {
    }
}
