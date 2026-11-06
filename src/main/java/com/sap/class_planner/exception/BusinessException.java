package com.sap.class_planner.exception;

import org.springframework.http.HttpStatus;

/**
 * Pelanggaran aturan bisnis. Default HTTP 422, dengan field "code" untuk dibaca frontend.
 * Contoh: throw new BusinessException("STUDENT_LOCKED", "Murid hanya cocok di kelas XI-2.");
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(String code, String message) {
        this(HttpStatus.UNPROCESSABLE_ENTITY, code, message);
    }

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
