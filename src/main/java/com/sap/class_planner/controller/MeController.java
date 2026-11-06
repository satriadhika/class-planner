package com.sap.class_planner.controller;

import com.sap.class_planner.dto.response.ActivityResponse;
import com.sap.class_planner.dto.response.ProgressResponse;
import com.sap.class_planner.service.ActivityLogService;
import com.sap.class_planner.service.ProgressService;
import com.sap.class_planner.service.StudentContextService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Data milik murid yang sedang login: /api/v1/me/... */
@RestController
@RequestMapping("/me")
@PreAuthorize("hasRole('MURID')")
public class MeController {

    private final ProgressService progress;
    private final StudentContextService context;
    private final ActivityLogService activity;

    public MeController(ProgressService progress, StudentContextService context, ActivityLogService activity) {
        this.progress = progress;
        this.context = context;
        this.activity = activity;
    }

    @GetMapping("/progress")
    public ProgressResponse progress(@AuthenticationPrincipal Jwt jwt) {
        return progress.myProgress(jwt);
    }

    @GetMapping("/activity")
    public List<ActivityResponse> activity(@AuthenticationPrincipal Jwt jwt) {
        return activity.recent(context.require(jwt));
    }
}
