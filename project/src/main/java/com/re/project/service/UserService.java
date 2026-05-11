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
        //Kiểm tra trùng lặp username, email, phone
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("USER_EXISTS");
        }
        if (userProfileRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_EXISTS");
        }
        if (userProfileRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("PHONE_EXISTS");
        }

        //Nếu khong bị trùng thì tạo vào lưu tk
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(UserRole.STUDENT);
        //Lưu
        User saveUser = userRepository.save(newUser);

        //Tạo hồ sơ nối với User
        UserProfile newUserProfile = new UserProfile();
        newUserProfile.setUser(saveUser);
        newUserProfile.setFullName(request.getFullName());
        newUserProfile.setEmail(request.getEmail());
        newUserProfile.setPhoneNumber(request.getPhoneNumber());
        //Lưu
        userProfileRepository.save(newUserProfile);
    }

    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) throw new RuntimeException("Không tìm thấy thiết bị để xóa");
        equipmentRepository.deleteById(id);
    }
}
