package com.project.base_v1.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "meter_readings")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class MeterReading extends BaseAuditEntity {
    @Id UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "meter_id", nullable = false) UtilityMeter meter;
    @Column(name = "billing_period", nullable = false) LocalDate billingPeriod;
    @Column(name = "previous_value", nullable = false, precision = 15, scale = 3) BigDecimal previousValue;
    @Column(name = "current_value", nullable = false, precision = 15, scale = 3) BigDecimal currentValue;
    @Column(nullable = false, precision = 15, scale = 3) BigDecimal consumption;
    @Builder.Default @Column(name = "reset_recorded", nullable = false) boolean resetRecorded = false;
    @Column(columnDefinition = "TEXT") String note;
    @Column(name = "read_at", nullable = false) Instant readAt;
}

