package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    public static AssignmentMetadata now(String assignedBy, String reason) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return new AssignmentMetadata(assignedBy, now, reason);
    }

    public String format() {
        return String.format("by %s at %s. Reason: %s", assignedBy, assignedAt, (reason != null ? reason : "N/A"));
    }
}
