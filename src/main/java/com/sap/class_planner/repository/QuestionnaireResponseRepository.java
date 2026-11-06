package com.sap.class_planner.repository;

import com.sap.class_planner.entity.QuestionnaireResponse;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionnaireResponseRepository extends JpaRepository<QuestionnaireResponse, Long> {

    Optional<QuestionnaireResponse> findByCycleIdAndStudentId(Long cycleId, Long studentId);
}
