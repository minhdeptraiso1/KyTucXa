package com.project.base_v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfile extends BaseAuditEntity {

    @Id
    UUID userId;

    @Column(nullable = false, length = 150)
    String fullName;

    @Column(unique = true, length = 50)
    String studentCode;

    LocalDate dateOfBirth;

    @Column(length = 20)
    String gender;

    @Column(length = 20)
    String phone;

    @Column(length = 50)
    String identityNumber;

    @Column(length = 150)
    String faculty;

    @Column(length = 100)
    String className;

    @Column(length = 500)
    String address;

    @Column(length = 150)
    String emergencyContactName;

    @Column(length = 20)
    String emergencyContactPhone;
}
