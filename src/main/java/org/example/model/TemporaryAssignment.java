package org.example.model;

import org.example.util.DateUtils;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private volatile String expiresAt;
    private final boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    public String getExpiresAt() {
        return this.expiresAt;
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    public boolean isExpired() {
        return DateUtils.isAfter(DateUtils.getCurrentDateTime(), expiresAt);
    }

    @Override
    public String assignmentType() { return "TEMPORARY"; }

    public synchronized void extend(String newExpirationDate) {
        this.expiresAt = newExpirationDate;
    }

    // Используем относительное время для вывода
    public String getTimeRemaining() {
        return DateUtils.formatRelativeTime(expiresAt);
    }

    @Override
    public String summary() {
        return super.summary() + "\nСрок действия: " + expiresAt + " (" + getTimeRemaining() + ")";
    }
}
