package com.re.project.repository;

import com.re.project.dto.LabUsageDto;
import com.re.project.model.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // Spring Boot tự động hiểu hàm này
    List<Equipment> findByAvailableQuantityGreaterThan(int quantity);

    // Thống kê thiết bị theo từng phòng Lab cho biểu đồ
    @Query("SELECT new com.re.project.dto.LabUsageDto(l.name, CAST(COUNT(e) AS int), 50, (COUNT(e) * 100.0 / 50)) " +
            "FROM Equipment e JOIN e.lab l GROUP BY l.name")
    List<LabUsageDto> getLabUsageStats();

    // Query tìm kiếm và phân trang
    @Query("SELECT e FROM Equipment e " +
            "WHERE (:keyword IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:labId IS NULL OR e.lab.id = :labId) " +
            "ORDER BY e.id DESC")
    Page<Equipment> findByFiltersAndPage(@Param("keyword") String keyword,
                                         @Param("labId") Long labId,
                                         Pageable pageable);
}