package org.example.util;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {
    @Test
    void testTruncate() {
        String longText = "This is a very long description";
        String truncated = FormatUtils.truncate(longText, 10);
        assertEquals("This is...", truncated);
    }

    @Test
    void testTableGeneration() {
        String[] headers = {"ID", "Name"};
        List<String[]> rows = List.of(
                new String[]{"1", "Admin"},
                new String[]{"2", "User"}
        );

        String table = FormatUtils.formatTable(headers, rows);

        assertTrue(table.contains("Admin"));
        assertTrue(table.contains("+----"));
    }
}
