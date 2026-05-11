package com.re.project.controller;

import com.re.project.model.Department;
import com.re.project.model.User;
import com.re.project.model.UserProfile;
import com.re.project.repository.DepartmentRepository;
import com.re.project.repository.UserProfileRepository;
import com.re.project.repository.UserRepository;
import com.re.project.service.MentoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final MentoringService mentoringService;

    @GetMapping
    public String viewProfile(Model model, Principal principal) {
        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            if (currentUser != null) {
                model.addAttribute("userProfile", currentUser.getUserProfile());
                if (currentUser.getRole().name().equals("LECTURER")) {
                    model.addAttribute("lecturerInfo", currentUser.getLecturer());
                }
            }
        }
        model.addAttribute("departments", mentoringService.getAllDepartments());
        model.addAttribute("view", "shared/profile");
        return "home";
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String academicRank, // Nhận giá trị từ Dropdown
            Principal principal,
            RedirectAttributes ra) {

        if (principal == null) return "redirect:/login";
        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);

        if (currentUser != null && currentUser.getUserProfile() != null) {
            UserProfile profile = currentUser.getUserProfile();
            boolean isUpdated = false;

            // Cập nhật Ngày sinh
            if (birthDate != null) { profile.setBirthDate(birthDate); isUpdated = true; }

            // Cập nhật SĐT (nếu đang trống)
            if (phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                    (profile.getPhoneNumber() == null || profile.getPhoneNumber().isEmpty())) {
                profile.setPhoneNumber(phoneNumber.trim()); isUpdated = true;
            }

            // Cập nhật Email (nếu đang trống)
            if (email != null && !email.trim().isEmpty() &&
                    (profile.getEmail() == null || profile.getEmail().isEmpty())) {
                profile.setEmail(email.trim()); isUpdated = true;
            }

            // Cập nhật Khoa
            if (departmentId != null && profile.getDepartment() == null) {
                Department dept = departmentRepository.findById(departmentId).orElse(null);
                if (dept != null) { profile.setDepartment(dept); isUpdated = true; }
            }

            // CẬP NHẬT CHỨC DANH (Logic: Nếu gửi về không rỗng và hiện tại trong DB đang rỗng)
            if (academicRank != null && !academicRank.trim().isEmpty() &&
                    (profile.getAcademicRank() == null || profile.getAcademicRank().isEmpty())) {
                profile.setAcademicRank(academicRank.trim());
                isUpdated = true;
            }

            if (isUpdated) {
                userProfileRepository.save(profile);
                ra.addFlashAttribute("success", "Đã cập nhật và niêm phong hồ sơ thành công!");
            } else {
                ra.addFlashAttribute("error", "Không có thông tin mới để cập nhật hoặc thông tin đã bị khóa.");
            }
        }
        return "redirect:/profile";
    }

    @PostMapping("/update-avatar")
    @ResponseBody
    public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile file, Principal principal) {
        try {
            if (principal == null) return ResponseEntity.status(403).body(Map.of("success", false));
            User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);

            String uploadDir = "uploads/avatars/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String fileName = currentUser.getUsername() + "_" + System.currentTimeMillis() + ".png";
            Path path = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            UserProfile profile = currentUser.getUserProfile();
            profile.setAvatarUrl("/uploads/avatars/" + fileName);
            userProfileRepository.save(profile);

            return ResponseEntity.ok(Map.of("success", true, "avatarPath", profile.getAvatarUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false));
        }
    }
}