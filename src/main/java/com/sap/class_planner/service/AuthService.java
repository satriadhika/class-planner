package com.sap.class_planner.service;

import com.sap.class_planner.dto.request.GoogleRegisterRequest;
import com.sap.class_planner.dto.request.LoginRequest;
import com.sap.class_planner.dto.response.CurrentUserResponse;
import com.sap.class_planner.dto.response.LoginResponse;
import com.sap.class_planner.dto.response.SchoolRef;
import com.sap.class_planner.entity.AppUser;
import com.sap.class_planner.enums.Role;
import com.sap.class_planner.entity.Student;
import com.sap.class_planner.exception.BusinessException;
import com.sap.class_planner.repository.AppUserRepository;
import com.sap.class_planner.repository.SchoolRepository;
import com.sap.class_planner.repository.StudentRepository;
import com.sap.class_planner.security.GoogleTokenVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Pesan sama untuk semua alasan "data tidak cocok", supaya tidak membocorkan NIS mana yang ada. */
    private static final String STUDENT_NOT_MATCHED_MESSAGE =
            "Data tidak cocok. Periksa kembali kode sekolah, NIS, dan tanggal lahir.";

    private final AppUserRepository users;
    private final SchoolRepository schools;
    private final StudentRepository students;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final GoogleTokenVerifier googleVerifier;
    private final RegistrationAttemptService attempts;

    public AuthService(AppUserRepository users,
                       SchoolRepository schools,
                       StudentRepository students,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService,
                       GoogleTokenVerifier googleVerifier,
                       RegistrationAttemptService attempts) {
        this.users = users;
        this.schools = schools;
        this.students = students;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.googleVerifier = googleVerifier;
        this.attempts = attempts;
    }

    // -----------------------------------------------------------------
    // Login username & password
    // -----------------------------------------------------------------
    @Transactional
    public LoginResponse loginWithPassword(LoginRequest request) {
        AppUser user = users
                .findActiveBySchoolCodeAndUsername(request.schoolCode().trim(), request.username().trim())
                .filter(u -> u.getPasswordHash() != null)
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                // Pesan sama untuk "user tidak ada" dan "password salah"
                .orElseThrow(() -> new BadCredentialsException("Username atau password salah"));

        return completeLogin(user);
    }

    // -----------------------------------------------------------------
    // Login Google
    // -----------------------------------------------------------------

    /**
     * Email sudah terhubung → login.
     * Belum terhubung → 422 ACCOUNT_NOT_LINKED, frontend menampilkan form aktivasi murid.
     */
    @Transactional
    public LoginResponse loginWithGoogle(String idToken) {
        Jwt google = verifyGoogleToken(idToken);
        String email = google.getClaimAsString("email");

        AppUser user = users.findActiveByEmail(email).orElse(null);
        if (user == null) {
            throw notRegistered(email);
        }

        ensureSameGoogleAccount(user, google);
        return completeLogin(user);
    }

    /**
     * Aktivasi akun murid pada login Google pertama:
     * kode sekolah + NIS + tanggal lahir harus cocok dengan data murid dari e-Rapor,
     * dan data murid itu belum terhubung dengan akun lain.
     */
    @Transactional
    public LoginResponse registerWithGoogle(GoogleRegisterRequest request) {
        Jwt google = verifyGoogleToken(request.idToken());
        String googleSub = google.getSubject();
        String email = google.getClaimAsString("email");
        String schoolCode = request.schoolCode().trim();
        String studentNumber = request.studentNumber().trim();

        // Sudah pernah aktivasi (misal klik dua kali) → langsung login saja
        AppUser existing = users.findActiveByEmail(email).orElse(null);
        if (existing != null) {
            ensureSameGoogleAccount(existing, google);
            return completeLogin(existing);
        }
        if (users.existsByEmailIncludingInactive(email)) {
            throw new DisabledException("Akun kamu dinonaktifkan. Hubungi waka sekolahmu.");
        }

        attempts.ensureNotBlocked(googleSub, schoolCode, studentNumber);

        Student student = students.findForActivation(schoolCode, studentNumber).orElse(null);
        if (student == null || !student.birthDateMatches(request.birthDate())) {
            attempts.recordFailure(googleSub, email, schoolCode, studentNumber, "NOT_MATCHED");
            throw new BusinessException("STUDENT_NOT_MATCHED", STUDENT_NOT_MATCHED_MESSAGE);
        }
        if (student.isLinked()) {
            attempts.recordFailure(googleSub, email, schoolCode, studentNumber, "ALREADY_CLAIMED");
            throw new BusinessException("STUDENT_ALREADY_CLAIMED",
                    "Data murid ini sudah terhubung dengan akun Google lain. Hubungi waka sekolahmu.");
        }

        // Username = NIS (unik per sekolah); kalau bentrok, pakai email
        String username = users.existsBySchoolIdAndUsername(student.getSchoolId(), studentNumber)
                ? email.toLowerCase()
                : studentNumber;

        AppUser user = users.saveAndFlush(AppUser.newStudentFromGoogle(
                student.getSchoolId(), username, email, student.getFullName(), googleSub));
        student.linkUser(user.getId());
        students.saveAndFlush(student);

        attempts.recordSuccess(googleSub, email, schoolCode, studentNumber);
        log.info("Akun murid diaktifkan: studentId={}, userId={}", student.getId(), user.getId());

        return completeLogin(user);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(Jwt token) {
        AppUser user = users.findActiveById(Long.valueOf(token.getSubject()))
                .orElseThrow(() -> new BadCredentialsException("Akun tidak aktif"));
        return toResponse(user);
    }

    // -----------------------------------------------------------------
    // Helper
    // -----------------------------------------------------------------
    private Jwt verifyGoogleToken(String idToken) {
        Jwt google;
        try {
            google = googleVerifier.verify(idToken);
        } catch (JwtException e) {
            log.warn("Token Google ditolak: {}", e.getMessage());
            throw new BadCredentialsException("Token Google tidak valid. Silakan login ulang.");
        }
        if (!Boolean.TRUE.equals(google.getClaimAsBoolean("email_verified"))) {
            throw new BadCredentialsException("Email Google belum terverifikasi");
        }
        return google;
    }

    private RuntimeException notRegistered(String email) {
        if (users.existsByEmailIncludingInactive(email)) {
            return new DisabledException("Akun kamu dinonaktifkan. Hubungi waka sekolahmu.");
        }
        return new BusinessException("ACCOUNT_NOT_LINKED",
                "Akun " + email + " belum terhubung. Lengkapi kode sekolah, NIS, dan tanggal lahir.");
    }

    /** Login pertama: kaitkan akun Google. Berikutnya: harus akun Google yang sama. */
    private void ensureSameGoogleAccount(AppUser user, Jwt google) {
        if (user.getGoogleSub() == null) {
            user.linkGoogleAccount(google.getSubject());
        } else if (!user.getGoogleSub().equals(google.getSubject())) {
            throw new BadCredentialsException("Akun Google tidak cocok dengan akun yang terdaftar");
        }
    }

    private LoginResponse completeLogin(AppUser user) {
        user.markLoggedIn();
        CurrentUserResponse me = toResponse(user);
        String token = tokenService.issue(user, me.teacherId(), me.studentId());
        return new LoginResponse(token, "Bearer", tokenService.expirationSeconds(), me);
    }

    private CurrentUserResponse toResponse(AppUser user) {
        SchoolRef school = user.getSchoolId() == null ? null
                : schools.findById(user.getSchoolId())
                        .map(s -> new SchoolRef(s.getId(), s.getCode(), s.getName()))
                        .orElse(null);

        Long teacherId = user.getRole() == Role.GURU
                ? users.findTeacherIdByUserId(user.getId()).orElse(null) : null;
        Long studentId = user.getRole() == Role.MURID
                ? users.findStudentIdByUserId(user.getId()).orElse(null) : null;

        return new CurrentUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole().name(),
                school,
                teacherId,
                studentId);
    }
}
