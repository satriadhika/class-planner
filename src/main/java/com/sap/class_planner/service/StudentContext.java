package com.sap.class_planner.service;

import com.sap.class_planner.entity.PlacementCycle;
import com.sap.class_planner.entity.Student;
import com.sap.class_planner.security.CurrentUser;

/** Murid yang sedang login + siklus penjurusan aktif untuk angkatannya. */
public record StudentContext(CurrentUser user, Student student, PlacementCycle cycle) {

    public Long studentId() {
        return student.getId();
    }

    public Long cycleId() {
        return cycle.getId();
    }
}
