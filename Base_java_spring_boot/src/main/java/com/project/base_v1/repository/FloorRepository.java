package com.project.base_v1.repository;

import com.project.base_v1.entity.Floor;
import com.project.base_v1.enums.FacilityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface FloorRepository extends JpaRepository<Floor, UUID>, JpaSpecificationExecutor<Floor> {

    List<Floor> findByBuildingIdOrderByFloorNumber(UUID buildingId);

    boolean existsByBuildingIdAndFloorNumber(UUID buildingId, int floorNumber);

    boolean existsByBuildingIdAndFloorNumberAndIdNot(UUID buildingId, int floorNumber, UUID id);

    List<Floor> findByBuildingIdAndStatus(UUID buildingId, FacilityStatus status);

    long countByBuildingId(UUID buildingId);
}
