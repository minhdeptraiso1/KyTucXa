package com.project.base_v1.entity;

import com.project.base_v1.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "price_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricePolicy extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(nullable = false, length = 200)
    String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    RoomType roomType;

    @Column(name = "price_per_month", nullable = false, precision = 15, scale = 2)
    BigDecimal pricePerMonth;

    @Column(name = "effective_from", nullable = false)
    LocalDate effectiveFrom;

    @Column(name = "effective_to")
    LocalDate effectiveTo;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    String description;
}
