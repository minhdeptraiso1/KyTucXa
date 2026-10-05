package com.project.base_v1.entity;

import com.project.base_v1.enums.ContractStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "contracts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Contract extends BaseAuditEntity {
    @Id
    UUID id;

    @Column(name = "contract_code", nullable = false, unique = true, length = 40)
    String contractCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    RoomAssignment assignment;

    @Column(name = "start_date", nullable = false)
    LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    LocalDate endDate;

    @Column(name = "rental_price", nullable = false, precision = 15, scale = 2)
    BigDecimal rentalPrice;

    @Builder.Default
    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal deposit = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ContractStatus status = ContractStatus.DRAFT;

    @Column(name = "terminated_at")
    Instant terminatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminated_by")
    User terminatedBy;

    @Column(name = "termination_reason", columnDefinition = "TEXT")
    String terminationReason;
}
