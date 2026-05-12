package com.re.project.service;

import com.re.project.dto.LabUsageDto;
import com.re.project.dto.RecentActivityDto;
import com.re.project.model.BorrowingDetail;
import com.re.project.model.BorrowingRecord;
import com.re.project.model.Equipment;
import com.re.project.repository.BorrowingRecordRepository;
import com.re.project.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final BorrowingRecordRepository borrowingRepository;
    private final EquipmentRepository equipmentRepository;

    // Đếm các yêu cầu đang chờ duyệt (PENDING)
    public long countPendingRequests() {
        return borrowingRepository.countByStatus("PENDING");
    }

    // Đếm các phiếu đã xuất kho và đang được mượn (BORROWING)
    public long countIssuedRecords() {
        return borrowingRepository.countByStatus("BORROWING");
    }

    public List<LabUsageDto> getLabUsageStats() {
        return equipmentRepository.getLabUsageStats();
    }

    public List<RecentActivityDto> getRecentBorrowingRecords(int limit) {
        return borrowingRepository.findRecentActivities(PageRequest.of(0, limit));
    }

    // Lấy danh sách phiếu chờ duyệt để hiện lên trang Duyệt mượn
    public List<BorrowingRecord> getPendingBorrowingRecords() {
        return borrowingRepository.findByStatus("PENDING");
    }

    // Xác nhận cho mượn (Duyệt)
    @Transactional
    public void confirmAndIssueEquipment(Long recordId) {
        BorrowingRecord record = borrowingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn!"));

        // 1. Kiểm tra tồn kho và trừ số lượng
        for (BorrowingDetail detail : record.getBorrowingDetails()) {
            Equipment eq = detail.getEquipment();
            if (eq.getAvailableQuantity() < detail.getQuantity()) {
                throw new RuntimeException("Thiết bị " + eq.getName() + " không đủ trong kho!");
            }
            eq.setAvailableQuantity(eq.getAvailableQuantity() - detail.getQuantity());
            equipmentRepository.save(eq);
        }

        // 2. Chuyển trạng thái từ PENDING -> BORROWING (Đang mượn)
        record.setStatus("BORROWING");
        record.setBorrowDate(LocalDateTime.now());
        borrowingRepository.save(record);
    }

    // Từ chối yêu cầu mượn
    @Transactional
    public void rejectBorrowingRequest(Long recordId) {
        BorrowingRecord record = borrowingRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn!"));

        // Chuyển trạng thái sang REJECTED
        record.setStatus("REJECTED");
        borrowingRepository.save(record);
    }

    public List<BorrowingRecord> getAllBorrowingRecords() {
        // Lấy toàn bộ phiếu mượn để quản lý
        return borrowingRepository.findAll();
    }
}