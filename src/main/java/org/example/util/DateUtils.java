package org.example.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_NO_SECONDS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter ISO_T_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    private static LocalDateTime parseLenient(String dateTimeString) {
        if (dateTimeString == null) return null;
        try {
            return LocalDateTime.parse(dateTimeString, TIME_FORMATTER);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(dateTimeString, DATE_TIME_NO_SECONDS_FORMATTER);
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.parse(dateTimeString, DATE_FORMATTER).atStartOfDay();
                } catch (DateTimeParseException e3) {
                    try {
                        return LocalDateTime.parse(dateTimeString, ISO_T_FORMATTER);
                    } catch (DateTimeParseException e4) {
                        return null;
                    }
                }
            }
        }
    }

    public static boolean isBefore(String date1, String date2) {
        LocalDateTime d1 = parseLenient(date1);
        LocalDateTime d2 = parseLenient(date2);
        return d1 != null && d2 != null && d1.isBefore(d2);
    }

    public static boolean isAfter(String date1, String date2) {
        LocalDateTime d1 = parseLenient(date1);
        LocalDateTime d2 = parseLenient(date2);
        return d1 != null && d2 != null && d1.isAfter(d2);
    }

    public static String offsetCurrentDateTime(long amount, TemporalUnit unit) {
        return LocalDateTime.now().plus(amount, unit).format(TIME_FORMATTER);
    }
    
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    public static String getCurrentDateTimeNoSeconds() {
        return LocalDateTime.now().format(DATE_TIME_NO_SECONDS_FORMATTER);
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
            LocalDateTime target = parseLenient(targetDate);
            if (target == null) return "unknown time";

            LocalDateTime now = LocalDateTime.now();
            long diff = ChronoUnit.DAYS.between(now, target);

            if (diff == 0) return "today";
            if (diff > 0) return "in " + diff + " days";
            return Math.abs(diff) + " days ago";
        } catch (Exception e) {
            return "unknown time";
        }
    }
}
