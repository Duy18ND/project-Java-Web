package com.re.project.repository;

import com.re.project.dto.RecentActivityDto;
import com.re.project.model.BorrowingRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    // Hàm đếm số phiếu chờ (Dùng cho Card Dashboard)
    long countByStatus(String status);

    // Hàm lấy danh sách phiếu cho Admin duyệt (Dùng cho trang admin-borrowing)
    List<BorrowingRecord> findByStatus(String status);

    // FIX LỖI BÁO ĐỎ: Query lấy dữ liệu đổ vào DTO cho Dashboard
    // Lưu ý: com.re.project.dto.RecentActivityDto phải khớp với package của Duy
    @Query("SELECT new com.re.project.dto.RecentActivityDto(" +
            "br.id, s.student.userProfile.fullName, 'Thiết bị thực hành', br.borrowDate, br.status) " +
            "FROM BorrowingRecord br JOIN br.session s " +
            "ORDER BY br.borrowDate DESC")
    List<RecentActivityDto> findRecentActivities(Pageable pageable);

}