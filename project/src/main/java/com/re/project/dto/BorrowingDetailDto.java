package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class BorrowingDetailDto {

    private Long id;

    @NotNull(message = "Phiếu mượn không được để trống")
    private Long borrowingRecordId;

    @NotNull(message = "Thiết bị không được để trống")
    private Long equipmentId;

    @Min(value = 1, message = "Số lượng mượn phải ít nhất là 1")
    @Max(value = 100, message = "Số lượng mượn không được vượt quá 100")
    private int quantity;
}