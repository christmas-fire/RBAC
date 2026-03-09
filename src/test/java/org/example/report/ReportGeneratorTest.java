package org.example.report;

import org.example.manager.AssignmentManager;
import org.example.manager.UserManager;
import org.example.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReportGeneratorTest {
    @Test
    void testUserReport() {
        UserManager um = new UserManager();
        AssignmentManager am = new AssignmentManager();
        ReportGenerator rg = new ReportGenerator();

        um.add(User.validate("test_user", "Test", "test@mail.com"));

        String report = rg.generateUserReport(um, am);
        assertTrue(report.contains("test_user"));
        assertTrue(report.contains("No roles"));
    }
}
