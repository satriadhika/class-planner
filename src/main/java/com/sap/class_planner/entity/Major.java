package com.sap.class_planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/** Jurusan kuliah yang dipilih murid, beserta mapel pilihan yang dibutuhkan. */
@Entity
@Table(name = "major",
        uniqueConstraints = @UniqueConstraint(name = "ux_major_code", columnNames = {"school_id", "code"}))
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    /** Tabel penghubung major_subject (satu mapel bisa dibutuhkan banyak jurusan). */
    @ManyToMany
    @JoinTable(name = "major_subject",
            joinColumns = @JoinColumn(name = "major_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"))
    private Set<Subject> requiredSubjects = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at" )
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected Major() {
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

    public Set<Subject> getRequiredSubjects() {
        return requiredSubjects;
    }
}
