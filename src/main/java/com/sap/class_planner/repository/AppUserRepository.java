package com.sap.class_planner.repository;

import com.sap.class_planner.entity.AppUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /** Login username & password: username unik per sekolah. */
    @Query("""
            select u from AppUser u, School s
            where s.id = u.schoolId
              and s.code = :schoolCode
              and s.deletedAt is null
              and u.username = :username
              and u.status = com.sap.class_planner.enums.UserStatus.ACTIVE
              and u.deletedAt is null
            """)
    Optional<AppUser> findActiveBySchoolCodeAndUsername(@Param("schoolCode") String schoolCode,
                                                        @Param("username") String username);

    /** Login Google: email unik di semua sekolah. */
    @Query("""
            select u from AppUser u
            where lower(u.email) = lower(:email)
              and u.status = com.sap.class_planner.enums.UserStatus.ACTIVE
              and u.deletedAt is null
            """)
    Optional<AppUser> findActiveByEmail(@Param("email") String email);

    /** Termasuk akun nonaktif: untuk membedakan "belum terdaftar" dengan "dinonaktifkan". */
    @Query("""
            select count(u) > 0 from AppUser u
            where lower(u.email) = lower(:email)
              and u.deletedAt is null
            """)
    boolean existsByEmailIncludingInactive(@Param("email") String email);

    @Query("""
            select count(u) > 0 from AppUser u
            where u.schoolId = :schoolId
              and u.username = :username
              and u.deletedAt is null
            """)
    boolean existsBySchoolIdAndUsername(@Param("schoolId") Long schoolId, @Param("username") String username);

    @Query("""
            select u from AppUser u
            where u.id = :id
              and u.status = com.sap.class_planner.enums.UserStatus.ACTIVE
              and u.deletedAt is null
            """)
    Optional<AppUser> findActiveById(@Param("id") Long id);

    @Query(value = "select id from teacher where user_id = :userId and deleted_at is null",
           nativeQuery = true)
    Optional<Long> findTeacherIdByUserId(@Param("userId") Long userId);

    @Query(value = "select id from student where user_id = :userId and deleted_at is null",
           nativeQuery = true)
    Optional<Long> findStudentIdByUserId(@Param("userId") Long userId);
}
