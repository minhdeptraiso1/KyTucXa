package com.project.base_v1.service;

import com.project.base_v1.dto.request.facility.CreateFloorRequest;
import com.project.base_v1.dto.request.facility.UpdateFloorRequest;
import com.project.base_v1.dto.response.facility.FloorResponse;

import java.util.List;
import java.util.UUID;

public interface FloorService {

    FloorResponse createFloor(UUID buildingId, CreateFloorRequest request);

    FloorResponse updateFloor(UUID id, UpdateFloorRequest request);

    FloorResponse getFloorById(UUID id);

    List<FloorResponse> getFloorsByBuilding(UUID buildingId);

    void deleteFloor(UUID id);
}
