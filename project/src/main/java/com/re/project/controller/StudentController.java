package com.re.project.controller;

import com.re.project.dto.MentoringSessionDto;
import com.re.project.model.User;
import com.re.project.model.UserProfile;
import com.re.project.repository.UserProfileRepository;
import com.re.project.repository.UserRepository;
import com.re.project.service.MentoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final MentoringService mentoringService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    // ==========================================
    // CÁC TRANG CƠ BẢN (DASHBOARD)
    // ==========================================

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);

            if (currentUser != null) {
                // 1. Truyền Họ tên
                if (currentUser.getUserProfile() != null) {
                    model.addAttribute("fullName", currentUser.getUserProfile().getFullName());
                } else {
                    model.addAttribute("fullName", currentUser.getUsername());
                }

                // 2. Lấy danh sách lịch sử thật từ Database
                List<MentoringSessionDto> historyList = mentoringService.getStudentHistory(currentUser.getId());
                model.addAttribute("recentActivities", historyList);

                // 3. Tự động tính toán các con số thống kê (Không dùng số fake nữa)
                long total = historyList.size();
                long pending = historyList.stream().filter(s -> s.getStatus().equals("PENDING")).count();
                // Đếm số ca có mượn thiết bị
                long borrowed = historyList.stream().filter(s -> s.getBorrowingRecordId() != null).count();

                model.addAttribute("totalSessions", total);
                model.addAttribute("pendingSessions", pending);
                model.addAttribute("borrowedDevices", borrowed);
            }
        }

        model.addAttribute("view", "student/student-dashboard");
        return "home";
    }

    // ==========================================
    // CORE-05: ĐẶT LỊCH CỐ VẤN & CHỐNG XUNG ĐỘT
    // ==========================================

    @GetMapping("/mentoring/book")
    public String showBookingForm(Model model, Principal principal) {
        model.addAttribute("departments", mentoringService.getAllDepartments());

        MentoringSessionDto dto = new MentoringSessionDto();

        // CHUẨN: Lấy ID User từ Spring Security
        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            if (currentUser != null) {
                dto.setStudentId(currentUser.getId());
            }
        }

        model.addAttribute("bookingDto", dto);
        model.addAttribute("view", "student/book-session");
        return "home";
    }

    @PostMapping("/mentoring/book")
    public String processBooking(@Valid @ModelAttribute("bookingDto") MentoringSessionDto dto,
                                 BindingResult result,
                                 Model model,
                                 Principal principal,
                                 RedirectAttributes ra) {

        System.out.println("\n========== [DEBUG] BẮT ĐẦU SUBMIT FORM ĐẶT LỊCH ==========");

        // 1. In dữ liệu gốc từ HTML gửi lên
        System.out.println("1. Dữ liệu từ form giao diện gửi xuống:");
        System.out.println("   - Lecturer ID : " + dto.getLecturerId());
        System.out.println("   - Start Time  : " + dto.getStartTime());
        System.out.println("   - End Time    : " + dto.getEndTime());
        System.out.println("   - Topic       : " + dto.getTopic());

        // 2. Ép cứng ID sinh viên từ Spring Security (Khắc phục lỗi NULL)
        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            if (currentUser != null) {
                dto.setStudentId(currentUser.getId());
                System.out.println("2. [THÀNH CÔNG] Đã map Student ID từ Security: " + currentUser.getId() + " (" + currentUser.getUsername() + ")");
            } else {
                System.out.println("2. [LỖI] Không tìm thấy User trong Database khớp với Principal!");
            }
        } else {
            System.out.println("2. [LỖI] Principal đang bị null (Người dùng chưa đăng nhập hoặc lỗi Session)!");
        }

        // 3. Kiểm tra Validation (Các ràng buộc @NotNull, @NotBlank)
        if (result.hasErrors()) {
            System.out.println("3. [THẤT BẠI] Dữ liệu KHÔNG hợp lệ (Bị Spring Validation chặn):");
            result.getFieldErrors().forEach(error -> {
                System.out.println("   -> Lỗi ở trường [" + error.getField() + "]: " + error.getDefaultMessage());
            });
            System.out.println("=> Đang trả về lại giao diện Form HTML...");

            model.addAttribute("departments", mentoringService.getAllDepartments());
            model.addAttribute("view", "student/book-session");
            return "home";
        }

        // 4. Bắt đầu lưu xuống Database
        try {
            System.out.println("3. Dữ liệu hợp lệ 100%. Chuẩn bị gọi Service lưu xuống DB...");
            mentoringService.scheduleSession(dto);

            System.out.println("4. [HOÀN TẤT] Đã lưu lịch tư vấn thành công!");
            ra.addFlashAttribute("success", "Đặt lịch thành công! Vui lòng chờ giảng viên xác nhận.");
            return "redirect:/student/mentoring/history";

        } catch (RuntimeException e) {
            // Bắt lỗi logic (Ví dụ: Trùng lịch, sai thời gian...)
            System.out.println("4. [BỊ CHẶN BỞI LOGIC SERVICE]: " + e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/student/mentoring/book";
        }
    }

    // ==========================================
    // CORE-07: TRA CỨU HỒ SƠ HỌC THUẬT (LỊCH SỬ)
    // ==========================================

    @GetMapping("/mentoring/history")
    public String viewHistory(Model model, Principal principal) {

        // CHUẨN: Lấy ID User từ Spring Security
        if (principal != null) {
            User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);
            if (currentUser != null) {
                model.addAttribute("historyList", mentoringService.getStudentHistory(currentUser.getId()));
            }
        }

        model.addAttribute("view", "student/student-history");
        return "home";
    }

    // ==========================================
    // CORE-09: HỦY LỊCH & GIẢI PHÓNG SLOT
    // ==========================================

    @GetMapping("/mentoring/cancel/{id}")
    public String cancelSession(@PathVariable Long id, RedirectAttributes ra) {
        try {
            mentoringService.cancelSession(id);
            ra.addFlashAttribute("success", "Đã hủy lịch thành công. Slot đã được giải phóng cho các bạn khác!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/mentoring/history";
    }

    // ==========================================
    // NGHIỆP VỤ TRẢ THIẾT BỊ (MỚI THÊM)
    // ==========================================
    @GetMapping("/mentoring/return/{borrowId}")
    public String returnEquipment(@PathVariable Long borrowId, RedirectAttributes ra) {
        try {
            mentoringService.returnEquipment(borrowId);
            ra.addFlashAttribute("success", "Xác nhận trả thiết bị thành công! Kho đã được cập nhật.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/student/mentoring/history";
    }

    // ==========================================
    // CẬP NHẬT PROFILE (LOGIC: ĐIỀN CHỖ TRỐNG & KHÓA)
    // ==========================================
    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate birthDate,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String email,
            Principal principal,
            RedirectAttributes ra) {

        if (principal == null) return "redirect:/login";

        User currentUser = userRepository.findByUsername(principal.getName()).orElse(null);

        if (currentUser != null && currentUser.getUserProfile() != null) {
            UserProfile profile = currentUser.getUserProfile();
            boolean isUpdated = false;

            // 1. NGÀY SINH: Luôn luôn cho phép cập nhật (Không khóa)
            if (birthDate != null) {
                profile.setBirthDate(birthDate);
                isUpdated = true;
            }

            // 2. SỐ ĐIỆN THOẠI: Chỉ nhận dữ liệu nếu DB đang trống (Chống hack đổi HTML)
            if (phoneNumber != null && !phoneNumber.trim().isEmpty() &&
                    (profile.getPhoneNumber() == null || profile.getPhoneNumber().trim().isEmpty())) {
                profile.setPhoneNumber(phoneNumber.trim());
                isUpdated = true;
            }

            // 3. EMAIL: Chỉ nhận dữ liệu nếu DB đang trống
            if (email != null && !email.trim().isEmpty() &&
                    (profile.getEmail() == null || profile.getEmail().trim().isEmpty())) {
                profile.setEmail(email.trim());
                isUpdated = true;
            }

            // Lưu xuống Database nếu có thay đổi
            if (isUpdated) {
                userProfileRepository.save(profile);
                ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công! Các thông định danh đã được hệ thống khóa.");
            } else {
                ra.addFlashAttribute("error", "Không có thông tin hợp lệ nào được cập nhật.");
            }
        } else {
            ra.addFlashAttribute("error", "Không tìm thấy hồ sơ cá nhân!");
        }

        // Đảm bảo bạn đã có hàm @GetMapping("/profile") để hứng lệnh redirect này nhé
        return "redirect:/student/profile";
    }
}