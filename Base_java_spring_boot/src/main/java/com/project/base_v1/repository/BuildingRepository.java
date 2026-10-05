package com.project.base_v1.repository;

import com.project.base_v1.entity.Building;
import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.enums.GenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID>, JpaSpecificationExecutor<Building> {

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    Optional<Building> findByCodeIgnoreCase(String code);

    List<Building> findByStatus(FacilityStatus status);

    List<Building> findByGenderType(GenderType genderType);
}
