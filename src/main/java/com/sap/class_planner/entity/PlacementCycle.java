package com.sap.class_planner.entity;

import com.sap.class_planner.enums.CycleStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/** Siklus penjurusan: satu per angkatan per tahun ajaran. */
@Entity
@Table(name = "placement_cycle",
        indexes = @Index(name = "ix_cycle_school_cohort", columnList = "school_id, cohort_entry_year"))
public class PlacementCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(nullable = false, length = 150)
    private String name; // contoh: 'Penjurusan Kelas 11 · 2027/2028'

    @Column(name = "target_academic_year", nullable = false, length = 9)
    private String targetAcademicYear; // contoh: '2027/2028'

    @Column(name = "cohort_entry_year", nullable = false)
    private Short cohortEntryYear; // angkatan peserta

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CycleStatus status;

    @Column(name = "survey_close_at")
    private OffsetDateTime surveyCloseAt;

    @Column(name = "selection_close_at")
    private OffsetDateTime selectionCloseAt;

    // parameter perhitungan kelas
    @Column(name = "class_count")
    private Short classCount;

    @Column(name = "class_capacity", nullable = false)
    private Short classCapacity = 36;

    @Column(name = "max_electives_per_class", nullable = false)
    private Short maxElectivesPerClass = 5;

    @Column(name = "subject_split_threshold", nullable = false)
    private Short subjectSplitThreshold = 18;

    @Column(name = "max_subject_class_pct", nullable = false)
    private Short maxSubjectClassPct = 75;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected PlacementCycle() {
        // untuk JPA
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public String getName() {
        return name;
    }

    public String getTargetAcademicYear() {
        return targetAcademicYear;
    }

    public Short getCohortEntryYear() {
        return cohortEntryYear;
    }

    public CycleStatus getStatus() {
        return status;
    }

    public OffsetDateTime getSurveyCloseAt() {
        return surveyCloseAt;
    }

    public OffsetDateTime getSelectionCloseAt() {
        return selectionCloseAt;
    }
}
