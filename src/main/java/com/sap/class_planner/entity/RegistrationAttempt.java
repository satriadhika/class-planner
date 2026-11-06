package com.sap.class_planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/** Catatan setiap percobaan aktivasi akun murid (berhasil maupun gagal). */
@Entity
@Table(name = "registration_attempt")
public class RegistrationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_sub", nullable = false)
    private String googleSub;

    @Column
    private String email;

    @Column(name = "school_code", length = 30)
    private String schoolCode;

    @Column(name = "student_number", length = 30)
    private String studentNumber;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 40)
    private String reason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected RegistrationAttempt() {
        // untuk JPA
    }

    public RegistrationAttempt(String googleSub, String email, String schoolCode,
                               String studentNumber, boolean success, String reason) {
        this.googleSub = googleSub;
        this.email = email;
        this.schoolCode = schoolCode;
        this.studentNumber = studentNumber;
        this.success = success;
        this.reason = reason;
    }
}
