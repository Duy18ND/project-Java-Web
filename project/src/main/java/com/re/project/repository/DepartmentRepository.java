package com.re.project.repository;

import com.re.project.model.BorrowingRecord;
import com.re.project.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
