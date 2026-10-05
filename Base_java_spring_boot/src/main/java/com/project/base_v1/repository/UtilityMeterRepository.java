package com.project.base_v1.repository;

import com.project.base_v1.entity.UtilityMeter;
import com.project.base_v1.enums.MeterStatus;
import com.project.base_v1.enums.UtilityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UtilityMeterRepository extends JpaRepository<UtilityMeter, UUID> {
    boolean existsByMeterCodeIgnoreCase(String meterCode);
    boolean existsByRoomIdAndUtilityTypeAndStatus(UUID roomId, UtilityType utilityType, MeterStatus status);
    List<UtilityMeter> findAllByOrderByMeterCodeAsc();
}

