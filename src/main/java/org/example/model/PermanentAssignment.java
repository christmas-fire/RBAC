package org.example.model;

public class PermanentAssignment extends AbstractRoleAssignment {
    private boolean revoked = false;

    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
    }

    public void revoke() { this.revoked = true; }

    public boolean isRevoked() { return revoked; }

    @Override
    public boolean isActive() { return !revoked; }

    @Override
    public String assignmentType() { return "PERMANENT"; }
}
