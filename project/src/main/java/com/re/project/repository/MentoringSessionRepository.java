package com.re.project.repository;

import com.re.project.dto.TopLecturerDto;
import com.re.project.model.MentoringSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {
    // Tìm các ca theo giảng viên và danh sách trạng thái (VD: ACCEPTED, COMPLETED)
    List<MentoringSession> findByLecturerIdAndStatusInOrderByStartTimeDesc(Long lecturerId, List<String> statuses);
    // 1. Hàm bạn đang thiếu: Tìm danh sách chờ duyệt theo ID giảng viên
    List<MentoringSession> findByLecturerIdAndStatusOrderByStartTimeAsc(Long lecturerId, String status);

    // 2. Kiểm tra xung đột lịch (Dùng cho cả Student khi đặt và Lecturer khi duyệt)
    @Query("SELECT COUNT(m) > 0 FROM MentoringSession m " +
            "WHERE m.lecturer.id = :lecturerId " +
            "AND m.status = 'ACCEPTED' " +
            "AND (m.startTime < :endTime AND m.endTime > :startTime)")
    boolean existsConflict(@Param("lecturerId") Long lecturerId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    // 3. Đếm số ca trong ngày hôm nay (So sánh ngày từ LocalDateTime)
    @Query("SELECT COUNT(m) FROM MentoringSession m " +
            "WHERE m.lecturer.id = :lecturerId " +
            "AND CAST(m.startTime AS date) = :today " +
            "AND m.status = :status")
    long countByLecturerIdAndDateAndStatus(@Param("lecturerId") Long lecturerId,
                                           @Param("today") LocalDate today,
                                           @Param("status") String status);

    // 4. Đếm số ca chờ duyệt (Dùng Query Method mặc định)
    long countByLecturerIdAndStatus(Long lecturerId, String status);

    // 5. Đếm tổng số sinh viên "duy nhất" đã từng hỗ trợ
    @Query("SELECT COUNT(DISTINCT m.student.id) FROM MentoringSession m " +
            "WHERE m.lecturer.id = :lecturerId")
    long countDistinctStudentIdsByLecturerId(@Param("lecturerId") Long lecturerId);

    // 6. Lấy 5 ca hướng dẫn sắp tới (Dùng Pageable để Limit)
    @Query("SELECT m FROM MentoringSession m " +
            "WHERE m.lecturer.id = :lecturerId " +
            "AND m.startTime > :now " +
            "AND m.status = 'ACCEPTED' " +
            "ORDER BY m.startTime ASC")
    List<MentoringSession> findUpcomingSessions(@Param("lecturerId") Long lecturerId,
                                                @Param("now") LocalDateTime now,
                                                Pageable pageable);


    @Query("SELECT m FROM MentoringSession m " +
            "LEFT JOIN FETCH m.academicEvaluation " +  // SỬA CHỖ NÀY
            "LEFT JOIN FETCH m.borrowingRecord br " +
            "LEFT JOIN FETCH br.borrowingDetails " +
            "WHERE m.student.id = :studentId ORDER BY m.startTime DESC")
    List<MentoringSession> findFullHistoryByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT l.user.userProfile.fullName, l.department.name, COUNT(ms) " +
            "FROM MentoringSession ms JOIN ms.lecturer l " +
            "WHERE ms.status = 'COMPLETED' " +
            "GROUP BY l.id " +
            "ORDER BY COUNT(ms) DESC")
    List<Object[]> findTop5Lecturers(Pageable pageable);

    @Query("SELECT new com.re.project.dto.TopLecturerDto(l.user.userProfile.fullName, l.specialization, COUNT(ms)) " +
            "FROM MentoringSession ms JOIN ms.lecturer l " +
            "WHERE ms.status = 'COMPLETED' " +
            "GROUP BY l.id, l.user.userProfile.fullName, l.specialization " +
            "ORDER BY COUNT(ms) DESC")
    List<TopLecturerDto> findTopLecturers(Pageable pageable);
}