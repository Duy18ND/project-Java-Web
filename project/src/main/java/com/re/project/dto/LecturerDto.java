package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LecturerDto {

    private Long id;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotNull(message = "Khoa/Bộ môn không được để trống")
    private Long departmentId;

    @NotBlank(message = "Học vị không được để trống")
    @Size(max = 50, message = "Học vị không được vượt quá 50 ký tự")
    private String degree;

    @NotBlank(message = "Chuyên ngành không được để trống")
    @Size(max = 100, message = "Chuyên ngành không được vượt quá 100 ký tự")
    private String specialization;

    private String fullName;
}