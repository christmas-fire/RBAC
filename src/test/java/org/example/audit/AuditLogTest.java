package org.example.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @Test
    void testLoggingAndFiltering() {
        auditLog.log("CREATE", "admin", "user1", "Success");
        auditLog.log("DELETE", "root", "user2", "Failed");

        assertEquals(2, auditLog.getAll().size());
        assertEquals(1, auditLog.getByPerformer("admin").size());
        assertEquals(1, auditLog.getByAction("DELETE").size());
    }
}
