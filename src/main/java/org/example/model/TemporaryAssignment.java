package org.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private final boolean autoRenew;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    public String getExpiresAt() {
        return this.expiresAt;
    }

    @Override
    public boolean isActive() { return !isExpired(); }

    @Override
    public String assignmentType() { return "TEMPORARY"; }

    public void extend(String newExpirationDate) {
        this.expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = LocalDateTime.parse(expiresAt, FORMATTER);
        return now.isAfter(expiry);
    }

    public String getTimeRemaining() {
        if (isExpired()) {
            return "Expired";
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = LocalDateTime.parse(expiresAt, FORMATTER);
        Duration duration = Duration.between(now, expiry);

        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();

        return String.format("%d days, %d hours, %d minutes remaining", days, hours, minutes);
    }

    @Override
    public String summary() {
        return super.summary() +
                String.format("\nExpires at: %s (%s)", expiresAt, getTimeRemaining()) +
                "\nAuto-renew: " + (autoRenew ? "Enabled" : "Disabled");
    }
}
