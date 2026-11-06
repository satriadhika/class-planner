package com.sap.class_planner.dto.response;

/** Data user yang sedang login. Nama field sama persis dengan model di Angular. */
public record CurrentUserResponse(
        Long id,
        String username,
        String displayName,
        String role,
        SchoolRef school,   // null untuk SUPER_ADMIN
        Long teacherId,     // null kalau bukan guru
        Long studentId) {   // null kalau bukan murid
}
