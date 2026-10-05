package com.project.base_v1.entity;

import com.project.base_v1.enums.BedStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "beds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bed extends BaseAuditEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    Room room;

    @Column(name = "bed_number", nullable = false, length = 10)
    String bedNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    BedStatus status = BedStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    String notes;

    public boolean isUsable() {
        return status == BedStatus.AVAILABLE;
    }
}
