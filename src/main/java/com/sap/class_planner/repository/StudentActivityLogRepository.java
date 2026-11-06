package com.sap.class_planner.repository;

import com.sap.class_planner.entity.StudentActivityLog;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentActivityLogRepository extends JpaRepository<StudentActivityLog, Long> {

    List<StudentActivityLog> findTop30ByCycleIdAndStudentIdOrderByIdDesc(Long cycleId, Long studentId);
}
