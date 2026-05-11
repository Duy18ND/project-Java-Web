package com.re.project.repository;

import com.re.project.model.Equipment;
import com.re.project.model.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRepository extends JpaRepository<Lab,Long> {
}
