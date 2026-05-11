package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class DepartmentDto {

    private Long id;

    @NotBlank(message = "Tên khoa không được để trống")
    @Size(min = 2, max = 100, message = "Tên khoa phải từ 2 đến 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    private List<Long> lecturerIds;
}