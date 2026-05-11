package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentDto {

    private Long id;

    @NotBlank(message = "Tên thiết bị không được để trống")
    @Size(min = 2, max = 100, message = "Tên thiết bị phải từ 2 đến 100 ký tự")
    private String name;

    @NotBlank(message = "Vui lòng chọn danh mục (Thiết bị/Tài liệu)")
    private String category;

    @Min(value = 0, message = "Tổng số lượng không được nhỏ hơn 0")
    private int totalQuantity;

    @Min(value = 0, message = "Số lượng khả dụng không được nhỏ hơn 0")
    private int availableQuantity;

    @NotNull(message = "Thiết bị phải thuộc về một phòng Lab cụ thể")
    private Long labId;

    private String labName;
}