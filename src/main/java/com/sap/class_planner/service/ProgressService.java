package com.sap.class_planner.service;

import com.sap.class_planner.dto.response.ProgressResponse;
import com.sap.class_planner.dto.response.ProgressResponse.CycleSummary;
import com.sap.class_planner.dto.response.ProgressResponse.StageResponse;
import com.sap.class_planner.entity.PlacementCycle;
import com.sap.class_planner.enums.StageKey;
import com.sap.class_planner.entity.Student;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class ProgressService {

    private final StudentContextService context;
    private final StageService stages;

    public ProgressService(StudentContextService context, StageService stages) {
        this.context = context;
        this.stages = stages;
    }

    @Transactional(readOnly = true)
    public ProgressResponse myProgress(Jwt jwt) {
        Student student = context.requireStudent(jwt);
        PlacementCycle cycle = context.findActiveCycle(student).orElse(null);
        if (cycle == null) {
            List<StageResponse> locked = Arrays.stream(StageKey.values())
                    .map(k -> new StageResponse(k.name(), "LOCKED", null, null))
                    .toList();
            return new ProgressResponse(null, locked);
        }
        CycleSummary summary = new CycleSummary(cycle.getId(), cycle.getName(), cycle.getStatus().name(),
                cycle.getSurveyCloseAt(), cycle.getSelectionCloseAt());
        return new ProgressResponse(summary, stages.progress(cycle, student.getId()));
    }
}
