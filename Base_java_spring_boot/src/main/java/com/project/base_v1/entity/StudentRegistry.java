package com.project.base_v1.entity;

import com.project.base_v1.enums.StudentRegistryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "student_registry")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StudentRegistry extends BaseAuditEntity {

    @Id
    UUID id;

    @Column(name = "student_code", nullable = false, unique = true, length = 50)
    String studentCode;

    @Column(name = "full_name", nullable = false, length = 150)
    String fullName;

    @Column(nullable = false)
    String email;

    @Column(nullable = false, length = 20)
    String phone;

    @Column(name = "class_name", nullable = false, length = 100)
    String className;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    StudentRegistryStatus status = StudentRegistryStatus.ACTIVE;
}
