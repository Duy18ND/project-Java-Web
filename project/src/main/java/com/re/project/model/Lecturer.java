package com.re.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lecturers")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Lecturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    private String degree;
    private String specialization;
}