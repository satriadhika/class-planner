package com.sap.class_planner.repository;

import com.sap.class_planner.entity.RegistrationAttempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface RegistrationAttemptRepository extends JpaRepository<RegistrationAttempt, Long> {

    @Query("""
            select count(a) from RegistrationAttempt a
            where a.googleSub = :googleSub
              and a.success = false
              and a.createdAt > :since
            """)
    long countFailedByGoogleSubSince(@Param("googleSub") String googleSub,
                                     @Param("since") OffsetDateTime since);

    @Query("""
            select count(a) from RegistrationAttempt a
            where upper(a.schoolCode) = upper(:schoolCode)
              and a.studentNumber = :studentNumber
              and a.success = false
              and a.createdAt > :since
            """)
    long countFailedByStudentSince(@Param("schoolCode") String schoolCode,
                                   @Param("studentNumber") String studentNumber,
                                   @Param("since") OffsetDateTime since);
}
