package com.project.base_v1.repository;

import com.project.base_v1.entity.Room;
import com.project.base_v1.enums.GenderType;
import com.project.base_v1.enums.RoomStatus;
import com.project.base_v1.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID>, JpaSpecificationExecutor<Room> {

    List<Room> findByFloorId(UUID floorId);

    List<Room> findByBuildingId(UUID buildingId);

    boolean existsByFloorIdAndRoomNumberIgnoreCase(UUID floorId, String roomNumber);

    boolean existsByFloorIdAndRoomNumberIgnoreCaseAndIdNot(UUID floorId, String roomNumber, UUID id);

    List<Room> findByBuildingIdAndStatus(UUID buildingId, RoomStatus status);

    List<Room> findByStatusAndGenderType(RoomStatus status, GenderType genderType);

    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.currentOccupancy < r.capacity")
    List<Room> findAvailableRooms();

    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' " +
           "AND r.genderType IN (:genderTypes) AND r.roomType = :roomType")
    List<Room> findAvailableRoomsByGenderAndType(
            @Param("genderTypes") List<GenderType> genderTypes,
            @Param("roomType") RoomType roomType);

    long countByFloorId(UUID floorId);

    long countByBuildingId(UUID buildingId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.building.id = :buildingId AND r.status = :status")
    long countByBuildingIdAndStatus(@Param("buildingId") UUID buildingId, @Param("status") RoomStatus status);

    @Query("SELECT SUM(r.capacity - r.currentOccupancy) FROM Room r WHERE r.status = 'AVAILABLE'")
    Long countTotalAvailableBeds();
}
