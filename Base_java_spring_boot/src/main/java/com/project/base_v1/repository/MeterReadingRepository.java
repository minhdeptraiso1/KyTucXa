package com.project.base_v1.repository;

import com.project.base_v1.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeterReadingRepository extends JpaRepository<MeterReading, UUID> {
    boolean existsByMeterIdAndBillingPeriod(UUID meterId, LocalDate billingPeriod);
    Optional<MeterReading> findFirstByMeterIdOrderByBillingPeriodDesc(UUID meterId);
    Optional<MeterReading> findFirstByMeterIdAndBillingPeriodBeforeOrderByBillingPeriodDesc(UUID meterId, LocalDate billingPeriod);
    Optional<MeterReading> findByMeterRoomIdAndMeterUtilityTypeAndBillingPeriod(UUID roomId, com.project.base_v1.enums.UtilityType type, LocalDate billingPeriod);
    List<MeterReading> findAllByOrderByBillingPeriodDesc();
}

