package com.sap.class_planner.entity;

import com.sap.class_planner.enums.Role;
import com.sap.class_planner.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Hanya kolom yang dibutuhkan untuk autentikasi yang dipetakan.
 * Kolom lain (created_at, updated_at, dll.) diisi default oleh database.
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private Long schoolId; // null untuk SUPER_ADMIN

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash; // null untuk user yang hanya login dengan Google

    @Column(length = 255)
    private String email;

    @Column(name = "google_sub")
    private String googleSub;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected AppUser() {
        // untuk JPA
    }

    /** Akun murid baru yang dibuat saat murid mengaktifkan akun dengan Google. */
    public static AppUser newStudentFromGoogle(Long schoolId, String username, String email,
                                               String displayName, String googleSub) {
        AppUser user = new AppUser();
        user.schoolId = schoolId;
        user.username = username;
        user.email = email.toLowerCase();
        user.displayName = displayName;
        user.googleSub = googleSub;
        user.role = Role.MURID;
        user.status = UserStatus.ACTIVE;
        return user;
    }

    /** Kaitkan akun Google saat login pertama. */
    public void linkGoogleAccount(String googleSub) {
        this.googleSub = googleSub;
    }

    public void markLoggedIn() {
        this.lastLoginAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}
