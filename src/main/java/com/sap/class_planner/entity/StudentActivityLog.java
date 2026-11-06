package com.sap.class_planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

/** Jejak setiap aktivitas murid (simpan draf, kirim kuesioner, unggah dokumen, dll). Hanya ditambah, tidak diubah. */
@Entity
@Table(name = "student_activity_log",
        indexes = @Index(name = "ix_activity_student", columnList = "cycle_id, student_id, created_at"))
public class StudentActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cycle_id", nullable = false)
    private Long cycleId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String action; // contoh: QUESTIONNAIRE_SUBMITTED

    @Column(columnDefinition = "text")
    private String detail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected StudentActivityLog() {
        // untuk JPA
    }

    public StudentActivityLog(Long cycleId, Long studentId, Long userId, String action, String detail) {
        this.cycleId = cycleId;
        this.studentId = studentId;
        this.userId = userId;
        this.action = action;
        this.detail = detail;
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
