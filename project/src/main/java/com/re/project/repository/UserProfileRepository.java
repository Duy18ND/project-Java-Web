package com.re.project.repository;

import com.re.project.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    //Check trùng Email
    boolean existsByEmail(String email);
    //Check trùng phone
    boolean existsByPhoneNumber(String phoneNumber);
}
