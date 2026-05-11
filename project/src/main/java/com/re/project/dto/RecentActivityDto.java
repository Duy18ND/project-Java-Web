package com.re.project.dto;

import java.time.LocalDateTime;

public record RecentActivityDto(
        Long id,               // Tương ứng br.id
        String studentName,    // Tương ứng fullName
        String equipmentNames, // Tương ứng 'Thiết bị thực hành'
        LocalDateTime date,    // Tương ứng br.borrowDate
        String status          // Tương ứng br.status
) {
}