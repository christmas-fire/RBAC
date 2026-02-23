package org.example.filter;

import org.example.model.Role;

@FunctionalInterface
public interface RoleFilter {
    boolean test(Role role);

    default RoleFilter and(RoleFilter other) {
        return r -> this.test(r) && other.test(r);
    }

    default RoleFilter or(RoleFilter other) {
        return r -> this.test(r) || other.test(r);
    }
}
