package com.sap.class_planner.service;

import com.sap.class_planner.entity.PlacementCycle;
import com.sap.class_planner.entity.Student;
import com.sap.class_planner.exception.BusinessException;
import com.sap.class_planner.repository.PlacementCycleRepository;
import com.sap.class_planner.repository.StudentRepository;
import com.sap.class_planner.security.CurrentUser;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Mengambil data murid & siklus aktif dari token. Data murid tidak pernah diambil dari request body. */
@Service
public class StudentContextService {

    private final StudentRepository students;
    private final PlacementCycleRepository cycles;

    public StudentContextService(StudentRepository students, PlacementCycleRepository cycles) {
        this.students = students;
        this.cycles = cycles;
    }

    @Transactional(readOnly = true)
    public Student requireStudent(Jwt jwt) {
        CurrentUser me = CurrentUser.from(jwt);
        if (me.studentId() == null) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "NOT_A_STUDENT", "Akun ini bukan akun murid.");
        }
        return students.findById(me.studentId())
                .filter(s -> s.getSchoolId().equals(me.schoolId()))
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "STUDENT_NOT_FOUND",
                        "Data murid tidak ditemukan."));
    }

    @Transactional(readOnly = true)
    public Optional<PlacementCycle> findActiveCycle(Student student) {
        return cycles.findActiveForCohort(student.getSchoolId(), student.getEntryYear());
    }

    /** Untuk endpoint yang wajib punya siklus aktif. 404 NO_ACTIVE_CYCLE kalau belum dibuka. */
    @Transactional(readOnly = true)
    public StudentContext require(Jwt jwt) {
        Student student = requireStudent(jwt);
        PlacementCycle cycle = findActiveCycle(student)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "NO_ACTIVE_CYCLE",
                        "Belum ada periode penjurusan yang dibuka untuk angkatanmu."));
        return new StudentContext(CurrentUser.from(jwt), student, cycle);
    }
}
