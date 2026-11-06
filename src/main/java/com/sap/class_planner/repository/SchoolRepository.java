package com.sap.class_planner.repository;

import com.sap.class_planner.entity.School;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
