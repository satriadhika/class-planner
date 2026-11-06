package com.sap.class_planner.dto.response;

public record AttachmentResponse(Long id, String fileName, String contentType, long size) {
}
