package com.sap.class_planner.repository;

import com.sap.class_planner.enums.CycleStatus;
import com.sap.class_planner.entity.PlacementCycle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlacementCycleRepository extends JpaRepository<PlacementCycle, Long> {

    /** Siklus aktif untuk angkatan murid: siklus terbaru yang sudah dibuka (bukan DRAFT) dan tidak dihapus. */
    Optional<PlacementCycle> findFirstBySchoolIdAndCohortEntryYearAndStatusNotAndDeletedAtIsNullOrderByIdDesc(
            Long schoolId, Short cohortEntryYear, CycleStatus excluded);

    default Optional<PlacementCycle> findActiveForCohort(Long schoolId, Short cohortEntryYear) {
        return findFirstBySchoolIdAndCohortEntryYearAndStatusNotAndDeletedAtIsNullOrderByIdDesc(
                schoolId, cohortEntryYear, CycleStatus.DRAFT);
    }
}
