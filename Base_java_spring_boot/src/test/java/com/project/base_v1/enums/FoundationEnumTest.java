package com.project.base_v1.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FoundationEnumTest {

    @Test
    void foundationContainsThreeBusinessRoles() {
        assertTrue(java.util.Set.of(UserRole.values()).containsAll(
                java.util.Set.of(UserRole.ADMIN, UserRole.STAFF, UserRole.USER)));
    }

    @Test
    void staffHasOperationalPermissionsButUserDoesNot() {
        assertTrue(UserRole.STAFF.permissions().contains(Permission.FACILITY_MANAGE));
        assertTrue(UserRole.STAFF.permissions().contains(Permission.ASSIGNMENT_MANAGE));
        assertTrue(!UserRole.USER.permissions().contains(Permission.FACILITY_MANAGE));
    }

    @Test
    void accountStatusesMatchPhaseOneContract() {
        assertTrue(java.util.Set.of(AccountStatus.values()).containsAll(
                java.util.Set.of(AccountStatus.PENDING, AccountStatus.ACTIVE,
                        AccountStatus.LOCKED, AccountStatus.INACTIVE)));
    }
}
