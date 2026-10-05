package com.project.base_v1.repository;

import com.project.base_v1.entity.StudentRegistry;
import com.project.base_v1.enums.StudentRegistryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRegistryRepository extends JpaRepository<StudentRegistry, UUID>, JpaSpecificationExecutor<StudentRegistry> {

    Optional<StudentRegistry> findByStudentCodeIgnoreCase(String studentCode);

    boolean existsByStudentCodeIgnoreCase(String studentCode);

    Optional<StudentRegistry> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByStatus(StudentRegistryStatus status);

    @Query("SELECT s FROM StudentRegistry s WHERE " +
           "LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "s.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(s.className) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<StudentRegistry> searchByKeyword(@Param("keyword") String keyword);
}
