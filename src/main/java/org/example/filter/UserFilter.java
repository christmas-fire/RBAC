package org.example.filter;

import org.example.model.User;

@FunctionalInterface
public interface UserFilter {
    boolean test(User user);

    default UserFilter and(UserFilter other) {
        return u -> this.test(u) && other.test(u);
    }

    default UserFilter or(UserFilter other) {
        return u -> this.test(u) || other.test(u);
    }
}
