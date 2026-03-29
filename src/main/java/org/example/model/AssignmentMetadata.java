package org.example.model;

import org.example.util.DateUtils;

import java.io.Serializable;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) implements Serializable {
    public static AssignmentMetadata now(String assignedBy, String reason) {
        String now = DateUtils.getCurrentDateTimeNoSeconds();
        return new AssignmentMetadata(assignedBy, now, reason);
    }

    public String format() {
        return String.format("by %s at %s. Reason: %s", assignedBy, assignedAt, (reason != null ? reason : "N/A"));
    }
}
