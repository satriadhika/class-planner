package com.sap.class_planner.controller;

import com.sap.class_planner.dto.request.GoogleLoginRequest;
import com.sap.class_planner.dto.request.GoogleRegisterRequest;
import com.sap.class_planner.dto.request.LoginRequest;
import com.sap.class_planner.dto.response.CurrentUserResponse;
import com.sap.class_planner.dto.response.LoginResponse;
import com.sap.class_planner.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Endpoint: /api/v1/auth/... (context-path /api/v1 diatur di application.properties) */
@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.loginWithPassword(request);
    }

    @PostMapping("/google")
    public LoginResponse loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginWithGoogle(request.idToken());
    }

    /** Login Google pertama untuk murid: aktivasi dengan kode sekolah + NIS + tanggal lahir. */
    @PostMapping("/google/register")
    public LoginResponse registerWithGoogle(@Valid @RequestBody GoogleRegisterRequest request) {
        return authService.registerWithGoogle(request);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal Jwt token) {
        return authService.currentUser(token);
    }
}
