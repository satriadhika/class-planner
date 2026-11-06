package com.sap.class_planner.repository;

import com.sap.class_planner.enums.StageKey;
import com.sap.class_planner.entity.StudentStage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentStageRepository extends JpaRepository<StudentStage, Long> {

    List<StudentStage> findByCycleIdAndStudentId(Long cycleId, Long studentId);

    Optional<StudentStage> findByCycleIdAndStudentIdAndStage(Long cycleId, Long studentId, StageKey stage);
}
