package com.re.project.controller;

import com.re.project.dto.UserRegisterRequest;
import com.re.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping({"/", "/login"})
    public String viewLogin(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String role = auth.getAuthorities().iterator().next().getAuthority();
            if (role.equals("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (role.equals("ROLE_LECTURER")) {
                return "redirect:/lecturer/dashboard";
            } else if (role.equals("ROLE_STUDENT")) {
                return "redirect:/student/dashboard";
            }
        }

        model.addAttribute("registerRequest", new UserRegisterRequest());
        return "login-register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registerRequest") UserRegisterRequest request,
            BindingResult result,
            Model model // THÊM MODEL ĐỂ ĐẨY THÔNG BÁO LỖI RA GIAO DIỆN
    ) {
        // 1. Có lỗi bỏ trống hoặc sai định dạng
        if (result.hasErrors()) {
            model.addAttribute("hasRegisterErrors", true);
            model.addAttribute("alertMessage", "Vui lòng kiểm tra lại các thông tin màu đỏ!");
            return "login-register";
        }

        // 2. Lỗi mật khẩu không khớp
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("hasRegisterErrors", true);
            model.addAttribute("alertMessage", "Mật khẩu xác nhận không khớp!");
            return "login-register";
        }

        // 3. Tiến hành lưu DB
        try {
            userService.registerNewUser(request);
            return "redirect:/login?success";

        } catch (RuntimeException e) {
            model.addAttribute("hasRegisterErrors", true); // Bật cờ giữ lại Tab Đăng ký
            String errorMsg = e.getMessage();

            if ("USER_EXISTS".equals(errorMsg)) {
                result.rejectValue("username", "error.username", "Tên đăng nhập đã tồn tại!");
                model.addAttribute("alertMessage", "Tên đăng nhập đã tồn tại!");
            } else if ("EMAIL_EXISTS".equals(errorMsg)) {
                result.rejectValue("email", "error.email", "Email đã tồn tại!");
                model.addAttribute("alertMessage", "Email đã tồn tại!");
            } else if ("PHONE_EXISTS".equals(errorMsg)) {
                result.rejectValue("phoneNumber", "error.phoneNumber", "Số điện thoại đã tồn tại!");
                model.addAttribute("alertMessage", "Số điện thoại đã tồn tại!");
            } else {
                e.printStackTrace();
                model.addAttribute("alertMessage", "Lỗi hệ thống: " + errorMsg);
            }

            return "login-register";
        }
    }
}