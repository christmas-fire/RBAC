package org.example.model;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    protected final String assignmentId;
    protected final User user;
    protected final Role role;
    protected final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = UUID.randomUUID().toString().substring(0, 8);
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() { return assignmentId; }

    @Override
    public User user() { return user; }

    @Override
    public Role role() { return role; }

    @Override
    public AssignmentMetadata metadata() { return metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRoleAssignment that)) return false;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }

    public String summary() {
        return String.format("[%s] %s assigned to %s by %s at %s\nReason: %s\nStatus: %s",
                assignmentType(),
                role.getName(),
                user.username(),
                metadata.assignedBy(),
                metadata.assignedAt(),
                metadata.reason(),
                (isActive() ? "ACTIVE" : "INACTIVE"));
    }
}
