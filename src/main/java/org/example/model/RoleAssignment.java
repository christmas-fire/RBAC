package org.example.model;

import java.io.Serializable;

public interface RoleAssignment extends Serializable {
    String assignmentId();
    User user();
    Role role();
    AssignmentMetadata metadata();
    boolean isActive();
    String assignmentType();
}
