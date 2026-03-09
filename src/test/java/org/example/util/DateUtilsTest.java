package org.example.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateUtilsTest {
    @Test
    @DisplayName("Дата: строковое сравнение в формате YYYY-MM-DD")
    void testDateComparison() {
        String date1 = "2025-01-01";
        String date2 = "2025-05-01";

        assertTrue(DateUtils.isBefore(date1, date2));
        assertTrue(DateUtils.isAfter(date2, date1));
    }

    @Test
    @DisplayName("Дата: проверка относительного времени (days ago/in days)")
    void testRelativeTime() {
        String today = DateUtils.getCurrentDate();
        assertEquals("today", DateUtils.formatRelativeTime(today));

        String future = DateUtils.addDays(today, 5);
        assertEquals("in 5 days", DateUtils.formatRelativeTime(future));
    }
}
