package com.re.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "borrowing_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BorrowingRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private MentoringSession session;

    private LocalDateTime borrowDate = LocalDateTime.now();
    private LocalDateTime expectedReturnDate;
    private LocalDateTime actualReturnDate;

    private String status = "PENDING";

    @OneToMany(mappedBy = "borrowingRecord", cascade = CascadeType.ALL)
    private List<BorrowingDetail> borrowingDetails;
}