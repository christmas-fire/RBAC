package org.example.filter;

import org.example.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserFiltersTest {

    private static User testUser;

    @BeforeAll
    static void setUp() {
        testUser = User.validate("john_doe", "John Doe", "john.doe@example.com");
    }

    @Test
    @DisplayName("Filter byUsername: should match correct username")
    void testByUsername_Match() {
        UserFilter filter = UserFilters.byUsername("john_doe");
        assertTrue(filter.test(testUser));
    }

    @Test
    @DisplayName("Filter byUsername: should not match incorrect username")
    void testByUsername_NoMatch() {
        UserFilter filter = UserFilters.byUsername("jane_doe");
        assertFalse(filter.test(testUser));
    }

    @Test
    @DisplayName("Filter byUsernameContains: should find substring and be case-insensitive")
    void testByUsernameContains() {
        UserFilter filter1 = UserFilters.byUsernameContains("john");
        assertTrue(filter1.test(testUser));

        UserFilter filter2 = UserFilters.byUsernameContains("DOE");
        assertTrue(filter2.test(testUser), "Should be case-insensitive");

        UserFilter filter3 = UserFilters.byUsernameContains("kevin");
        assertFalse(filter3.test(testUser));
    }

    @Test
    @DisplayName("Filter byEmail: should match exact email")
    void testByEmail() {
        UserFilter filter = UserFilters.byEmail("john.doe@example.com");
        assertTrue(filter.test(testUser));

        UserFilter filter2 = UserFilters.byEmail("john.doe@another.com");
        assertFalse(filter2.test(testUser));
    }

    @Test
    @DisplayName("Filter byFullNameContains: should find substring and be case-insensitive")
    void testByFullNameContains() {
        UserFilter filter1 = UserFilters.byFullNameContains("John");
        assertTrue(filter1.test(testUser));

        UserFilter filter2 = UserFilters.byFullNameContains("dOE");
        assertTrue(filter2.test(testUser), "Should be case-insensitive");

        UserFilter filter3 = UserFilters.byFullNameContains("Smith");
        assertFalse(filter3.test(testUser));
    }
}
