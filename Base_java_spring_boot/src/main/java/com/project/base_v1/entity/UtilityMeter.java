package com.project.base_v1.entity;

import com.project.base_v1.enums.MeterStatus;
import com.project.base_v1.enums.UtilityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "utility_meters")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public class UtilityMeter extends BaseAuditEntity {
    @Id UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id", nullable = false) Room room;
    @Column(name = "meter_code", nullable = false, unique = true, length = 50) String meterCode;
    @Enumerated(EnumType.STRING) @Column(name = "utility_type", nullable = false) UtilityType utilityType;
    @Column(nullable = false, length = 20) String unit;
    @Builder.Default @Enumerated(EnumType.STRING) @Column(nullable = false) MeterStatus status = MeterStatus.ACTIVE;
}

