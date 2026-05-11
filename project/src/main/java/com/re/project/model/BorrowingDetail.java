package com.re.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "borrowing_details")
@AllArgsConstructor @NoArgsConstructor @Setter @Getter
public class BorrowingDetail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private BorrowingRecord borrowingRecord;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    private int quantity = 1;
}