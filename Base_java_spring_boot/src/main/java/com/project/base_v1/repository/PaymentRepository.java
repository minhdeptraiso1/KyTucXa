package com.project.base_v1.repository;

import com.project.base_v1.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsByPaymentCode(String code);
    Optional<Payment> findByPaymentCode(String code);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Payment> findAllByOrderByCreatedAtDesc();
}

