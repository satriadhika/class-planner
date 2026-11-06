package com.sap.class_planner.dto.response;

import java.util.List;

/** Jurusan kuliah + nama mapel pilihan yang dibutuhkan. */
public record MajorResponse(Long id, String code, String name, List<String> requiredSubjects) {
}
