package com.sap.class_planner.repository;

import com.sap.class_planner.entity.Student;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Cari murid untuk aktivasi akun. Dikunci (SELECT ... FOR UPDATE) supaya dua akun Google
     * yang mengaktifkan data murid yang sama di waktu bersamaan tidak sama-sama berhasil.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from Student s, School sc
            where sc.id = s.schoolId
              and upper(sc.code) = upper(:schoolCode)
              and sc.deletedAt is null
              and s.studentNumber = :studentNumber
              and s.deletedAt is null
            """)
    Optional<Student> findForActivation(@Param("schoolCode") String schoolCode,
                                        @Param("studentNumber") String studentNumber);
}
