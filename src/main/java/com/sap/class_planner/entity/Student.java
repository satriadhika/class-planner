package com.sap.class_planner.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Data murid dari e-Rapor. Akun login (userId) boleh kosong sampai murid mengaktifkan akunnya.
 */
@Entity
@Table(name = "student",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_student_number", columnNames = {"school_id", "student_number"}),
                @UniqueConstraint(name = "ux_student_user", columnNames = "user_id")
        },
        indexes = @Index(name = "ix_student_school_entry", columnList = "school_id, entry_year"))
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "student_number", nullable = false, length = 30)
    private String studentNumber; // NIS

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "entry_year", nullable = false)
    private Short entryYear; // angkatan

    @Column(name = "current_class_label", length = 30)
    private String currentClassLabel; // contoh: 'X-3'

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected Student() {
        // untuk JPA
    }

    public boolean isLinked() {
        return userId != null;
    }

    public boolean birthDateMatches(LocalDate date) {
        return birthDate != null && birthDate.equals(date);
    }

    public void linkUser(Long userId) {
        if (this.userId != null) {
            throw new IllegalStateException("Murid sudah terhubung dengan akun lain");
        }
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public Short getEntryYear() {
        return entryYear;
    }

    public String getCurrentClassLabel() {
        return currentClassLabel;
    }
}
