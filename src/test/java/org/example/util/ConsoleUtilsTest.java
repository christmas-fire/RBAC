package org.example.util;

import org.junit.jupiter.api.Test;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class ConsoleUtilsTest {
    @Test
    void testPromptString() {
        Scanner mockScanner = new Scanner("Hello World\n");
        String result = ConsoleUtils.promptString(mockScanner, "Message", true);
        assertEquals("Hello World", result);
    }

    @Test
    void testPromptInt() {
        Scanner mockScanner = new Scanner("15\n5\n");
        int result = ConsoleUtils.promptInt(mockScanner, "Enter number", 1, 10);
        assertEquals(5, result);
    }

    @Test
    void testPromptYesNo() {
        Scanner mockScanner = new Scanner("да\n");
        assertTrue(ConsoleUtils.promptYesNo(mockScanner, "Confirm?"));
    }
}
