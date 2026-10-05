package com.project.base_v1.repository;

import com.project.base_v1.entity.RoomAssignment;
import com.project.base_v1.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;
import java.time.LocalDate;

public interface RoomAssignmentRepository extends JpaRepository<RoomAssignment, UUID>, JpaSpecificationExecutor<RoomAssignment> {
    List<RoomAssignment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByUserIdAndStatus(UUID userId, AssignmentStatus status);
    boolean existsByBedIdAndStatus(UUID bedId, AssignmentStatus status);
    boolean existsByRegistrationId(UUID registrationId);
    Optional<RoomAssignment> findByUserIdAndStatus(UUID userId, AssignmentStatus status);

    @Query("SELECT a FROM RoomAssignment a JOIN FETCH a.user JOIN FETCH a.bed b JOIN FETCH b.room r JOIN FETCH r.building WHERE a.id = :id")
    Optional<RoomAssignment> findDetailedById(@Param("id") UUID id);

    @Query("SELECT a FROM RoomAssignment a JOIN FETCH a.user JOIN FETCH a.bed b WHERE b.room.id=:roomId AND a.startDate<=:periodEnd AND (a.endDate IS NULL OR a.endDate>=:periodStart)")
    List<RoomAssignment> findResidentsForPeriod(@Param("roomId") UUID roomId,
                                                @Param("periodStart") LocalDate periodStart,
                                                @Param("periodEnd") LocalDate periodEnd);
}
