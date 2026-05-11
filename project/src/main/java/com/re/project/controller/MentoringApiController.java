package com.re.project.controller;

import com.re.project.dto.LecturerDto;
import com.re.project.service.MentoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class MentoringApiController {

    private final MentoringService mentoringService;

    // API lấy danh sách giảng viên (Trả về JSON cho AJAX)
    @GetMapping("/lecturers")
    @ResponseBody
    public List<LecturerDto> getLecturersByDept(@RequestParam Long deptId) {
        return mentoringService.getLecturersByDepartment(deptId);
    }

    /**
     * Hủy lịch hẹn dành cho Sinh viên
     * URL: /api/mentoring/cancel/{id}
     */
    @GetMapping("/mentoring/cancel/{id}")
    public String cancelMentoring(@PathVariable Long id, RedirectAttributes ra) {
        try {
            mentoringService.cancelSession(id);
            // Gửi thông báo thành công về trang history
            ra.addFlashAttribute("success", "Đã hủy lịch hẹn và giải phóng thời gian cho giảng viên.");
        } catch (Exception e) {
            // Gửi thông báo lỗi nếu vi phạm quy tắc 24h
            ra.addFlashAttribute("error", "Hủy lịch thất bại: " + e.getMessage());
        }

        // Chuyển hướng về trang danh sách lịch sử của sinh viên
        return "redirect:/student/mentoring/history";
    }
}