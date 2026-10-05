package com.project.base_v1.service.impl;

import com.project.base_v1.dto.response.facility.FacilityOverviewStatsResponse;
import com.project.base_v1.enums.BedStatus;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.repository.BedRepository;
import com.project.base_v1.repository.BuildingRepository;
import com.project.base_v1.repository.FloorRepository;
import com.project.base_v1.repository.RoomRepository;
import com.project.base_v1.service.FacilityStatsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FacilityStatsServiceImpl implements FacilityStatsService {

    BuildingRepository buildingRepository;
    FloorRepository floorRepository;
    RoomRepository roomRepository;
    BedRepository bedRepository;

    @Override
    @Transactional(readOnly = true)
    public FacilityOverviewStatsResponse getOverviewStats() {
        long totalBuildings = buildingRepository.count();
        long totalFloors = floorRepository.count();
        long totalRooms = roomRepository.count();

        long availableRooms = roomRepository.findAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.AVAILABLE)
                .count();

        long fullRooms = roomRepository.findAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.FULL)
                .count();

        long maintenanceRooms = roomRepository.findAll().stream()
                .filter(r -> r.getStatus() == RoomStatus.MAINTENANCE || r.getStatus() == RoomStatus.INACTIVE)
                .count();

        long totalBeds = bedRepository.count();
        long availableBeds = bedRepository.countAllAvailableBeds();
        long occupiedBeds = bedRepository.countAllOccupiedBeds();
        long maintenanceBeds = bedRepository.countByStatus(BedStatus.MAINTENANCE);

        double occupancyRate = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100 : 0.0;
        occupancyRate = Math.round(occupancyRate * 10.0) / 10.0;

        return FacilityOverviewStatsResponse.builder()
                .totalBuildings(totalBuildings)
                .totalFloors(totalFloors)
                .totalRooms(totalRooms)
                .availableRooms(availableRooms)
                .fullRooms(fullRooms)
                .maintenanceRooms(maintenanceRooms)
                .totalBeds(totalBeds)
                .availableBeds(availableBeds)
                .occupiedBeds(occupiedBeds)
                .maintenanceBeds(maintenanceBeds)
                .occupancyRate(occupancyRate)
                .build();
    }
}
