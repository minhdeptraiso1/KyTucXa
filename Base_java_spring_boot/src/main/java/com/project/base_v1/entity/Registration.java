package com.project.base_v1.entity;

import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RegistrationStatus;
import com.project.base_v1.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "registrations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Registration extends BaseAuditEntity {
    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_room_type", nullable = false)
    RoomType requestedRoomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "requested_gender_type", nullable = false)
    GenderType requestedGenderType;

    @Column(name = "preferred_start_date", nullable = false)
    LocalDate preferredStartDate;

    @Column(name = "preferred_end_date")
    LocalDate preferredEndDate;

    @Column(columnDefinition = "TEXT")
    String reason;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    User reviewedBy;

    @Column(name = "reviewed_at")
    Instant reviewedAt;
}
