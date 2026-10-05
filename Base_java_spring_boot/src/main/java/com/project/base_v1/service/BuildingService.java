package com.project.base_v1.service;

import com.project.base_v1.dto.request.facility.CreateBuildingRequest;
import com.project.base_v1.dto.request.facility.UpdateBuildingRequest;
import com.project.base_v1.dto.response.facility.BuildingResponse;
import com.project.base_v1.enums.FacilityStatus;
import com.project.base_v1.enums.GenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BuildingService {

    BuildingResponse createBuilding(CreateBuildingRequest request);

    BuildingResponse updateBuilding(UUID id, UpdateBuildingRequest request);

    BuildingResponse getBuildingById(UUID id);

    Page<BuildingResponse> getBuildings(String search, FacilityStatus status, GenderType genderType, Pageable pageable);

    List<BuildingResponse> getAllActiveBuildings();

    void deleteBuilding(UUID id);
}
