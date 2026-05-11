package com.re.project.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mentoring_sessions")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MentoringSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecturer_id")
    private Lecturer lecturer;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String topic;

    @Column(columnDefinition = "varchar(50) default 'PENDING'")
    private String status;

    // 1. Đã xóa biến cũ, chỉ giữ lại đúng biến này cho phần Đánh giá
    @OneToOne(mappedBy = "mentoringSession", cascade = CascadeType.ALL)
    private AcademicEvaluation academicEvaluation;

    // 2. Phần mượn thiết bị giữ nguyên (Vì trong BorrowingRecord biến tên là "session")
    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private BorrowingRecord borrowingRecord;
}