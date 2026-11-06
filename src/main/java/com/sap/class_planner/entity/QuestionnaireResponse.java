package com.sap.class_planner.entity;

import com.sap.class_planner.enums.ResponseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

/** Jawaban kuesioner satu murid di satu siklus. */
@Entity
@Table(name = "questionnaire_response",
        uniqueConstraints = @UniqueConstraint(name = "ux_response_cycle_student", columnNames = {"cycle_id", "student_id"}))
public class QuestionnaireResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cycle_id", nullable = false)
    private Long cycleId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResponseStatus status = ResponseStatus.DRAFT;

    @Column(length = 100)
    private String aspiration; // cita-cita

    @Column(name = "supporting_activities", length = 1000)
    private String supportingActivities; // penunjang cita-cita

    @Column(name = "major_1_id")
    private Long major1Id;

    @Column(name = "major_2_id")
    private Long major2Id;

    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected QuestionnaireResponse() {
        // untuk JPA
    }

    public static QuestionnaireResponse start(Long cycleId, Long studentId) {
        QuestionnaireResponse response = new QuestionnaireResponse();
        response.cycleId = cycleId;
        response.studentId = studentId;
        return response;
    }

    public void updateAnswers(String aspiration, String supportingActivities, Long major1Id, Long major2Id) {
        this.aspiration = aspiration;
        this.supportingActivities = supportingActivities;
        this.major1Id = major1Id;
        this.major2Id = major2Id;
    }

    public void submit() {
        this.status = ResponseStatus.SUBMITTED;
        this.submittedAt = OffsetDateTime.now();
    }

    public boolean isSubmitted() {
        return status == ResponseStatus.SUBMITTED;
    }

    public Long getId() {
        return id;
    }

    public Long getCycleId() {
        return cycleId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getAspiration() {
        return aspiration;
    }

    public String getSupportingActivities() {
        return supportingActivities;
    }

    public Long getMajor1Id() {
        return major1Id;
    }

    public Long getMajor2Id() {
        return major2Id;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }
}
