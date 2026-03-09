package org.example.command;

import org.example.audit.AuditLog;
import org.example.manager.*;
import org.example.model.*;
import org.example.report.ReportGenerator;

import java.util.*;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager = new UserManager();
    private final RoleManager roleManager = new RoleManager();
    private final AssignmentManager assignmentManager = new AssignmentManager();
    private final AuditLog auditLog = new AuditLog();
    private final ReportGenerator reportGenerator = new ReportGenerator();

    private String currentUser = "system";

    public RBACSystem() {
        roleManager.setAssignmentChecker(role ->
                assignmentManager.findAll().stream().anyMatch(a -> a.role().equals(role) && a.isActive())
        );
        assignmentManager.setExistenceCheckers(
                u -> userManager.exists(u.username()),
                r -> roleManager.exists(r.getName())
        );
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
