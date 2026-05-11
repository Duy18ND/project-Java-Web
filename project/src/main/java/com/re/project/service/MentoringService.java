package com.re.project.service;

import com.re.project.dto.*;
import com.re.project.model.*;
import com.re.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentoringService {

    private final MentoringSessionRepository mentoringRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;
    private final AcademicEvaluationRepository academicEvaluationRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final EquipmentRepository equipmentRepository;

    // ==========================================
    // 1. LẤY DỮ LIỆU CƠ BẢN
    // ==========================================
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(dept -> DepartmentDto.builder()
                        .id(dept.getId())
                        .name(dept.getName())
                        .description(dept.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    public List<LecturerDto> getLecturersByDepartment(Long departmentId) {
        return lecturerRepository.findByDepartmentId(departmentId).stream()
                .map(this::convertToLecturerDto).collect(Collectors.toList());
    }

    // ==========================================
    // 2. QUẢN LÝ CA HỌC
    // ==========================================
    @Transactional(readOnly = true)
    public MentoringSessionDto getSessionById(Long id) {
        MentoringSession session = mentoringRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca tư vấn ID: " + id));
        return convertToDto(session);
    }

    @Transactional
    public void scheduleSession(MentoringSessionDto dto) {
        if (dto.getStartTime().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Thời gian không hợp lệ!");

        if (mentoringRepository.existsConflict(dto.getLecturerId(), dto.getStartTime(), dto.getEndTime()))
            throw new RuntimeException("Giảng viên đã có lịch khác trong khung giờ này!");

        User student = userRepository.findById(dto.getStudentId()).orElseThrow();
        Lecturer lecturer = lecturerRepository.findById(dto.getLecturerId()).orElseThrow();

        MentoringSession session = new MentoringSession();
        session.setStudent(student);
        session.setLecturer(lecturer);
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setTopic(dto.getTopic());
        session.setStatus("PENDING");

        mentoringRepository.save(session);
    }

    @Transactional
    public void updateSessionStatus(Long sessionId, String status) {
        MentoringSession session = mentoringRepository.findById(sessionId).orElseThrow();
        if ("ACCEPTED".equals(status) && mentoringRepository.existsConflict(
                session.getLecturer().getId(), session.getStartTime(), session.getEndTime())) {
            throw new RuntimeException("Lịch bị trùng!");
        }
        session.setStatus(status);
        mentoringRepository.save(session);
    }

    // ==========================================
    // 3. THỐNG KÊ & LỊCH TRÌNH
    // ==========================================
    public long countTodaySessions(Long lecturerId) {
        return mentoringRepository.countByLecturerIdAndDateAndStatus(lecturerId, LocalDate.now(), "ACCEPTED");
    }

    public long countPendingRequests(Long lecturerId) {
        return mentoringRepository.countByLecturerIdAndStatus(lecturerId, "PENDING");
    }

    public long countUniqueStudents(Long lecturerId) {
        return mentoringRepository.countDistinctStudentIdsByLecturerId(lecturerId);
    }

    public List<MentoringSessionDto> getUpcomingSessions(Long lecturerId, int limit) {
        return mentoringRepository.findUpcomingSessions(lecturerId, LocalDateTime.now(), PageRequest.of(0, limit))
                .stream().map(this::convertToDto).toList();
    }

    public List<MentoringSessionDto> getPendingSessions(Long lecturerId) {
        return mentoringRepository.findByLecturerIdAndStatusOrderByStartTimeAsc(lecturerId, "PENDING")
                .stream().map(this::convertToDto).toList();
    }

    public List<MentoringSessionDto> getLecturerSchedule(Long lecturerId) {
        return mentoringRepository.findByLecturerIdAndStatusInOrderByStartTimeDesc(
                lecturerId, List.of("ACCEPTED", "COMPLETED")
        ).stream().map(this::convertToDto).toList();
    }

    public List<TopLecturerDto> getTopLecturers(int limit) {
        return mentoringRepository.findTopLecturers(PageRequest.of(0, limit));
    }

    // ==========================================
    // 4. LƯU ĐÁNH GIÁ & CẤP THIẾT BỊ (CORE-06)
    // ==========================================
    @Transactional
    public void saveEvaluation(Long sessionId, Double score, String feedback, List<Long> equipmentIds) {
        MentoringSession session = mentoringRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca tư vấn ID: " + sessionId));

        AcademicEvaluation eval = session.getAcademicEvaluation();
        if (eval == null) {
            eval = new AcademicEvaluation();
            eval.setMentoringSession(session);
        }

        eval.setScore(score);
        eval.setFeedback(feedback);
        eval.setEvaluationDate(LocalDateTime.now());
        academicEvaluationRepository.save(eval);

        if (equipmentIds != null && !equipmentIds.isEmpty()) {
            List<Equipment> selectedEquips = equipmentRepository.findAllById(equipmentIds);

            if (!selectedEquips.isEmpty()) {
                BorrowingRecord record = session.getBorrowingRecord();
                if (record == null) {
                    record = new BorrowingRecord();
                    record.setSession(session);
                    record.setBorrowDate(LocalDateTime.now());
                }

                // Chế độ chờ cấp phát
                record.setStatus("PENDING");

                if (record.getBorrowingDetails() != null) {
                    record.getBorrowingDetails().clear();
                } else {
                    record.setBorrowingDetails(new ArrayList<>());
                }

                List<BorrowingDetail> details = record.getBorrowingDetails();
                for (Equipment eq : selectedEquips) {
                    BorrowingDetail detail = new BorrowingDetail();
                    detail.setBorrowingRecord(record);
                    detail.setEquipment(eq);
                    detail.setQuantity(1);
                    details.add(detail);
                }
                borrowingRecordRepository.save(record);
            }
        }

        session.setStatus("COMPLETED");
        mentoringRepository.save(session);
    }

    // ==========================================
    // 5. HỦY CA DẠY (CORE-09)
    // ==========================================
    @Transactional
    public void cancelSession(Long sessionId) {
        MentoringSession session = mentoringRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca tư vấn!"));

        if ("COMPLETED".equals(session.getStatus()) || "REJECTED".equals(session.getStatus())) {
            throw new RuntimeException("Không thể hủy ca tư vấn đã hoàn thành hoặc đã bị từ chối.");
        }

        if (session.getStartTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new RuntimeException("Quá hạn hủy lịch! Bạn chỉ có thể hủy trước giờ bắt đầu ít nhất 24 tiếng.");
        }

        session.setStatus("CANCELLED");
        mentoringRepository.save(session);
    }

    // ==========================================
    // 6. NGHIỆP VỤ TRẢ THIẾT BỊ (MỚI)
    // ==========================================
    @Transactional
    public void returnEquipment(Long borrowId) {
        BorrowingRecord record = borrowingRecordRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu mượn!"));

        if (!"BORROWING".equals(record.getStatus())) {
            throw new RuntimeException("Phiếu này đã được trả hoặc chưa được Admin xuất kho!");
        }

        // 1. Hoàn tồn kho
        for (BorrowingDetail detail : record.getBorrowingDetails()) {
            Equipment eq = detail.getEquipment();
            eq.setAvailableQuantity(eq.getAvailableQuantity() + detail.getQuantity());
            equipmentRepository.save(eq);
        }

        // 2. Cập nhật trạng thái
        record.setStatus("RETURNED");
        record.setActualReturnDate(LocalDateTime.now());
        borrowingRecordRepository.save(record);
    }

    // ==========================================
    // 7. DÀNH CHO SINH VIÊN (STUDENT)
    // ==========================================
    @Transactional(readOnly = true)
    public List<MentoringSessionDto> getStudentHistory(Long studentId) {
        return mentoringRepository.findFullHistoryByStudentId(studentId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ==========================================
    // 8. HELPER METHODS (CONVERTERS)
    // ==========================================
    private MentoringSessionDto convertToDto(MentoringSession session) {
        MentoringSessionDto dto = new MentoringSessionDto();
        dto.setId(session.getId());
        dto.setTopic(session.getTopic());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setStatus(session.getStatus());

        if (session.getStudent() != null) {
            dto.setStudentId(session.getStudent().getId());
            dto.setStudentName(session.getStudent().getUserProfile() != null ?
                    session.getStudent().getUserProfile().getFullName() : session.getStudent().getUsername());
        }

        if (session.getLecturer() != null && session.getLecturer().getUser() != null) {
            dto.setLecturerId(session.getLecturer().getId());
            dto.setLecturerName(session.getLecturer().getUser().getUserProfile() != null ?
                    session.getLecturer().getUser().getUserProfile().getFullName() : "Giảng viên");
        }

        if (session.getAcademicEvaluation() != null) {
            dto.setEvaluationId(session.getAcademicEvaluation().getId());
            dto.setScore(session.getAcademicEvaluation().getScore());
            dto.setFeedback(session.getAcademicEvaluation().getFeedback());
        }

        if (session.getBorrowingRecord() != null) {
            dto.setBorrowingRecordId(session.getBorrowingRecord().getId());
            dto.setBorrowingStatus(session.getBorrowingRecord().getStatus());

            if (session.getBorrowingRecord().getBorrowingDetails() != null) {
                String equipments = session.getBorrowingRecord().getBorrowingDetails().stream()
                        .map(d -> d.getQuantity() + "x " + d.getEquipment().getName())
                        .collect(Collectors.joining(", "));
                dto.setBorrowedEquipments(equipments);
            }
        }
        return dto;
    }

    private LecturerDto convertToLecturerDto(Lecturer l) {
        LecturerDto dto = new LecturerDto();
        dto.setId(l.getId());
        if (l.getUser() != null) {
            dto.setUserId(l.getUser().getId());
            dto.setFullName(l.getUser().getUserProfile() != null ?
                    l.getUser().getUserProfile().getFullName() : l.getUser().getUsername());
        }
        dto.setDegree(l.getDegree());
        dto.setSpecialization(l.getSpecialization());
        return dto;
    }
}