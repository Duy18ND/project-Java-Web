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

        // Chặn người dùng khi đã đăng nhập nhưng cố tình ấn quay lại trang
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {

            // Lấy Role của người dùng hiện tại
            String role = auth.getAuthorities().iterator().next().getAuthority();

            // Chuyển hướng (Redirect) về đúng đường dẫn theo Role
            if (role.equals("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            } else if (role.equals("ROLE_LECTURER")) {
                return "redirect:/lecturer/dashboard";
            } else if (role.equals("ROLE_STUDENT")) {
                return "redirect:/student/dashboard";
            }
        }

        // Nếu chưa đăng nhập thì mới cho hiển thị form Login/Register
        model.addAttribute("registerRequest", new UserRegisterRequest());
        return "login-register";
    }

    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registerRequest") UserRegisterRequest request,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return "login-register";
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp!");
            return "login-register";
        }

        try {
            userService.registerNewUser(request);
            return "redirect:/login?success";

        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();

            if ("USER_EXISTS".equals(errorMsg)) {
                result.rejectValue("username", "error.username", "Tên đăng nhập đã tồn tại!");
            } else if ("EMAIL_EXISTS".equals(errorMsg)) {
                result.rejectValue("email", "error.email", "Email đã tồn tại!");
            } else if ("PHONE_EXISTS".equals(errorMsg)) {
                result.rejectValue("phoneNumber", "error.phoneNumber", "Số điện thoại đã tồn tại!");
            } else {
                e.printStackTrace();
                System.out.println("====== LỖI LƯU DATABASE: " + errorMsg + " ======");
            }

            return "login-register";
        }
    }
}