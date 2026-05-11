package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LabDto {

    private Long id;

    @NotBlank(message = "Tên phòng Lab không được để trống")
    private String name;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private int equipmentCount;
}