package com.sap.class_planner.entity;

import com.sap.class_planner.enums.StageKey;
import com.sap.class_planner.enums.StageProgress;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/**
 * Status satu tahap penjurusan milik satu murid di satu siklus.
 * Baris dibuat saat murid mulai mengerjakan tahap itu, lalu ditandai selesai.
 */
@Entity
@Table(name = "student_stage",
        uniqueConstraints = @UniqueConstraint(name = "ux_student_stage", columnNames = {"cycle_id", "student_id", "stage"}))
public class StudentStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cycle_id", nullable = false)
    private Long cycleId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StageKey stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StageProgress status;

    @Column(name = "started_at", nullable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected StudentStage() {
        // untuk JPA
    }

    public static StudentStage start(Long cycleId, Long studentId, StageKey stage) {
        StudentStage s = new StudentStage();
        s.cycleId = cycleId;
        s.studentId = studentId;
        s.stage = stage;
        s.status = StageProgress.IN_PROGRESS;
        s.startedAt = OffsetDateTime.now();
        return s;
    }

    public void complete() {
        if (status != StageProgress.DONE) {
            status = StageProgress.DONE;
            completedAt = OffsetDateTime.now();
        }
    }

    public boolean isDone() {
        return status == StageProgress.DONE;
    }

    public Long getId() {
        return id;
    }

    public StageKey getStage() {
        return stage;
    }

    public StageProgress getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }
}
