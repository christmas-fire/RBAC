package org.example.manager;

import org.example.filter.RoleFilter;
import org.example.model.Permission;
import org.example.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {
    private RoleManager roleManager;
    private Role adminRole;
    private Permission readPermission;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
        adminRole = new Role("Administrator", "Full system access");
        readPermission = new Permission("READ", "USERS", "View users");
    }

    @Test
    @DisplayName("Добавление роли: успешная индексация по ID и имени")
    void testAddRoleSuccess() {
        roleManager.add(adminRole);

        assertTrue(roleManager.findById(adminRole.getId()).isPresent());
        assertTrue(roleManager.findByName("Administrator").isPresent());
        assertEquals(1, roleManager.count());
    }

    @Test
    @DisplayName("Уникальность имени: нельзя добавить роль с существующим названием")
    void testUniqueNameConstraint() {
        roleManager.add(adminRole);
        Role duplicateNameRole = new Role("Administrator", "Another description");

        assertThrows(IllegalArgumentException.class, () ->
                roleManager.add(duplicateNameRole)
        );
    }

    @Test
    @DisplayName("Удаление роли: успех, если роль не назначена")
    void testRemoveRoleSuccess() {
        roleManager.add(adminRole);
        roleManager.setAssignmentChecker(role -> false);

        boolean removed = roleManager.remove(adminRole);
        assertTrue(removed);
        assertFalse(roleManager.exists("Administrator"));
    }

    @Test
    @DisplayName("Удаление роли: ошибка IllegalStateException, если роль назначена")
    void testRemoveRoleFailureWhenAssigned() {
        roleManager.add(adminRole);
        roleManager.setAssignmentChecker(role -> true);

        Exception exception = assertThrows(IllegalStateException.class, () ->
                roleManager.remove(adminRole)
        );
        assertTrue(exception.getMessage().contains("назначена пользователям"));
    }

    @Test
    @DisplayName("Работа с правами: добавление и удаление прав через менеджера")
    void testRolePermissionsManagement() {
        roleManager.add(adminRole);

        roleManager.addPermissionToRole("Administrator", readPermission);
        assertTrue(adminRole.hasPermission(readPermission));

        roleManager.removePermissionFromRole("Administrator", readPermission);
        assertFalse(adminRole.hasPermission(readPermission));
    }

    @Test
    @DisplayName("Поиск ролей по конкретному праву")
    void testFindRolesWithPermission() {
        Role managerRole = new Role("Manager", "Resource manager");
        roleManager.add(adminRole);
        roleManager.add(managerRole);

        roleManager.addPermissionToRole("Administrator", readPermission);
        roleManager.addPermissionToRole("Manager", readPermission);

        List<Role> roles = roleManager.findRolesWithPermission("READ", "USERS");

        assertEquals(2, roles.size());
        assertTrue(roles.contains(adminRole));
        assertTrue(roles.contains(managerRole));
    }

    @Test
    @DisplayName("Фильтрация: поиск ролей по количеству прав")
    void testRoleFilteringByPermissionCount() {
        roleManager.add(adminRole);
        roleManager.add(new Role("Guest", "No perms")); // 0 прав

        roleManager.addPermissionToRole("Administrator", readPermission);

        RoleFilter atLeastOne = r -> !r.getPermissions().isEmpty();
        List<Role> result = roleManager.findByFilter(atLeastOne);

        assertEquals(1, result.size());
        assertEquals("Administrator", result.getFirst().getName());
    }

    @Test
    @DisplayName("Синхронизация Map: метод clear очищает оба хранилища")
    void testClearSync() {
        roleManager.add(adminRole);
        roleManager.clear();

        assertEquals(0, roleManager.count());
        assertFalse(roleManager.findByName("Administrator").isPresent());
    }
}
