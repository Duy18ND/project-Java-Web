package com.re.project.service;

import com.re.project.dto.EquipmentDto;
import com.re.project.model.Equipment;
import com.re.project.model.Lab;
import com.re.project.repository.EquipmentRepository;
import com.re.project.repository.LabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final LabRepository labRepository;

    public List<Equipment> getAllAvailable() {
        return equipmentRepository.findByAvailableQuantityGreaterThan(0);
    }

    public List<EquipmentDto> getAll() {
        return equipmentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public EquipmentDto getById(Long id) {
        Equipment eq = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị với ID: " + id));
        return convertToDto(eq);
    }

    // ========= ĐÃ FIX LOGIC QUẢN LÝ KHO TẠI ĐÂY =========
    public void save(EquipmentDto dto) {
        Equipment equipment;

        if (dto.getId() != null) {
            // TRƯỜNG HỢP: SỬA (UPDATE)
            equipment = equipmentRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị để cập nhật"));

            // 1. Tính xem máy này đang có bao nhiêu cái bị mượn ra ngoài
            int currentlyBorrowed = equipment.getTotalQuantity() - equipment.getAvailableQuantity();

            // 2. Cập nhật Tổng số lượng mới
            int newTotal = dto.getTotalQuantity();

            // 3. Tính lại Tồn kho mới
            int newAvailable = newTotal - currentlyBorrowed;

            // BẢO HIỂM: Chặn Admin không được giảm Tổng số lượng xuống thấp hơn số lượng sinh viên đang mượn
            if (newAvailable < 0) {
                throw new RuntimeException("Lỗi: Không thể giảm tổng số lượng vì hiện đang có " + currentlyBorrowed + " thiết bị được mượn bên ngoài!");
            }

            equipment.setTotalQuantity(newTotal);
            equipment.setAvailableQuantity(newAvailable);

        } else {
            // TRƯỜNG HỢP: THÊM MỚI (INSERT)
            equipment = new Equipment();
            equipment.setTotalQuantity(dto.getTotalQuantity());
            // Hàng mới nhập về, chưa ai mượn -> Tồn kho = Tổng số lượng
            equipment.setAvailableQuantity(dto.getTotalQuantity());
        }

        // Cập nhật các trường thông tin cơ bản khác
        equipment.setName(dto.getName());
        equipment.setCategory(dto.getCategory());

        if (dto.getLabId() != null) {
            Lab lab = labRepository.findById(dto.getLabId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Lab với ID: " + dto.getLabId()));
            equipment.setLab(lab);
        }

        equipmentRepository.save(equipment);
    }
    // ====================================================

    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy thiết bị để xóa");
        }
        equipmentRepository.deleteById(id);
    }

    private EquipmentDto convertToDto(Equipment eq) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(eq.getId());
        dto.setName(eq.getName());
        dto.setCategory(eq.getCategory());
        dto.setTotalQuantity(eq.getTotalQuantity());
        dto.setAvailableQuantity(eq.getAvailableQuantity());

        if (eq.getLab() != null) {
            dto.setLabId(eq.getLab().getId());
            dto.setLabName(eq.getLab().getName());
        }
        return dto;
    }

    public long countTotalEquipments() {
        return equipmentRepository.count();
    }

    public Page<EquipmentDto> getEquipmentsPaged(String keyword, Long labId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String safeKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Page<Equipment> equipmentPage = equipmentRepository.findByFiltersAndPage(safeKeyword, labId, pageable);
        return equipmentPage.map(this::convertToDto);
    }
}