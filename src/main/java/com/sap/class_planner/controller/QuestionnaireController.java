package com.sap.class_planner.controller;

import com.sap.class_planner.dto.request.QuestionnaireAnswersRequest;
import com.sap.class_planner.dto.response.AttachmentResponse;
import com.sap.class_planner.dto.response.QuestionnaireView;
import com.sap.class_planner.service.QuestionnaireService;
import com.sap.class_planner.service.QuestionnaireService.StoredFile;
import com.sap.class_planner.service.StudentContextService;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/** Kuesioner murid: /api/v1/me/questionnaire/... */
@RestController
@RequestMapping("/me/questionnaire")
@PreAuthorize("hasRole('MURID')")
public class QuestionnaireController {

    private final QuestionnaireService questionnaire;
    private final StudentContextService context;

    public QuestionnaireController(QuestionnaireService questionnaire, StudentContextService context) {
        this.questionnaire = questionnaire;
        this.context = context;
    }

    @GetMapping
    public QuestionnaireView get(@AuthenticationPrincipal Jwt jwt) {
        return questionnaire.get(context.require(jwt));
    }

    /** Simpan draf (boleh belum lengkap). */
    @PutMapping
    public QuestionnaireView saveDraft(@AuthenticationPrincipal Jwt jwt,
                                       @Valid @RequestBody QuestionnaireAnswersRequest request) {
        return questionnaire.saveDraft(context.require(jwt), request);
    }

    /** Kirim final. Setelah ini jawaban terkunci dan tahap KUESIONER = DONE. */
    @PostMapping("/submit")
    public QuestionnaireView submit(@AuthenticationPrincipal Jwt jwt,
                                    @Valid @RequestBody QuestionnaireAnswersRequest request) {
        return questionnaire.submit(context.require(jwt), request);
    }

    @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse upload(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) {
        return questionnaire.addAttachment(context.require(jwt), file);
    }

    @GetMapping("/attachments/{id}")
    public ResponseEntity<Resource> download(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        StoredFile file = questionnaire.downloadAttachment(context.require(jwt), id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }

    @DeleteMapping("/attachments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        questionnaire.deleteAttachment(context.require(jwt), id);
    }
}
