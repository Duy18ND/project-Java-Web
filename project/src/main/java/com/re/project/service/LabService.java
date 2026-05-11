package com.re.project.service;

import com.re.project.dto.LabDto;
import com.re.project.model.Lab;
import com.re.project.repository.LabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabService {
    private final LabRepository labRepository;

    @Transactional(readOnly = true)
    public List<LabDto> getAll() {
        return labRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Quan trọng: Phải có Transactional ở đây vì hàm mapToDto có gọi .size() của Lazy Collection
    @Transactional(readOnly = true)
    public LabDto getById(Long id) {
        Lab lab = labRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng Lab"));
        return mapToDto(lab);
    }

    private LabDto mapToDto(Lab lab) {
        return LabDto.builder()
                .id(lab.getId())
                .name(lab.getName())
                .description(lab.getDescription())
                // Tính số lượng thiết bị đang có trong phòng này
                .equipmentCount(lab.getEquipments() != null ? lab.getEquipments().size() : 0)
                .build();
    }
}