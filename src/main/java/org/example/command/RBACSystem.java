package org.example.command;

import org.example.audit.AuditLog;
import org.example.manager.*;
import org.example.model.*;
import org.example.report.ReportGenerator;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RBACSystem implements Serializable {
    private final UserManager userManager = new UserManager();
    private final RoleManager roleManager = new RoleManager();
    private final AssignmentManager assignmentManager = new AssignmentManager();
    private final transient AuditLog auditLog = new AuditLog();
    private final transient ReportGenerator reportGenerator = new ReportGenerator();

    private final transient ExecutorService executor = Executors.newCachedThreadPool();

    private String currentUser = "system";

    public RBACSystem() {
        connectManagers();
    }

    private void connectManagers() {
        roleManager.setAssignmentChecker(role ->
                assignmentManager.findAll().stream().anyMatch(a -> a.role().equals(role) && a.isActive())
        );
        assignmentManager.setExistenceCheckers(
                u -> userManager.exists(u.username()),
                r -> roleManager.exists(r.getName())
        );
    }

    public void shutdown() {
        System.out.println("Shutting down background services...");
        executor.shutdown();
        auditLog.stop();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        System.out.println("Shutdown complete.");
    }

    public void reportUsersAsync() {
        System.out.println("Запуск генерации отчёта в фоновом режиме...");
        executor.submit(() -> {
            String report = reportGenerator.generateUserReportParallel(userManager, assignmentManager);
            System.out.println("\n--- АСИНХРОННЫЙ ОТЧЁТ ГОТОВ ---\n" + report + "\n---------------------------------\n");
        });
    }

    public void saveStateAsync(String filename) {
        System.out.println("Запуск сохранения состояния в фоновом режиме...");
        executor.submit(() -> {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
                oos.writeObject(this);
                System.out.println("Состояние системы успешно сохранено в " + filename);
            } catch (Exception e) {
                System.err.println("Ошибка при сохранении состояния: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void initialize() {
        Permission readUsers = new Permission("READ", "users", "View users list");
        Permission writeUsers = new Permission("WRITE", "users", "Create/Edit users");
        Permission deleteUsers = new Permission("DELETE", "users", "Delete users");

        Role admin = new Role("Admin", "Full system access");
        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        admin.addPermission(deleteUsers);

        Role viewer = new Role("Viewer", "Read-only access");
        viewer.addPermission(readUsers);

        roleManager.add(admin);
        roleManager.add(viewer);

        User root = User.validate("admin", "System Root", "admin@rbac.local");
        userManager.add(root);

        assignmentManager.add(new PermanentAssignment(root, admin,
                AssignmentMetadata.now("system", "Initial admin setup")));
    }

    public String generateStatistics() {
        long totalUsers = userManager.count();
        long totalRoles = roleManager.count();
        List<RoleAssignment> allAssign = assignmentManager.findAll();
        long active = allAssign.stream().filter(RoleAssignment::isActive).count();
        long expired = allAssign.stream().filter(a -> !a.isActive()).count();

        String topRoles = allAssign.stream()
                .collect(Collectors.groupingBy(a -> a.role().getName(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));

        return String.format(
                "--- SYSTEM STATISTICS ---\n" +
                        "Users: %d | Roles: %d\n" +
                        "Assignments: %d (Active: %d, Inactive/Expired: %d)\n" +
                        "Top Roles: %s\n" +
                        "-------------------------",
                totalUsers, totalRoles, allAssign.size(), active, expired, topRoles.isEmpty() ? "None" : topRoles);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String user) {
        this.currentUser = user;
    }

    public AuditLog getAuditLog() { return auditLog; }

    public ReportGenerator getReportGenerator() { return reportGenerator; }
}
