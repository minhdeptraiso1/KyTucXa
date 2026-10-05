package com.project.base_v1.repository;

import com.project.base_v1.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByStudentCode(String studentCode);
    boolean existsByStudentCode(String studentCode);
}
