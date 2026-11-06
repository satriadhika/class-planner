package com.sap.class_planner.entity;

import com.sap.class_planner.enums.Rumpun;
import com.sap.class_planner.enums.SubjectCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/** Mata pelajaran. UMUM = mapel kelas 10 (nilai rapor), PILIHAN = mapel pilihan kelas 11–12. */
@Entity
@Table(name = "subject",
        uniqueConstraints = @UniqueConstraint(name = "ux_subject_code", columnNames = {"school_id", "code"}))
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubjectCategory category;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Rumpun rumpun; // null = netral

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected Subject() {
        // untuk JPA
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public SubjectCategory getCategory() {
        return category;
    }

    public Rumpun getRumpun() {
        return rumpun;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }
}
