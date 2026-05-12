package com.re.project.controller;

import com.re.project.dto.EquipmentDto;
import com.re.project.model.BorrowingRecord;
import com.re.project.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EquipmentService equipmentService;
    private final LabService labService;
    private final AdminService adminService;
    private final LecturerService lecturerService;
    private final MentoringService mentoringService;

    @GetMapping("/dashboard")
    public String viewDashBoard(Model model){
        model.addAttribute("totalEquipments", equipmentService.countTotalEquipments());
        model.addAttribute("pendingCount", adminService.countPendingRequests());
        model.addAttribute("issuedCount", adminService.countIssuedRecords());
        model.addAttribute("lecturerCount", lecturerService.countActiveLecturers());

        model.addAttribute("labStats", adminService.getLabUsageStats());
        model.addAttribute("topLecturers", mentoringService.getTopLecturers(5));
        model.addAttribute("recentBorrows", adminService.getRecentBorrowingRecords(5));

        model.addAttribute("view", "admin/admin-dashboard");
        return "home";
    }

    @GetMapping("/equipment")
    public String viewEquipment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long labId,
            Model model) {

        if (page < 0) page = 0;
        int pageSize = 5;

        try {
            Page<EquipmentDto> equipmentPage = equipmentService.getEquipmentsPaged(keyword, labId, page, pageSize);

            model.addAttribute("equipmentPage", equipmentPage);
            model.addAttribute("equipments", equipmentPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", equipmentPage.getTotalPages());
            model.addAttribute("totalItems", equipmentPage.getTotalElements());

            model.addAttribute("keyword", keyword);
            model.addAttribute("selectedLabId", labId);

            model.addAttribute("labs", labService.getAll());

        } catch (Exception e) {
            System.out.println("Lỗi load dữ liệu: " + e.getMessage());

            model.addAttribute("equipments", new ArrayList<>());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
        }

        model.addAttribute("equipmentDto", new EquipmentDto());
        model.addAttribute("view", "admin/admin-equipment");
        return "home";
    }

    @PostMapping("/equipment/save")
    public String saveEquipment(@Valid @ModelAttribute("equipmentDto") EquipmentDto dto,
                                BindingResult result, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("error", "Dữ liệu nhập không hợp lệ!");
            return "redirect:/admin/equipment";
        }

        try {
            equipmentService.save(dto);
            ra.addFlashAttribute("success", "Lưu thiết bị thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/equipment";
    }

    @GetMapping("/equipment/delete/{id}")
    public String deleteEquipment(@PathVariable Long id, RedirectAttributes ra) {
        equipmentService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa thiết bị!");
        return "redirect:/admin/equipment";
    }

    @GetMapping("/equipment/api/{id}")
    @ResponseBody
    public EquipmentDto getEquipmentApi(@PathVariable Long id) {
        return equipmentService.getById(id);
    }

    // ==========================================
    // QUẢN LÝ MƯỢN TRẢ (ĐÃ CẬP NHẬT CHIA TAB)
    // ==========================================
    @GetMapping("/borrow-requests")
    public String viewBorrowRequests(Model model){
        // 1. Lấy TẤT CẢ phiếu mượn thay vì chỉ lấy phiếu Pending
        List<BorrowingRecord> allRecords = adminService.getAllBorrowingRecords();

        // 2. Tính toán số lượng cho từng Tab
        long pendingCount = allRecords.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long borrowingCount = allRecords.stream().filter(r -> "BORROWING".equals(r.getStatus())).count();
        long returnedCount = allRecords.stream().filter(r -> "RETURNED".equals(r.getStatus())).count();

        // 3. Đẩy dữ liệu ra giao diện
        model.addAttribute("allRecords", allRecords);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("borrowingCount", borrowingCount);
        model.addAttribute("returnedCount", returnedCount);

        model.addAttribute("view", "admin/admin-borrow-requests");
        return "home";
    }

    // Nút Bấm: DUYỆT (Cho mượn)
    @GetMapping("/borrow-requests/approve/{id}")
    public String approveBorrowRequest(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminService.confirmAndIssueEquipment(id);
            ra.addFlashAttribute("success", "Đã duyệt và xuất kho thành công phiếu mượn #" + id);
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage()); // Báo lỗi nếu thiếu Tồn kho
        }
        return "redirect:/admin/borrow-requests";
    }

    // Nút Bấm: TỪ CHỐI (Không cho mượn)
    @GetMapping("/borrow-requests/reject/{id}")
    public String rejectBorrowRequest(@PathVariable Long id, RedirectAttributes ra) {
        try {
            adminService.rejectBorrowingRequest(id);
            ra.addFlashAttribute("success", "Đã từ chối phiếu mượn #" + id);
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi khi từ chối phiếu mượn.");
        }
        return "redirect:/admin/borrow-requests";
    }

    // Nút Bấm: THU HỒI (Admin xác nhận nhập kho)
    @GetMapping("/borrow-requests/return/{id}")
    public String confirmReturnEquipmentAdmin(@PathVariable Long id, RedirectAttributes ra) {
        try {
            // Tận dụng hàm returnEquipment đã viết bên MentoringService để hoàn kho
            mentoringService.returnEquipment(id);
            ra.addFlashAttribute("success", "Đã xác nhận thu hồi thiết bị và cộng lại vào kho thành công!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/borrow-requests";
    }
}