package com.re.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserRegisterRequest {
    @NotBlank(message = "Họ và tên không được để trống!")
    private String fullName;

    @NotBlank(message = "Email không được để trống!")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Email không đúng định dạng (VD: example@gmail.com)!"
    )
    private String email;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 6, message = "Tên đăng nhập phải có ít nhất 6 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tên đăng nhập chỉ được chứa chữ cái không dấu và số")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống!")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự!")
    @Pattern(regexp = "^[\\x21-\\x7E]+$", message = "Mật khẩu không được chứa khoảng trắng và không có dấu tiếng Việt!")
    private String password;

    @NotBlank(message = "Vui lòng xác nhận lại mật khẩu!")
    private String confirmPassword;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Pattern(regexp = "^(0|84|\\+84)[0-9]{9}$", message = "Số điện thoại phải bắt đầu bằng 0, 84 hoặc +84 và gồm đúng 10 số hợp lệ!")
    private String phoneNumber;
}