package org.example.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {
    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    @DisplayName("Инициализация: проверка создания начальных данных")
    void testInitialize() {
        system.initialize();

        assertTrue(system.getUserManager().exists("admin"));
        assertTrue(system.getRoleManager().exists("Admin"));
        assertTrue(system.getRoleManager().exists("Viewer"));
        assertFalse(system.getAssignmentManager().findAll().isEmpty());
    }

    @Test
    @DisplayName("Статистика: проверка формирования строки статистики")
    void testGenerateStatistics() {
        system.initialize();
        String stats = system.generateStatistics();

        assertNotNull(stats);
        assertTrue(stats.contains("Users: 1"));
        assertTrue(stats.contains("Roles: 2"));
        assertTrue(stats.contains("Admin (1)"));
    }
}
