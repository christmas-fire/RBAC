package org.example.manager;

import org.example.model.PermanentAssignment;
import org.example.model.RoleAssignment;
import org.example.model.TemporaryAssignment;
import org.example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {
    private AssignmentManager assignmentManager;
    private User testUser;
    private Role adminRole;
    private Role viewerRole;
    private AssignmentMetadata meta;

    @BeforeEach
    void setUp() {
        assignmentManager = new AssignmentManager();
        testUser = User.validate("alice_88", "Alice Smith", "alice@example.com");
        adminRole = new Role("Admin", "Full access");
        viewerRole = new Role("Viewer", "Read only");
        meta = AssignmentMetadata.now("system", "Test setup");

        assignmentManager.setExistenceCheckers(user -> true, role -> true);
    }

    @Test
    @DisplayName("Добавление назначения: успешный сценарий")
    void testAddAssignmentSuccess() {
        RoleAssignment pa = new PermanentAssignment(testUser, adminRole, meta);
        assignmentManager.add(pa);

        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.userHasRole(testUser, adminRole));
    }

    @Test
    @DisplayName("Запрет дубликатов: нельзя назначить ту же роль пользователю дважды одновременно")
    void testDuplicateActiveAssignmentThrowsException() {
        RoleAssignment pa1 = new PermanentAssignment(testUser, adminRole, meta);
        assignmentManager.add(pa1);

        RoleAssignment pa2 = new PermanentAssignment(testUser, adminRole, meta);

        assertThrows(IllegalStateException.class, () ->
                assignmentManager.add(pa2)
        );
    }

    @Test
    @DisplayName("Проверка существования: ошибка если пользователь или роль не найдены в системе")
    void testAddAssignmentExistenceCheck() {
        assignmentManager.setExistenceCheckers(user -> false, role -> true);
        RoleAssignment pa = new PermanentAssignment(testUser, adminRole, meta);

        assertThrows(IllegalArgumentException.class, () ->
                assignmentManager.add(pa)
        );
    }

    @Test
    @DisplayName("Агрегация прав: сбор всех уникальных прав из нескольких активных ролей")
    void testGetUserPermissionsAggregation() {
        Permission read = new Permission("READ", "USERS", "View");
        Permission write = new Permission("WRITE", "USERS", "Edit");

        adminRole.addPermission(read);
        adminRole.addPermission(write);
        viewerRole.addPermission(read);

        assignmentManager.add(new PermanentAssignment(testUser, adminRole, meta));
        assignmentManager.add(new PermanentAssignment(testUser, viewerRole, meta));

        Set<Permission> allPerms = assignmentManager.getUserPermissions(testUser);

        assertEquals(2, allPerms.size());
        assertTrue(assignmentManager.userHasPermission(testUser, "WRITE", "USERS"));
    }

    @Test
    @DisplayName("Отзыв назначения: постоянная роль становится неактивной")
    void testRevokePermanentAssignment() {
        PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);
        assignmentManager.add(pa);

        assignmentManager.revokeAssignment(pa.assignmentId());

        assertFalse(pa.isActive());
        assertFalse(assignmentManager.userHasRole(testUser, adminRole));
    }

    @Test
    @DisplayName("Продление временного назначения: изменение даты истечения")
    void testExtendTemporaryAssignment() {
        String initialExpiry = "2025-01-01 12:00";
        String newExpiry = "2026-01-01 12:00";
        TemporaryAssignment ta = new TemporaryAssignment(testUser, viewerRole, meta, initialExpiry, false);
        assignmentManager.add(ta);

        assignmentManager.extendTemporaryAssignment(ta.assignmentId(), newExpiry);

        assertEquals(newExpiry, ta.getExpiresAt());
    }

    @Test
    @DisplayName("Безопасность типов: нельзя отозвать временное назначение методом revoke")
    void testRevokeOnTemporaryThrowsException() {
        TemporaryAssignment ta = new TemporaryAssignment(testUser, viewerRole, meta, "2099-01-01 00:00", false);
        assignmentManager.add(ta);

        assertThrows(UnsupportedOperationException.class, () ->
                assignmentManager.revokeAssignment(ta.assignmentId())
        );
    }

    @Test
    @DisplayName("Фильтрация активных назначений")
    void testGetActiveAssignments() {
        PermanentAssignment pa = new PermanentAssignment(testUser, adminRole, meta);
        PermanentAssignment revokedPa = new PermanentAssignment(testUser, viewerRole, meta);

        assignmentManager.add(pa);
        assignmentManager.add(revokedPa);
        assignmentManager.revokeAssignment(revokedPa.assignmentId());

        List<RoleAssignment> active = assignmentManager.getActiveAssignments();

        assertEquals(1, active.size());
        assertEquals("Admin", active.getFirst().role().getName());
    }
}
