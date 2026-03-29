package org.example;

import org.example.command.RBACSystem;
import org.example.filter.UserFilters;
import org.example.model.PermanentAssignment;
import org.example.model.AssignmentMetadata;
import org.example.model.Role;
import org.example.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTest {

    private static final int NUM_THREADS = 10;
    private static final int OPERATIONS_PER_THREAD = 100;

    @Test
    public void concurrentReadWriteTest() {
        System.out.println("Starting stress test with " + NUM_THREADS + " threads, " + OPERATIONS_PER_THREAD + " ops each.");

        RBACSystem system = new RBACSystem();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        // Pre-create some roles to be assigned
        Role workerRole = new Role("Worker", "Standard permissions");
        Role auditorRole = new Role("Auditor", "Read-only permissions");
        system.getRoleManager().add(workerRole);
        system.getRoleManager().add(auditorRole);

        final AtomicInteger usersCreated = new AtomicInteger(0);
        final AtomicInteger usersUpdated = new AtomicInteger(0);
        final AtomicInteger rolesAssigned = new AtomicInteger(0);

        List<Callable<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < NUM_THREADS; i++) {
            final int threadIndex = i;
            tasks.add(() -> {
                Random random = new Random();
                for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                    String username = "user" + threadIndex + "-" + j;

                    // Step 1: Always create a user in each operation cycle.
                    User newUser;
                    try {
                        newUser = User.validate(username, "FullName", username + "@test.com");
                        system.getUserManager().add(newUser);
                        usersCreated.incrementAndGet();
                    } catch (IllegalArgumentException e) {
                        System.err.println("Unexpected exception during user creation: " + e.getMessage());
                        continue; // Skip this cycle if creation fails
                    }

                    // Step 2: Perform a random follow-up operation on the user that was just created.
                    int operation = random.nextInt(3);
                    switch (operation) {
                        case 0: // Assign a role to the new user
                            try {
                                Role roleToAssign = random.nextBoolean() ? workerRole : auditorRole;
                                AssignmentMetadata meta = AssignmentMetadata.now("stress-test", "auto-assign");
                                system.getAssignmentManager().add(new PermanentAssignment(newUser, roleToAssign, meta));
                                rolesAssigned.incrementAndGet();
                            } catch (IllegalStateException e) {
                                // This is a valid outcome in a concurrent test if the same role is assigned twice.
                            }
                            break;
                        case 1: // Update the new user
                            system.getUserManager().update(username, "Updated Name", "updated."+username+"@test.com");
                            usersUpdated.incrementAndGet();
                            break;
                        case 2: // Search for users (a read operation)
                            system.getUserManager().findByFilterParallel(UserFilters.byUsernameContains("user" + threadIndex));
                            break;
                    }
                }
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executor.invokeAll(tasks);
            // Check for exceptions
            for (Future<Void> future : futures) {
                future.get(); // This will throw an exception if the callable threw one
            }
        } catch (InterruptedException | ExecutionException e) {
            fail("Test failed due to an exception in a worker thread.", e);
        } finally {
            system.shutdown();
            executor.shutdown();
        }

        System.out.println("Stress test finished.");
        System.out.println("Users created: " + usersCreated.get());
        System.out.println("Users updated: " + usersUpdated.get());
        System.out.println("Roles assigned: " + rolesAssigned.get());

        // Final state validation
        long finalUserCount = system.getUserManager().count();
        long finalAssignmentCount = system.getAssignmentManager().count();

        System.out.println("Final user count in manager: " + finalUserCount);
        System.out.println("Final assignment count in manager: " + finalAssignmentCount);

        assertEquals(usersCreated.get(), finalUserCount, "The number of users created should match the final count in the manager.");
        assertTrue(finalUserCount > 0, "At least one user should have been created.");
        assertTrue(finalUserCount <= (long) NUM_THREADS * OPERATIONS_PER_THREAD, "User count should not exceed the total possible creations.");
    }
}
