package com.re.project.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserProfileDto {

    private Long id;

    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;

    @Pattern(regexp = "^(\\+84|0)[3|5|7|8|9][0-9]{8}$",
            message = "Số điện thoại không đúng định dạng Việt Nam")
    private String phoneNumber;

    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    private String studentCode;
    private String academicRank;
    private Long departmentId;

    // --- BỔ SUNG TRƯỜNG NGÀY SINH ---
    @Past(message = "Ngày sinh phải là một ngày trong quá khứ")
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Ép kiểu chuẩn với <input type="date">
    private LocalDate birthDate;
}