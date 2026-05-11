package com.re.project.repository;

import com.re.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Tìm để đăng nhập
    Optional<User> findByUsername(String username);
    // Kiểm tra trùng tên đăng nhập
    boolean existsByUsername(String username);
}
