package com.sap.class_planner.controller;

import com.sap.class_planner.dto.response.MajorResponse;
import com.sap.class_planner.security.CurrentUser;
import com.sap.class_planner.service.MajorService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Daftar jurusan kuliah sekolah user yang login: /api/v1/majors */
@RestController
@RequestMapping("/majors")
public class MajorController {

    private final MajorService majors;

    public MajorController(MajorService majors) {
        this.majors = majors;
    }

    @GetMapping
    public List<MajorResponse> list(@AuthenticationPrincipal Jwt jwt) {
        return majors.list(CurrentUser.from(jwt).schoolId());
    }
}
