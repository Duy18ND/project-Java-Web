package com.re.project.repository;

import com.re.project.model.AcademicEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicEvaluationRepository extends JpaRepository<AcademicEvaluation, Long> {
    AcademicEvaluation findByMentoringSessionId(Long sessionId);
}