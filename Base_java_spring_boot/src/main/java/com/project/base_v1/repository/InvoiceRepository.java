package com.project.base_v1.repository;

import com.project.base_v1.entity.Invoice;
import com.project.base_v1.enums.InvoiceStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {
    boolean existsByInvoiceCode(String code);
    boolean existsByContractIdAndBillingPeriod(UUID contractId, LocalDate billingPeriod);
    @Query("select (count(i) > 0) from Invoice i where i.contract.assignment.bed.room.id=:roomId and i.billingPeriod=:period and i.status <> com.project.base_v1.enums.InvoiceStatus.CANCELLED")
    boolean existsIssuedForRoomAndPeriod(@Param("roomId") UUID roomId, @Param("period") LocalDate period);
    List<Invoice> findByUserIdOrderByBillingPeriodDesc(UUID userId);
    List<Invoice> findByStatusInAndDueDateBefore(List<InvoiceStatus> statuses, LocalDate dueDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i left join fetch i.items where i.id=:id")
    Optional<Invoice> findByIdForUpdate(@Param("id") UUID id);

    @Query(value = "select distinct i from Invoice i left join fetch i.items where i.id=:id")
    Optional<Invoice> findDetailedById(@Param("id") UUID id);
}

