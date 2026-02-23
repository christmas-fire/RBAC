package org.example.filter;

import org.example.model.RoleAssignment;

@FunctionalInterface
public interface AssignmentFilter {
    boolean test(RoleAssignment a);

    default AssignmentFilter and(AssignmentFilter other) {
        return a -> this.test(a) && other.test(a);
    }

    default AssignmentFilter or(AssignmentFilter other) {
        return a -> this.test(a) || other.test(a);
    }
}
