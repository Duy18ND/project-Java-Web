package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BorrowingRecordDto {

    private Long id;

    @NotNull(message = "Buổi học không được để trống")
    private Long sessionId;

    private LocalDateTime borrowDate;

    @NotNull(message = "Ngày trả dự kiến không được để trống")
    @Future(message = "Ngày trả dự kiến phải ở tương lai")
    private LocalDateTime expectedReturnDate;

    private LocalDateTime actualReturnDate;

    @Pattern(regexp = "^(PENDING|BORROWING|RETURNED|OVERDUE|REJECTED)$",
            message = "Trạng thái không hợp lệ")
    private String status;

    @NotEmpty(message = "Danh sách thiết bị mượn không được trống")
    private List<BorrowingDetailDto> details;
}