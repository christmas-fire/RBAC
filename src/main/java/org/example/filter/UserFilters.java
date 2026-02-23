package org.example.filter;

public class UserFilters {
    public static UserFilter byUsername(String username) {
        return u -> u.username().equals(username);
    }

    public static UserFilter byUsernameContains(String sub) {
        return u -> u.username().toLowerCase().contains(sub.toLowerCase());
    }

    public static UserFilter byEmail(String email) {
        return u -> u.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        return u -> u.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String sub) {
        return u -> u.fullName().toLowerCase().contains(sub.toLowerCase());
    }
}
