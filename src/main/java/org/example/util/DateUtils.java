package org.example.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    public static boolean isBefore(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) < 0;
    }

    public static boolean isAfter(String date1, String date2) {
        if (date1 == null || date2 == null) return false;
        return date1.compareTo(date2) > 0;
    }

    public static String addDays(String date, int days) {
        try {
            String pureDate = date.contains(" ") ? date.split(" ")[0] : date;
            LocalDate ld = LocalDate.parse(pureDate, DATE_FORMATTER);
            return ld.plusDays(days).format(DATE_FORMATTER);
        } catch (Exception e) {
            return date;
        }
    }

    public static String formatRelativeTime(String targetDate) {
        try {
            String pureDate = targetDate.contains(" ") ? targetDate.split(" ")[0] : targetDate;
            LocalDate target = LocalDate.parse(pureDate, DATE_FORMATTER);
            LocalDate now = LocalDate.now();

            long diff = ChronoUnit.DAYS.between(now, target);

            if (diff == 0) return "today";
            if (diff > 0) return "in " + diff + " days";
            return Math.abs(diff) + " days ago";
        } catch (Exception e) {
            return "unknown time";
        }
    }
}
