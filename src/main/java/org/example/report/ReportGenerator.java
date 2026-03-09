package org.example.report;

import org.example.manager.*;
import org.example.model.*;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class ReportGenerator {

    // 1. Отчёт по всем пользователям с их ролями
    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ (%d чел.) ===\n", userManager.count()));
        sb.append(String.format("%-15s | %-25s | %-30s\n", "Username", "Full Name", "Active Roles"));
        sb.append("-".repeat(75)).append("\n");

        for (User user : userManager.findAll()) {
            String roles = assignmentManager.findByUser(user).stream()
                    .filter(RoleAssignment::isActive)
                    .map(a -> a.role().getName())
                    .collect(Collectors.joining(", "));

            sb.append(String.format("%-15s | %-25s | %-30s\n",
                    user.username(), user.fullName(), roles.isEmpty() ? "No roles" : roles));
        }
        return sb.toString();
    }

    // 2. Отчёт по ролям с количеством пользователей
    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== ОТЧЁТ ПО РОЛЯМ (%d шт.) ===\n", roleManager.count()));
        sb.append(String.format("%-20s | %-10s | %-15s\n", "Role Name", "Users", "Permissions Count"));
        sb.append("-".repeat(50)).append("\n");

        for (Role role : roleManager.findAll()) {
            long userCount = assignmentManager.findAll().stream()
                    .filter(a -> a.role().equals(role) && a.isActive())
                    .count();

            sb.append(String.format("%-20s | %-10d | %-15d\n",
                    role.getName(), userCount, role.getPermissions().size()));
        }
        return sb.toString();
    }

    // 3. Матрица прав (пользователи × ресурсы)
    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        // Находим все уникальные ресурсы в системе
        Set<String> allResources = new TreeSet<>();
        assignmentManager.findAll().forEach(a ->
                a.role().getPermissions().forEach(p -> allResources.add(p.resource())));

        StringBuilder sb = new StringBuilder();
        sb.append("=== МАТРИЦА ПРАВ (User x Resource) ===\n\n");

        // Заголовок (ресурсы)
        sb.append(String.format("%-15s", "User \\ Res"));
        for (String res : allResources) {
            sb.append(String.format(" | %-12s", res));
        }
        sb.append("\n").append("-".repeat(15 + allResources.size() * 15)).append("\n");

        // Строки (пользователи)
        for (User user : userManager.findAll()) {
            sb.append(String.format("%-15s", user.username()));
            Set<Permission> userPerms = assignmentManager.getUserPermissions(user);

            for (String res : allResources) {
                boolean hasAccess = userPerms.stream().anyMatch(p -> p.resource().equalsIgnoreCase(res));
                sb.append(String.format(" | %-12s", hasAccess ? "[  X  ]" : "[     ]"));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // 4. Сохранение отчёта в файл
    public void exportToFile(String report, String filename) {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.print(report);
            System.out.println("Файл успешно сохранен: " + filename);
        } catch (Exception e) {
            System.out.println("Ошибка записи в файл: " + e.getMessage());
        }
    }
}
