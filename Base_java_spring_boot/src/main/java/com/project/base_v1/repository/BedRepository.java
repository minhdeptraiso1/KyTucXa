package com.project.base_v1.repository;

import com.project.base_v1.entity.Bed;
import com.project.base_v1.enums.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;
import java.util.UUID;

public interface BedRepository extends JpaRepository<Bed, UUID>, JpaSpecificationExecutor<Bed> {

    List<Bed> findByRoomIdOrderByBedNumber(UUID roomId);

    boolean existsByRoomIdAndBedNumberIgnoreCase(UUID roomId, String bedNumber);

    boolean existsByRoomIdAndBedNumberIgnoreCaseAndIdNot(UUID roomId, String bedNumber, UUID id);

    List<Bed> findByRoomIdAndStatus(UUID roomId, BedStatus status);

    long countByRoomIdAndStatus(UUID roomId, BedStatus status);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.room.id = :roomId AND b.status = 'AVAILABLE'")
    long countAvailableBedsByRoom(@Param("roomId") UUID roomId);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.room.building.id = :buildingId AND b.status = 'AVAILABLE'")
    long countAvailableBedsByBuilding(@Param("buildingId") UUID buildingId);

    long countByRoomId(UUID roomId);

    long countByStatus(BedStatus status);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.status = 'AVAILABLE'")
    long countAllAvailableBeds();

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.status = 'OCCUPIED'")
    long countAllOccupiedBeds();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bed b JOIN FETCH b.room r JOIN FETCH r.building WHERE b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") UUID id);

    @Query("SELECT b FROM Bed b JOIN FETCH b.room r JOIN FETCH r.building " +
           "WHERE b.status = 'AVAILABLE' AND r.status = 'AVAILABLE' " +
           "AND r.roomType = :roomType " +
           "AND (r.genderType = :genderType OR r.genderType = com.project.base_v1.enums.GenderType.MIXED)")
    List<Bed> findAvailableForRegistration(
            @Param("roomType") com.project.base_v1.enums.RoomType roomType,
            @Param("genderType") com.project.base_v1.enums.GenderType genderType);
}
