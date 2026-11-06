package com.sap.class_planner.repository;

import com.sap.class_planner.entity.Major;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface MajorRepository extends JpaRepository<Major, Long> {

    /** Semua jurusan sekolah + mapel wajibnya dalam satu query (tanpa N+1). */
    @Query("""
            select distinct m from Major m
            left join fetch m.requiredSubjects
            where m.schoolId = :schoolId and m.deletedAt is null
            order by m.name
            """)
    List<Major> findAllWithSubjects(@Param("schoolId") Long schoolId);

    @Query("""
            select count(m) from Major m
            where m.id in :ids and m.schoolId = :schoolId and m.deletedAt is null
            """)
    long countActiveInSchool(@Param("schoolId") Long schoolId, @Param("ids") Collection<Long> ids);
}
