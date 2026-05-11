package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AcademicEvaluationDto {

    private Long id;

    @NotNull(message = "Buổi học không được để trống")
    private Long sessionId;

    @NotNull(message = "Điểm số không được để trống")
    @DecimalMin(value = "0.0", message = "Điểm số không được nhỏ hơn 0")
    @DecimalMax(value = "10.0", message = "Điểm số không được lớn hơn 10")
    private Double score;

    @Size(max = 1000, message = "Nhận xét không được vượt quá 1000 ký tự")
    private String feedback;

    private LocalDateTime evaluationDate;
}