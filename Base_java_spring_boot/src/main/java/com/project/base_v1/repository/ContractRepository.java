package com.project.base_v1.repository;

import com.project.base_v1.entity.Contract;
import com.project.base_v1.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.*;

public interface ContractRepository extends JpaRepository<Contract, UUID>, JpaSpecificationExecutor<Contract> {
    List<Contract> findByUserIdOrderByCreatedAtDesc(UUID userId);
    boolean existsByUserIdAndStatus(UUID userId, ContractStatus status);
    boolean existsByContractCode(String contractCode);
    Optional<Contract> findByAssignmentId(UUID assignmentId);
    boolean existsByAssignmentIdAndStatus(UUID assignmentId, ContractStatus status);
    List<Contract> findByStatusAndEndDateBefore(ContractStatus status, java.time.LocalDate date);
}
