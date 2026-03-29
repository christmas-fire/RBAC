package org.example.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    @AfterEach
    void tearDown() {
        auditLog.stop();
    }

    @Test
    @DisplayName("Async logging should eventually process entries")
    void testAsyncLogging() throws InterruptedException {
        // Act
        auditLog.log("CREATE", "admin", "user1", "Success");
        auditLog.log("DELETE", "root", "user2", "Failed");

        // Assert: Wait for the async worker to process the queue
        // In a real project, use Awaitility or a similar library. Here, a simple poll loop will do.
        waitForCondition(() -> auditLog.getAll().size() == 2, 2, "Log entries were not processed in time.");

        assertEquals(2, auditLog.getAll().size());
        assertEquals(1, auditLog.getByPerformer("admin").size());
        assertEquals(1, auditLog.getByAction("DELETE").size());
    }

    @Test
    @DisplayName("stop() should process remaining items in the queue")
    void testStopFlushesQueue() {
        // Arrange
        AuditLog localAuditLog = new AuditLog(); // Use a local instance to control stop() precisely

        // Act
        localAuditLog.log("ACTION1", "perf1", "t1", "d1");
        localAuditLog.log("ACTION2", "perf2", "t2", "d2");
        localAuditLog.log("ACTION3", "perf3", "t3", "d3");

        // Stop the log service, which should trigger the flush
        localAuditLog.stop();

        // Assert
        assertEquals(3, localAuditLog.getAll().size(), "stop() should ensure all queued items are processed.");
    }

    /**
     * Helper method to wait for a condition to be true with a timeout.
     * @param condition The condition to check.
     * @param timeoutSeconds The maximum time to wait.
     * @param message The assertion message on failure.
     */
    private void waitForCondition(java.util.function.BooleanSupplier condition, long timeoutSeconds, String message) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(100); // Poll every 100ms
        }
        fail(message);
    }
}
