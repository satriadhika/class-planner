package com.sap.class_planner.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

/** Progres penjurusan murid. cycle = null kalau belum ada siklus yang dibuka untuk angkatannya. */
public record ProgressResponse(CycleSummary cycle, List<StageResponse> stages) {

    public record CycleSummary(Long id, String name, String status,
                               OffsetDateTime surveyCloseAt, OffsetDateTime selectionCloseAt) {
    }

    /** status: DONE | CURRENT | LOCKED | MISSED (periode tahap sudah lewat tapi belum dikerjakan). */
    public record StageResponse(String key, String status, OffsetDateTime startedAt, OffsetDateTime completedAt) {
    }
}
