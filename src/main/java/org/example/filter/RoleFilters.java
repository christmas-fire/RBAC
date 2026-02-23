package org.example.filter;

import org.example.model.Permission;

public class RoleFilters {
    public static RoleFilter byName(String name) {
        return r -> r.getName().equals(name);
    }

    public static RoleFilter byNameContains(String sub) {
        return r -> r.getName().toLowerCase().contains(sub.toLowerCase());
    }

    public static RoleFilter hasPermission(Permission p) {
        return r -> r.hasPermission(p);
    }

    public static RoleFilter hasPermission(String name, String res) {
        return r -> r.hasPermission(name, res);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return r -> r.getPermissions().size() >= n;
    }
}
