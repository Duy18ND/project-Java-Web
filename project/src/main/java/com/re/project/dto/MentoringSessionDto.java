package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MentoringSessionDto {

    private Long id;

    private Long studentId;
    private String lecturerName;
    @NotNull(message = "Giảng viên không được để trống")
    private Long lecturerId;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Future(message = "Thời gian bắt đầu phải ở tương lai")
    private LocalDateTime startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @Future(message = "Thời gian kết thúc phải ở tương lai")
    private LocalDateTime endTime;

    @NotBlank(message = "Chủ đề không được để trống")
    @Size(min = 5, max = 255, message = "Chủ đề phải từ 5 đến 255 ký tự")
    private String topic;

    @Pattern(regexp = "^(PENDING|CONFIRMED|COMPLETED|CANCELLED)$",
            message = "Trạng thái phải là PENDING, CONFIRMED, COMPLETED hoặc CANCELLED")
    private String status;

    // Tham chiếu kết quả liên quan
    private Long evaluationId;
    private Long borrowingRecordId;
    private String borrowedEquipments;
    private Double score;
    private String studentName;
    private String feedback;
    private String borrowingStatus;
}