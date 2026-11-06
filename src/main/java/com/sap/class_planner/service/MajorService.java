package com.sap.class_planner.service;

import com.sap.class_planner.dto.response.MajorResponse;
import com.sap.class_planner.entity.Subject;
import com.sap.class_planner.repository.MajorRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class MajorService {

    private final MajorRepository majors;

    public MajorService(MajorRepository majors) {
        this.majors = majors;
    }

    @Transactional(readOnly = true)
    public List<MajorResponse> list(Long schoolId) {
        return majors.findAllWithSubjects(schoolId).stream()
                .map(m -> new MajorResponse(m.getId(), m.getCode(), m.getName(),
                        m.getRequiredSubjects().stream()
                                .filter(s -> s.getDeletedAt() == null)
                                .map(Subject::getName)
                                .sorted(Comparator.naturalOrder())
                                .toList()))
                .toList();
    }
}
