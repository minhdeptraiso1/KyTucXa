package com.project.base_v1.enums;

import java.util.EnumSet;
import java.util.Set;

public enum UserRole {
    ADMIN(EnumSet.allOf(Permission.class)),
    STAFF(EnumSet.of(
            Permission.USER_READ,
            Permission.USER_MANAGE,
            Permission.PROFILE_READ,
            Permission.FACILITY_READ,
            Permission.FACILITY_MANAGE,
            Permission.REGISTRATION_MANAGE,
            Permission.ASSIGNMENT_MANAGE,
            Permission.CONTRACT_MANAGE,
            Permission.BILLING_MANAGE,
            Permission.OPERATIONS_MANAGE,
            Permission.REPORT_READ
    )),
    USER(EnumSet.of(
            Permission.PROFILE_READ,
            Permission.PROFILE_WRITE,
            Permission.FACILITY_READ
    ));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = Set.copyOf(permissions);
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}
