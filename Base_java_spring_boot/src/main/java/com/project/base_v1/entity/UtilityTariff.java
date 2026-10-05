package com.project.base_v1.entity;

import com.project.base_v1.enums.UtilityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "utility_tariffs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class UtilityTariff extends BaseAuditEntity {
    @Id UUID id;
    @Enumerated(EnumType.STRING) @Column(name = "utility_type", nullable = false) UtilityType utilityType;
    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2) BigDecimal unitPrice;
    @Column(name = "effective_from", nullable = false) LocalDate effectiveFrom;
    @Column(name = "effective_to") LocalDate effectiveTo;
    @Builder.Default @Column(nullable = false) boolean active = true;
}

