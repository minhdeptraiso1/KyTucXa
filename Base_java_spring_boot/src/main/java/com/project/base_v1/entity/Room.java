package com.project.base_v1.entity;

import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.enums.RoomType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Room extends BaseAuditEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    Floor floor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id", nullable = false)
    Building building;

    @Column(name = "room_number", nullable = false, length = 20)
    String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    RoomType roomType;

    @Column(nullable = false)
    int capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_type", nullable = false, length = 20)
    GenderType genderType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RoomStatus status = RoomStatus.AVAILABLE;

    @Builder.Default
    @Column(name = "current_occupancy", nullable = false)
    int currentOccupancy = 0;

    @Column(name = "area_sqm", precision = 6, scale = 2)
    BigDecimal areaSqm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_policy_id")
    PricePolicy pricePolicy;

    @Column(name = "image_url", length = 1000)
    String imageUrl;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(columnDefinition = "TEXT")
    String notes;

    /**
     * Tính số giường còn trống = capacity - currentOccupancy
     */
    public int getAvailableBeds() {
        return capacity - currentOccupancy;
    }

    /**
     * Cập nhật trạng thái phòng dựa trên currentOccupancy và capacity.
     * Chỉ cập nhật khi phòng đang ở trạng thái vận hành bình thường.
     */
    public void recalculateStatus() {
        if (status == RoomStatus.MAINTENANCE || status == RoomStatus.INACTIVE) {
            return; // Giữ nguyên trạng thái đặc biệt
        }
        if (currentOccupancy >= capacity) {
            this.status = RoomStatus.FULL;
        } else {
            this.status = RoomStatus.AVAILABLE;
        }
    }
}
