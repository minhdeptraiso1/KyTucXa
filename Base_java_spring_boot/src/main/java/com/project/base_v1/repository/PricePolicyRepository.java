package com.project.base_v1.repository;

import com.project.base_v1.entity.PricePolicy;
import com.project.base_v1.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricePolicyRepository extends JpaRepository<PricePolicy, UUID>, JpaSpecificationExecutor<PricePolicy> {

    List<PricePolicy> findByRoomType(RoomType roomType);

    List<PricePolicy> findByIsActiveTrue();

    @Query("SELECT p FROM PricePolicy p WHERE p.roomType = :roomType AND p.isActive = true " +
           "AND p.effectiveFrom <= :date AND (p.effectiveTo IS NULL OR p.effectiveTo >= :date) " +
           "ORDER BY p.effectiveFrom DESC")
    Optional<PricePolicy> findActiveByRoomTypeAndDate(
            @Param("roomType") RoomType roomType,
            @Param("date") LocalDate date);
}
