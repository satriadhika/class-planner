package com.sap.class_planner.service;

import com.sap.class_planner.dto.request.QuestionnaireAnswersRequest;
import com.sap.class_planner.dto.response.AttachmentResponse;
import com.sap.class_planner.dto.response.QuestionnaireView;
import com.sap.class_planner.dto.response.QuestionnaireView.StudentSummary;
import com.sap.class_planner.enums.CycleStatus;
import com.sap.class_planner.entity.QuestionnaireAttachment;
import com.sap.class_planner.entity.QuestionnaireResponse;
import com.sap.class_planner.enums.StageKey;
import com.sap.class_planner.exception.BusinessException;
import com.sap.class_planner.repository.MajorRepository;
import com.sap.class_planner.repository.QuestionnaireAttachmentRepository;
import com.sap.class_planner.repository.QuestionnaireResponseRepository;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class QuestionnaireService {

    public static final int MAX_ATTACHMENTS = 3;
    public static final long MAX_ATTACHMENT_BYTES = 5L * 1024 * 1024;

    private final QuestionnaireResponseRepository responses;
    private final QuestionnaireAttachmentRepository attachments;
    private final MajorRepository majors;
    private final StageService stages;
    private final ActivityLogService activity;
    private final FileStorageService storage;

    public QuestionnaireService(QuestionnaireResponseRepository responses,
                                QuestionnaireAttachmentRepository attachments,
                                MajorRepository majors,
                                StageService stages,
                                ActivityLogService activity,
                                FileStorageService storage) {
        this.responses = responses;
        this.attachments = attachments;
        this.majors = majors;
        this.stages = stages;
        this.activity = activity;
        this.storage = storage;
    }

    // ---------------------------------------------------------------- baca

    @Transactional
    public QuestionnaireView get(StudentContext ctx) {
        QuestionnaireResponse response = responses.findByCycleIdAndStudentId(ctx.cycleId(), ctx.studentId())
                .orElse(null);
        if (response == null && isSurveyOpen(ctx)) {
            // tahap kuesioner dianggap mulai saat murid pertama kali membuka halamannya
            stages.markInProgress(ctx.cycleId(), ctx.studentId(), StageKey.KUESIONER);
            activity.log(ctx, ActivityLogService.QUESTIONNAIRE_OPENED, null);
        }
        return toView(ctx, response);
    }

    // ---------------------------------------------------------------- simpan draf & submit

    @Transactional
    public QuestionnaireView saveDraft(StudentContext ctx, QuestionnaireAnswersRequest req) {
        QuestionnaireResponse response = editableResponse(ctx);
        validateMajors(ctx, req, false);
        response.updateAnswers(trim(req.aspiration()), trim(req.supportingActivities()),
                req.major1Id(), req.major2Id());
        stages.markInProgress(ctx.cycleId(), ctx.studentId(), StageKey.KUESIONER);
        activity.log(ctx, ActivityLogService.QUESTIONNAIRE_DRAFT_SAVED, null);
        return toView(ctx, response);
    }

    @Transactional
    public QuestionnaireView submit(StudentContext ctx, QuestionnaireAnswersRequest req) {
        QuestionnaireResponse response = editableResponse(ctx);
        String aspiration = trim(req.aspiration());
        if (aspiration == null) {
            throw new BusinessException("QUESTIONNAIRE_INCOMPLETE", "Cita-cita wajib diisi.");
        }
        validateMajors(ctx, req, true);
        response.updateAnswers(aspiration, trim(req.supportingActivities()), req.major1Id(), req.major2Id());
        response.submit();
        stages.markDone(ctx.cycleId(), ctx.studentId(), StageKey.KUESIONER);
        activity.log(ctx, ActivityLogService.QUESTIONNAIRE_SUBMITTED,
                "major1=" + req.major1Id() + ", major2=" + req.major2Id());
        return toView(ctx, response);
    }

    // ---------------------------------------------------------------- dokumen penunjang

    @Transactional
    public AttachmentResponse addAttachment(StudentContext ctx, MultipartFile file) {
        QuestionnaireResponse response = editableResponse(ctx);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "File kosong.");
        }
        if (file.getSize() > MAX_ATTACHMENT_BYTES) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE, "FILE_TOO_LARGE", "Ukuran file maksimal 5 MB.");
        }
        if (attachments.countByResponseId(response.getId()) >= MAX_ATTACHMENTS) {
            throw new BusinessException("TOO_MANY_FILES", "Maksimal " + MAX_ATTACHMENTS + " dokumen.");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        FileKind kind = FileKind.detect(bytes); // cek isi file, bukan cuma ekstensi
        if (kind == null) {
            throw new BusinessException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "FILE_TYPE_NOT_ALLOWED",
                    "Format file harus PDF, JPG, atau PNG.");
        }

        String key = storage.store("questionnaire/" + response.getId(), kind.extension, bytes);
        afterRollback(() -> storage.deleteQuietly(key));

        QuestionnaireAttachment saved = attachments.save(new QuestionnaireAttachment(
                response.getId(), key, safeName(file.getOriginalFilename(), kind), kind.contentType, (long) bytes.length));
        stages.markInProgress(ctx.cycleId(), ctx.studentId(), StageKey.KUESIONER);
        activity.log(ctx, ActivityLogService.ATTACHMENT_UPLOADED, saved.getOriginalName());
        return toResponse(saved);
    }

    @Transactional
    public void deleteAttachment(StudentContext ctx, Long attachmentId) {
        QuestionnaireResponse response = editableResponse(ctx);
        QuestionnaireAttachment attachment = attachments.findByIdAndResponseId(attachmentId, response.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ATTACHMENT_NOT_FOUND",
                        "Dokumen tidak ditemukan."));
        attachments.delete(attachment);
        activity.log(ctx, ActivityLogService.ATTACHMENT_DELETED, attachment.getOriginalName());
        String key = attachment.getStorageKey();
        afterCommit(() -> storage.deleteQuietly(key));
    }

    @Transactional(readOnly = true)
    public StoredFile downloadAttachment(StudentContext ctx, Long attachmentId) {
        QuestionnaireResponse response = responses.findByCycleIdAndStudentId(ctx.cycleId(), ctx.studentId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ATTACHMENT_NOT_FOUND",
                        "Dokumen tidak ditemukan."));
        QuestionnaireAttachment a = attachments.findByIdAndResponseId(attachmentId, response.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ATTACHMENT_NOT_FOUND",
                        "Dokumen tidak ditemukan."));
        return new StoredFile(storage.load(a.getStorageKey()), a.getOriginalName(), a.getContentType());
    }

    public record StoredFile(Resource resource, String fileName, String contentType) {
    }

    // ---------------------------------------------------------------- helper

    private boolean isSurveyOpen(StudentContext ctx) {
        if (ctx.cycle().getStatus() != CycleStatus.SURVEY_OPEN) {
            return false;
        }
        OffsetDateTime closeAt = ctx.cycle().getSurveyCloseAt();
        return closeAt == null || OffsetDateTime.now().isBefore(closeAt);
    }

    /** Ambil (atau buat) jawaban yang masih boleh diubah. */
    private QuestionnaireResponse editableResponse(StudentContext ctx) {
        if (!isSurveyOpen(ctx)) {
            throw new BusinessException("SURVEY_CLOSED", "Periode pengisian kuesioner sedang tidak dibuka.");
        }
        QuestionnaireResponse response = responses.findByCycleIdAndStudentId(ctx.cycleId(), ctx.studentId())
                .orElseGet(() -> responses.save(QuestionnaireResponse.start(ctx.cycleId(), ctx.studentId())));
        if (response.isSubmitted()) {
            throw new BusinessException("ALREADY_SUBMITTED", "Kuesioner sudah dikirim dan tidak bisa diubah.");
        }
        return response;
    }

    private void validateMajors(StudentContext ctx, QuestionnaireAnswersRequest req, boolean required) {
        if (required && (req.major1Id() == null || req.major2Id() == null)) {
            throw new BusinessException("QUESTIONNAIRE_INCOMPLETE", "Pilihan jurusan 1 dan 2 wajib diisi.");
        }
        if (req.major1Id() != null && req.major1Id().equals(req.major2Id())) {
            throw new BusinessException("SAME_MAJOR", "Pilihan jurusan 1 dan 2 harus berbeda.");
        }
        Set<Long> ids = new HashSet<>();
        if (req.major1Id() != null) ids.add(req.major1Id());
        if (req.major2Id() != null) ids.add(req.major2Id());
        if (!ids.isEmpty() && majors.countActiveInSchool(ctx.student().getSchoolId(), ids) != ids.size()) {
            throw new BusinessException("MAJOR_NOT_FOUND", "Jurusan yang dipilih tidak tersedia.");
        }
    }

    private QuestionnaireView toView(StudentContext ctx, QuestionnaireResponse r) {
        StudentSummary student = new StudentSummary(ctx.student().getFullName(),
                ctx.student().getStudentNumber(), ctx.student().getCurrentClassLabel());
        OffsetDateTime closeAt = ctx.cycle().getSurveyCloseAt();
        if (r == null) {
            return new QuestionnaireView("NOT_STARTED", isSurveyOpen(ctx), closeAt, null, student,
                    "", "", List.of(), null, null);
        }
        List<AttachmentResponse> files = r.getId() == null ? List.of()
                : attachments.findByResponseIdOrderByIdAsc(r.getId()).stream().map(this::toResponse).toList();
        return new QuestionnaireView(r.getStatus().name(), isSurveyOpen(ctx) && !r.isSubmitted(), closeAt,
                r.getSubmittedAt(), student, nullToEmpty(r.getAspiration()), nullToEmpty(r.getSupportingActivities()),
                files, r.getMajor1Id(), r.getMajor2Id());
    }

    private AttachmentResponse toResponse(QuestionnaireAttachment a) {
        return new AttachmentResponse(a.getId(), a.getOriginalName(), a.getContentType(), a.getSizeBytes());
    }

    private static String trim(String value) {
        if (value == null) return null;
        String t = value.strip();
        return t.isEmpty() ? null : t;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String safeName(String original, FileKind kind) {
        String name = original == null ? "" : original.replaceAll("[\\\\/\\r\\n\\t]", "_").strip();
        if (name.isEmpty()) name = "dokumen." + kind.extension;
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }

    private static void afterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    /** File sudah ditulis ke disk sebelum commit; kalau transaksi gagal, hapus lagi. */
    private static void afterRollback(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    action.run();
                }
            }
        });
    }

    /** Jenis file yang diizinkan, dikenali dari magic bytes. */
    enum FileKind {
        PDF("pdf", "application/pdf"),
        JPG("jpg", "image/jpeg"),
        PNG("png", "image/png");

        final String extension;
        final String contentType;

        FileKind(String extension, String contentType) {
            this.extension = extension;
            this.contentType = contentType;
        }

        static FileKind detect(byte[] b) {
            if (b.length >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') return PDF;
            if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) return JPG;
            if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                    && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) return PNG;
            return null;
        }
    }
}
