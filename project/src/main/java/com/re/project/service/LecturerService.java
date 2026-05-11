package com.re.project.service;

import com.re.project.repository.LecturerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LecturerService {
    private final LecturerRepository lecturerRepository;

    public long countActiveLecturers() {
        return lecturerRepository.count();
    }
}