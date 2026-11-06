package com.sap.class_planner.service;

import com.sap.class_planner.dto.response.ActivityResponse;
import com.sap.class_planner.entity.StudentActivityLog;
import com.sap.class_planner.repository.StudentActivityLogRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Jejak aktivitas murid. Ditulis di transaksi yang sama dengan aksinya,
 * jadi kalau aksinya gagal, log-nya juga tidak tersimpan.
 */
@Service
public class ActivityLogService {

    public static final String QUESTIONNAIRE_OPENED = "QUESTIONNAIRE_OPENED";
    public static final String QUESTIONNAIRE_DRAFT_SAVED = "QUESTIONNAIRE_DRAFT_SAVED";
    public static final String QUESTIONNAIRE_SUBMITTED = "QUESTIONNAIRE_SUBMITTED";
    public static final String ATTACHMENT_UPLOADED = "ATTACHMENT_UPLOADED";
    public static final String ATTACHMENT_DELETED = "ATTACHMENT_DELETED";

    private final StudentActivityLogRepository logs;

    public ActivityLogService(StudentActivityLogRepository logs) {
        this.logs = logs;
    }

    @Transactional
    public void log(StudentContext ctx, String action, String detail) {
        logs.save(new StudentActivityLog(ctx.cycleId(), ctx.studentId(), ctx.user().userId(), action, detail));
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> recent(StudentContext ctx) {
        return logs.findTop30ByCycleIdAndStudentIdOrderByIdDesc(ctx.cycleId(), ctx.studentId()).stream()
                .map(l -> new ActivityResponse(l.getId(), l.getAction(), l.getDetail(), l.getCreatedAt()))
                .toList();
    }
}
