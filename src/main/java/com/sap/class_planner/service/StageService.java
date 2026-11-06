package com.sap.class_planner.service;

import com.sap.class_planner.dto.response.ProgressResponse.StageResponse;
import com.sap.class_planner.enums.CycleStatus;
import com.sap.class_planner.entity.PlacementCycle;
import com.sap.class_planner.enums.StageKey;
import com.sap.class_planner.entity.StudentStage;
import com.sap.class_planner.repository.StudentStageRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Menyimpan & menghitung status tahap murid.
 * Yang disimpan di DB: kapan tahap dimulai (IN_PROGRESS) dan selesai (DONE).
 * Status tampilan (CURRENT/LOCKED/MISSED) dihitung dari status siklus, jadi selalu konsisten
 * walaupun waka mengubah status siklus.
 */
@Service
public class StageService {

    private final StudentStageRepository stages;

    public StageService(StudentStageRepository stages) {
        this.stages = stages;
    }

    @Transactional
    public StudentStage markInProgress(Long cycleId, Long studentId, StageKey key) {
        return stages.findByCycleIdAndStudentIdAndStage(cycleId, studentId, key)
                .orElseGet(() -> stages.save(StudentStage.start(cycleId, studentId, key)));
    }

    @Transactional
    public void markDone(Long cycleId, Long studentId, StageKey key) {
        markInProgress(cycleId, studentId, key).complete();
    }

    @Transactional(readOnly = true)
    public List<StageResponse> progress(PlacementCycle cycle, Long studentId) {
        Map<StageKey, StudentStage> saved = new EnumMap<>(StageKey.class);
        stages.findByCycleIdAndStudentId(cycle.getId(), studentId).forEach(s -> saved.put(s.getStage(), s));

        List<StageResponse> result = new ArrayList<>();
        boolean currentAssigned = false;
        for (StageKey key : StageKey.values()) {
            StudentStage row = saved.get(key);
            String status;
            if (row != null && row.isDone()) {
                status = "DONE";
            } else if (isOpen(key, cycle.getStatus(), saved) && !currentAssigned) {
                status = "CURRENT";
                currentAssigned = true;
            } else if (isPast(key, cycle.getStatus())) {
                status = "MISSED";
            } else {
                status = "LOCKED";
            }
            result.add(new StageResponse(key.name(), status,
                    row == null ? null : row.getStartedAt(),
                    row == null ? null : row.getCompletedAt()));
        }
        return result;
    }

    /** Apakah murid boleh mengerjakan tahap ini sekarang. */
    public boolean isOpen(StageKey key, CycleStatus status, Map<StageKey, StudentStage> saved) {
        return switch (key) {
            case KUESIONER -> status == CycleStatus.SURVEY_OPEN;
            case PILIH_KELAS -> status == CycleStatus.SELECTION_OPEN;
            case SURAT -> status == CycleStatus.SELECTION_OPEN && isDone(saved, StageKey.PILIH_KELAS);
            case KELAS_FINAL -> status == CycleStatus.FINALIZED;
        };
    }

    /** Periode tahap ini sudah lewat. */
    private boolean isPast(StageKey key, CycleStatus status) {
        return switch (key) {
            case KUESIONER -> status.ordinal() > CycleStatus.SURVEY_OPEN.ordinal();
            case PILIH_KELAS, SURAT -> status.ordinal() > CycleStatus.SELECTION_OPEN.ordinal();
            case KELAS_FINAL -> false;
        };
    }

    private boolean isDone(Map<StageKey, StudentStage> saved, StageKey key) {
        StudentStage row = saved.get(key);
        return row != null && row.isDone();
    }
}
