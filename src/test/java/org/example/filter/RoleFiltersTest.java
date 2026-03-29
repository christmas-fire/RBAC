package org.example.filter;

import org.example.model.Permission;
import org.example.model.Role;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleFiltersTest {

    private static Role testRole;
    private static Permission readPerm;
    private static Permission writePerm;

    @BeforeAll
    static void setUp() {
        testRole = new Role("Admin-Role", "A role for testing");
        readPerm = new Permission("READ", "documents", "dsdsdas");
        writePerm = new Permission("WRITE", "documents", "asdasdadas");
        testRole.addPermission(readPerm);
        testRole.addPermission(writePerm);
    }

    @Test
    @DisplayName("Filter byName: should match correct name")
    void testByName() {
        assertTrue(RoleFilters.byName("Admin-Role").test(testRole));
        assertFalse(RoleFilters.byName("Guest-Role").test(testRole));
    }

    @Test
    @DisplayName("Filter byNameContains: should find substring and be case-insensitive")
    void testByNameContains() {
        assertTrue(RoleFilters.byNameContains("admin").test(testRole));
        assertFalse(RoleFilters.byNameContains("guest").test(testRole));
    }

    @Test
    @DisplayName("Filter hasPermission (by object): should find existing permission")
    void testHasPermissionByObject() {
        assertTrue(RoleFilters.hasPermission(readPerm).test(testRole));
        assertFalse(RoleFilters.hasPermission(new Permission("DELETE", "documents", "asdadsa")).test(testRole));
    }

    @Test
    @DisplayName("Filter hasPermission (by name/resource): should find existing permission")
    void testHasPermissionByNameAndResource() {
        assertTrue(RoleFilters.hasPermission("READ", "documents").test(testRole));
        assertFalse(RoleFilters.hasPermission("DELETE", "documents").test(testRole));
        assertFalse(RoleFilters.hasPermission("READ", "images").test(testRole));
    }

    @Test
    @DisplayName("Filter hasAtLeastNPermissions: should correctly count permissions")
    void testHasAtLeastNPermissions() {
        assertTrue(RoleFilters.hasAtLeastNPermissions(1).test(testRole));
        assertTrue(RoleFilters.hasAtLeastNPermissions(2).test(testRole));
        assertFalse(RoleFilters.hasAtLeastNPermissions(3).test(testRole));
    }
}
