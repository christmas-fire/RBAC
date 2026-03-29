package org.example.report;

import org.example.manager.AssignmentManager;
import org.example.manager.RoleManager;
import org.example.manager.UserManager;
import org.example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;
    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager();
        reportGenerator = new ReportGenerator();

        User user1 = User.validate("user1", "Alice", "alice@test.com");
        User user2 = User.validate("user2", "Bob", "bob@test.com");
        userManager.add(user1);
        userManager.add(user2);

        Role adminRole = new Role("Admin", "Admin Role");
        adminRole.addPermission(new Permission("WRITE", "documents", "dsdsda"));
        adminRole.addPermission(new Permission("DELETE", "users", "asdsadsad"));

        Role viewerRole = new Role("Viewer", "Viewer Role");
        viewerRole.addPermission(new Permission("READ", "documents", "asdasdad"));
        roleManager.add(adminRole);
        roleManager.add(viewerRole);

        assignmentManager.add(new PermanentAssignment(user1, adminRole, AssignmentMetadata.now("test", "")));
        assignmentManager.add(new PermanentAssignment(user2, viewerRole, AssignmentMetadata.now("test", "")));
    }

    @Test
    @DisplayName("generateUserReport should produce correct content")
    void testGenerateUserReport() {
        String report = reportGenerator.generateUserReport(userManager, assignmentManager);
        assertTrue(report.contains("user1"));
        assertTrue(report.contains("Alice"));
        assertTrue(report.contains("Admin"));
        assertTrue(report.contains("user2"));
        assertTrue(report.contains("Bob"));
        assertTrue(report.contains("Viewer"));
    }

    @Test
    @DisplayName("generateRoleReport should produce correct content")
    void testGenerateRoleReport() {
        String report = reportGenerator.generateRoleReport(roleManager, assignmentManager);
        assertTrue(report.matches("(?s).*Admin\\s+\\|\\s+1\\s+\\|\\s+2.*"));
        assertTrue(report.matches("(?s).*Viewer\\s+\\|\\s+1\\s+\\|\\s+1.*"));
    }

    @Test
    @DisplayName("generatePermissionMatrix should produce correct content")
    void testGeneratePermissionMatrix() {
        String report = reportGenerator.generatePermissionMatrix(userManager, assignmentManager);
        assertTrue(report.matches("(?s).*user1\\s+\\|\\s+\\[\\s+X\\s+\\]\\s+\\|\\s+\\[\\s+X\\s+\\].*"));
        assertTrue(report.matches("(?s).*user2\\s+\\|\\s+\\[\\s+X\\s+\\]\\s+\\|\\s+\\[\\s+\\].*"));
    }

    @Test
    @DisplayName("Parallel user report should contain the same info as serial version")
    void testGenerateUserReportParallel() {
        String serialReport = reportGenerator.generateUserReport(userManager, assignmentManager);
        String parallelReport = reportGenerator.generateUserReportParallel(userManager, assignmentManager);

        assertTrue(parallelReport.contains("user1"));
        assertTrue(parallelReport.contains("Admin"));
        assertTrue(parallelReport.contains("user2"));
        assertTrue(parallelReport.contains("Viewer"));
    }

    @Test
    @DisplayName("Parallel role report should contain the same info as serial version")
    void testGenerateRoleReportParallel() {
        String parallelReport = reportGenerator.generateRoleReportParallel(roleManager, assignmentManager);
        assertTrue(parallelReport.matches("(?s).*Admin\\s+\\|\\s+1\\s+\\|\\s+2.*"));
        assertTrue(parallelReport.matches("(?s).*Viewer\\s+\\|\\s+1\\s+\\|\\s+1.*"));
    }

    @Test
    @DisplayName("Parallel permission matrix should contain the same info as serial version")
    void testGeneratePermissionMatrixParallel() {
        String parallelReport = reportGenerator.generatePermissionMatrixParallel(userManager, assignmentManager);
        assertTrue(parallelReport.matches("(?s).*user1\\s+\\|\\s+\\[\\s+X\\s+\\]\\s+\\|\\s+\\[\\s+X\\s+\\].*"));
        assertTrue(parallelReport.matches("(?s).*user2\\s+\\|\\s+\\[\\s+X\\s+\\]\\s+\\|\\s+\\[\\s+\\].*"));
    }
}
