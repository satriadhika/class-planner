package com.sap.class_planner.repository;

import com.sap.class_planner.entity.QuestionnaireAttachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionnaireAttachmentRepository extends JpaRepository<QuestionnaireAttachment, Long> {

    List<QuestionnaireAttachment> findByResponseIdOrderByIdAsc(Long responseId);

    long countByResponseId(Long responseId);

    Optional<QuestionnaireAttachment> findByIdAndResponseId(Long id, Long responseId);
}
