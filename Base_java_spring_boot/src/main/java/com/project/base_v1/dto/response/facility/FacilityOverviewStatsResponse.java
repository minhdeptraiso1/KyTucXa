package com.project.base_v1.dto.response.facility;

import lombok.Builder;

@Builder
public record FacilityOverviewStatsResponse(
        long totalBuildings,
        long totalFloors,
        long totalRooms,
        long availableRooms,
        long fullRooms,
        long maintenanceRooms,
        long totalBeds,
        long availableBeds,
        long occupiedBeds,
        long maintenanceBeds,
        double occupancyRate
) {}
