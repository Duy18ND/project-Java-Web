package com.re.project.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "academic_evaluations")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AcademicEvaluation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    private MentoringSession mentoringSession;

    private Double score;
    private String feedback;
    private LocalDateTime evaluationDate = LocalDateTime.now();
}