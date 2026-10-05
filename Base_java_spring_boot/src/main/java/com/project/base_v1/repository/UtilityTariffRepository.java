package com.project.base_v1.repository;

import com.project.base_v1.entity.UtilityTariff;
import com.project.base_v1.enums.UtilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UtilityTariffRepository extends JpaRepository<UtilityTariff, UUID> {
    @Query("select t from UtilityTariff t where t.utilityType=:type and t.active=true and t.effectiveFrom<=:date and (t.effectiveTo is null or t.effectiveTo>=:date) order by t.effectiveFrom desc, t.createdAt desc")
    List<UtilityTariff> findEffective(@Param("type") UtilityType type, @Param("date") LocalDate date);
    List<UtilityTariff> findAllByOrderByEffectiveFromDesc();
}

