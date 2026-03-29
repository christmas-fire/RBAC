package org.example.filter;

import org.example.model.Role;
import org.example.model.RoleAssignment;
import org.example.model.TemporaryAssignment;
import org.example.model.User;
import org.example.util.DateUtils;

public class AssignmentFilters {
    public static AssignmentFilter byUser(User user) {
        return a -> a.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return a -> a.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return a -> !a.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return a -> a.assignmentType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        return a -> DateUtils.isAfter(a.metadata().assignedAt(), date);
    }

    public static AssignmentFilter expiringBefore(String date) {
        return a -> {
            if (a instanceof TemporaryAssignment temp) {
                return DateUtils.isBefore(temp.getExpiresAt(), date);
            }
            return false;
        };
    }
}
