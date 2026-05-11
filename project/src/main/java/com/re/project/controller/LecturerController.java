package com.re.project.controller;

import com.re.project.dto.MentoringSessionDto;
import com.re.project.model.Equipment;
import com.re.project.model.User;
import com.re.project.repository.UserRepository;
import com.re.project.service.EquipmentService;
import com.re.project.service.MentoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/lecturer")
@RequiredArgsConstructor
public class LecturerController {

    private final MentoringService mentoringService;
    private final EquipmentService equipmentService;
    private final UserRepository userRepository;

    // ==========================================
    // 1. DASHBOARD GIẢNG VIÊN
    // ==========================================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null && user.getLecturer() != null) {
            Long lId = user.getLecturer().getId();

            String displayName = (user.getUserProfile() != null && user.getUserProfile().getFullName() != null)
                    ? user.getUserProfile().getFullName()
                    : user.getUsername();

            model.addAttribute("fullName", displayName);
            model.addAttribute("todaySessions", mentoringService.countTodaySessions(lId));
            model.addAttribute("pendingApprovals", mentoringService.countPendingRequests(lId));
            model.addAttribute("totalStudents", mentoringService.countUniqueStudents(lId));
            model.addAttribute("upcomingSessions", mentoringService.getUpcomingSessions(lId, 5));
        }
        model.addAttribute("view", "lecturer/lecturer-dashboard");
        return "home";
    }

    // ==========================================
    // 2. DANH SÁCH CHỜ DUYỆT
    // ==========================================
    @GetMapping("/appointments")
    public String viewAppointments(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null && user.getLecturer() != null) {
            model.addAttribute("pendingList", mentoringService.getPendingSessions(user.getLecturer().getId()));
        }
        model.addAttribute("view", "lecturer/lecturer-appointments");
        return "home";
    }

    // ==========================================
    // 3. XỬ LÝ DUYỆT & TỪ CHỐI
    // ==========================================
    @GetMapping("/approve/{id}")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        try {
            mentoringService.updateSessionStatus(id, "ACCEPTED");
            ra.addFlashAttribute("success", "Đã chốt lịch thành công! Duy chuẩn bị giáo án nhé. 😉");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/lecturer/appointments";
    }

    @GetMapping("/reject/{id}")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        try {
            mentoringService.updateSessionStatus(id, "REJECTED");
            ra.addFlashAttribute("success", "Đã từ chối yêu cầu.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/lecturer/appointments";
    }

    // ==========================================
    // 4. LỊCH TRÌNH
    // ==========================================
    @GetMapping("/schedule")
    public String viewSchedule(Model model, Principal principal) {
        User user = userRepository.findByUsername(principal.getName()).orElse(null);
        if (user != null && user.getLecturer() != null) {
            model.addAttribute("scheduleList", mentoringService.getLecturerSchedule(user.getLecturer().getId()));
            List<Equipment> equipments = equipmentService.getAllAvailable();
            model.addAttribute("equipments", equipments);
        }
        model.addAttribute("view", "lecturer/lecturer-schedule");
        return "home";
    }

    // ==========================================
    // 5. HIỂN THỊ FORM ĐÁNH GIÁ (Bắt lỗi quá thời gian)
    // ==========================================
    @GetMapping("/evaluate/{id}")
    public String showEvaluatePage(@PathVariable Long id, Model model, RedirectAttributes ra) {
        MentoringSessionDto session = mentoringService.getSessionById(id);

        if (LocalDateTime.now().isBefore(session.getStartTime())) {
            ra.addFlashAttribute("error", "Chưa đến giờ dạy, không thể đánh giá sớm Duy ơi! Đợi đúng lịch nhé. ⏳");
            return "redirect:/lecturer/schedule";
        }

        if (!"ACCEPTED".equals(session.getStatus())) {
            ra.addFlashAttribute("error", "Ca học này không ở trạng thái có thể đánh giá.");
            return "redirect:/lecturer/schedule";
        }

        List<Equipment> equipments = equipmentService.getAllAvailable();
        model.addAttribute("session", session);
        model.addAttribute("equipments", equipments);
        model.addAttribute("view", "lecturer/lecturer-evaluate");
        return "home";
    }

    // ==========================================
    // 6. LƯU ĐÁNH GIÁ & CẤP THIẾT BỊ (Nghiệp vụ cốt lõi)
    // ==========================================
    @PostMapping("/evaluate/save")
    public String saveEvaluation(
            @RequestParam Long sessionId,
            @RequestParam Double score,
            @RequestParam(required = false, defaultValue = "") String quickFeedback,
            @RequestParam(required = false, defaultValue = "") String feedback,
            @RequestParam(required = false) List<Long> selectedEquipments,
            RedirectAttributes ra) {
        try {
            String fullFeedback = feedback;
            if (!quickFeedback.isBlank()) {
                fullFeedback = "[" + quickFeedback + "] " + feedback;
            }

            mentoringService.saveEvaluation(sessionId, score, fullFeedback, selectedEquipments);
            ra.addFlashAttribute("success", "Đã lưu đánh giá và chỉ định thiết bị thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi lưu dữ liệu: " + e.getMessage());
        }
        return "redirect:/lecturer/schedule";
    }
}