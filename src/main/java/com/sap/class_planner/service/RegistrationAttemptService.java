package com.sap.class_planner.service;

import com.sap.class_planner.entity.RegistrationAttempt;
import com.sap.class_planner.exception.BusinessException;
import com.sap.class_planner.repository.RegistrationAttemptRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Membatasi percobaan aktivasi supaya NIS + tanggal lahir tidak bisa ditebak terus-menerus.
 * Dipisah dari AuthService supaya pencatatan memakai transaksi sendiri (REQUIRES_NEW):
 * catatan percobaan gagal tetap tersimpan walaupun transaksi aktivasi di-rollback.
 */
@Service
public class RegistrationAttemptService {

    /** Maksimal percobaan gagal per akun Google dalam 1 jam. */
    static final int MAX_FAILED_PER_ACCOUNT = 5;
    /** Maksimal percobaan gagal untuk satu NIS (dari akun Google mana pun) dalam 1 jam. */
    static final int MAX_FAILED_PER_STUDENT = 10;

    private final RegistrationAttemptRepository attempts;

    public RegistrationAttemptService(RegistrationAttemptRepository attempts) {
        this.attempts = attempts;
    }

    @Transactional(readOnly = true)
    public void ensureNotBlocked(String googleSub, String schoolCode, String studentNumber) {
        OffsetDateTime since = OffsetDateTime.now().minusHours(1);

        if (attempts.countFailedByGoogleSubSince(googleSub, since) >= MAX_FAILED_PER_ACCOUNT
                || attempts.countFailedByStudentSince(schoolCode, studentNumber, since) >= MAX_FAILED_PER_STUDENT) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_ATTEMPTS",
                    "Terlalu banyak percobaan gagal. Coba lagi 1 jam lagi atau hubungi waka sekolahmu.");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String googleSub, String email, String schoolCode,
                              String studentNumber, String reason) {
        attempts.save(new RegistrationAttempt(googleSub, email, schoolCode, studentNumber, false, reason));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String googleSub, String email, String schoolCode, String studentNumber) {
        attempts.save(new RegistrationAttempt(googleSub, email, schoolCode, studentNumber, true, null));
    }
}
