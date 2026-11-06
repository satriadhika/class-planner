package com.sap.class_planner.dto.response;

import java.time.OffsetDateTime;

public record ActivityResponse(Long id, String action, String detail, OffsetDateTime createdAt) {
}
