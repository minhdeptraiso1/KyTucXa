package com.project.base_v1.entity;

import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.enums.GenderType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "buildings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Building extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(nullable = false, length = 20, unique = true)
    String code;

    @Column(nullable = false, length = 200)
    String name;

    @Column(length = 500)
    String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_type", nullable = false, length = 20)
    GenderType genderType;

    @Column(name = "total_floors", nullable = false)
    int totalFloors;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    FacilityStatus status = FacilityStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    String description;
}
