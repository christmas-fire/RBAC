package org.example.filter;

import org.example.model.*;
import org.example.util.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentFiltersTest {

    private static User testUser;
    private static Role testRole;
    private static AssignmentMetadata testMeta;

    private static RoleAssignment permanentActive;
    private static RoleAssignment permanentInactive;
    private static RoleAssignment temporaryActive;
    private static RoleAssignment temporaryExpired;

    @BeforeAll
    static void setUp() {
        testUser = User.validate("test_user", "Test User", "test@example.com");
        User anotherUser = User.validate("another_user", "Another User", "another@example.com");
        testRole = new Role("Test-Role", "A role for assignments");
        Role anotherRole = new Role("Another-Role", "...");

        testMeta = new AssignmentMetadata("2023-01-01T10:00:00", "admin", "Initial setup");
        AssignmentMetadata anotherMeta = new AssignmentMetadata("2024-01-01T10:00:00", "supervisor", "Review");

        String futureDate = DateUtils.offsetCurrentDateTime(1, ChronoUnit.DAYS);
        String pastDate = DateUtils.offsetCurrentDateTime(-1, ChronoUnit.DAYS);

        permanentActive = new PermanentAssignment(testUser, testRole, testMeta);

        PermanentAssignment paInactive = new PermanentAssignment(anotherUser, testRole, testMeta);
        paInactive.revoke();
        permanentInactive = paInactive;

        temporaryActive = new TemporaryAssignment(testUser, anotherRole, anotherMeta, futureDate, false);
        temporaryExpired = new TemporaryAssignment(anotherUser, anotherRole, anotherMeta, pastDate, false);
    }

    @Test
    @DisplayName("Filter byUser and byUsername")
    void testByUserAndUsername() {
        assertTrue(AssignmentFilters.byUser(testUser).test(permanentActive));
        assertFalse(AssignmentFilters.byUser(new User("u", "n", "e")).test(permanentActive));
        assertTrue(AssignmentFilters.byUsername("test_user").test(temporaryActive));
        assertFalse(AssignmentFilters.byUsername("another_user").test(permanentActive));
    }

    @Test
    @DisplayName("Filter byRole and byRoleName")
    void testByRoleAndRoleName() {
        assertTrue(AssignmentFilters.byRole(testRole).test(permanentActive));
        assertFalse(AssignmentFilters.byRole(new Role("r", "d")).test(permanentActive));
        assertTrue(AssignmentFilters.byRoleName("Test-Role").test(permanentActive));
        assertFalse(AssignmentFilters.byRoleName("Another-Role").test(permanentActive));
    }

    @Test
    @DisplayName("Filter activeOnly")
    void testActiveOnly() {
        assertTrue(AssignmentFilters.activeOnly().test(permanentActive));
        assertTrue(AssignmentFilters.activeOnly().test(temporaryActive));
        assertFalse(AssignmentFilters.activeOnly().test(permanentInactive));
        assertFalse(AssignmentFilters.activeOnly().test(temporaryExpired));
    }

    @Test
    @DisplayName("Filter inactiveOnly")
    void testInactiveOnly() {
        assertFalse(AssignmentFilters.inactiveOnly().test(permanentActive));
        assertFalse(AssignmentFilters.inactiveOnly().test(temporaryActive));
        assertTrue(AssignmentFilters.inactiveOnly().test(permanentInactive));
        assertTrue(AssignmentFilters.inactiveOnly().test(temporaryExpired));
    }

    @Test
    @DisplayName("Filter byType")
    void testByType() {
        assertTrue(AssignmentFilters.byType("PERMANENT").test(permanentActive));
        assertTrue(AssignmentFilters.byType("permanent").test(permanentActive), "Should be case-insensitive");
        assertFalse(AssignmentFilters.byType("TEMPORARY").test(permanentActive));

        assertTrue(AssignmentFilters.byType("TEMPORARY").test(temporaryActive));
        assertFalse(AssignmentFilters.byType("PERMANENT").test(temporaryActive));
    }

    @Test
    @DisplayName("Filter expiringBefore")
    void testExpiringBefore() {
        String farFutureDate = DateUtils.offsetCurrentDateTime(2, ChronoUnit.DAYS);
        String nearFutureDate = DateUtils.offsetCurrentDateTime(0, ChronoUnit.SECONDS);

        assertTrue(AssignmentFilters.expiringBefore(nearFutureDate).test(temporaryExpired));
        assertFalse(AssignmentFilters.expiringBefore(farFutureDate).test(permanentActive));
        assertFalse(AssignmentFilters.expiringBefore(nearFutureDate).test(temporaryActive));
        assertTrue(AssignmentFilters.expiringBefore(farFutureDate).test(temporaryActive));
    }
}
