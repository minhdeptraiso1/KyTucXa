package com.project.base_v1.repository;

import com.project.base_v1.entity.Registration;
import com.project.base_v1.enums.RegistrationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RegistrationRepository extends JpaRepository<Registration, UUID>, JpaSpecificationExecutor<Registration> {
    List<Registration> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByUserIdAndStatus(UUID userId, RegistrationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Registration r JOIN FETCH r.user WHERE r.id = :id")
    Optional<Registration> findByIdForUpdate(@Param("id") UUID id);
}
