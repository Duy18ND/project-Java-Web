package com.re.project.service;

import com.re.project.dto.UserRegisterRequest;
import com.re.project.model.User;
import com.re.project.model.UserProfile;
import com.re.project.model.UserRole;
import com.re.project.repository.EquipmentRepository;
import com.re.project.repository.UserProfileRepository;
import com.re.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public void registerNewUser(UserRegisterRequest request) {
        // Kiểm tra trùng lặp username, email, phone
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("USER_EXISTS");
        }
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_EXISTS");
        }
        if (userProfileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("PHONE_EXISTS");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Gán Role lấy trực tiếp từ DTO (mặc định đã là ROLE_STUDENT)
        newUser.setRole(request.getRole());

        // BẮT BUỘC SAVE TRƯỚC ĐỂ DATABASE TẠO ID TỰ ĐỘNG
        newUser = userRepository.save(newUser);

        // 3. Tạo Profile cho User
        UserProfile profile = new UserProfile();
        profile.setUser(newUser);
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setPhoneNumber(request.getPhoneNumber());

        // ==========================================
        // 4. LOGIC XỬ LÝ MÃ SINH VIÊN VÀ HỌC HÀM
        // ==========================================

        if (UserRole.STUDENT.equals(newUser.getRole())) {
            // String.format("%03d", id) tự động thêm số 0 ở đầu để đủ 3 chữ số
            String generatedStudentCode = String.format("B24DTCN%03d", newUser.getId());
            profile.setStudentCode(generatedStudentCode);

            // Mặc định rank cho sinh viên
            profile.setAcademicRank("Student");

        } else if (UserRole.LECTURER.equals(newUser.getRole())) {
            // Giảng viên không có mã sinh viên
            profile.setStudentCode(null);
            profile.setAcademicRank(null);
        }

        // 5. Lưu Profile xuống Database
        userProfileRepository.save(profile);
    }

    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) throw new RuntimeException("Không tìm thấy thiết bị để xóa");
        equipmentRepository.deleteById(id);
    }
}