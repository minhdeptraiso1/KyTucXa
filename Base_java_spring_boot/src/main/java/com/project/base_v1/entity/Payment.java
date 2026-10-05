package com.project.base_v1.entity;

import com.project.base_v1.enums.PaymentMethod;
import com.project.base_v1.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class Payment extends BaseAuditEntity {
    @Id UUID id;
    @Column(name = "payment_code", nullable = false, unique = true, length = 40) String paymentCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "invoice_id", nullable = false) Invoice invoice;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) User user;
    @Column(nullable = false, precision = 15, scale = 2) BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false) PaymentMethod method;
    @Enumerated(EnumType.STRING) @Column(nullable = false) PaymentStatus status;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100) String idempotencyKey;
    @Column(name = "external_reference", length = 100) String externalReference;
    @Column(name = "paid_at") Instant paidAt;
    @Column(name = "failure_reason", columnDefinition = "TEXT") String failureReason;
}

