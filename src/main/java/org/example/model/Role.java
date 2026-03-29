package org.example.model;

import java.util.*;

public class Role {
    private static int counter = 1;
    private final String id;
    private String name;
    private String description;
    private final Set<Permission> permissions = Collections.synchronizedSet(new HashSet<>());

    public Role(String name, String description) {
        this.id = "role_" + (counter++);
        this.name = name;
        this.description = description;
    }

    public synchronized void addPermission(Permission p) { permissions.add(p); }

    public synchronized void removePermission(Permission p) { permissions.remove(p); }

    public boolean hasPermission(Permission p) { return permissions.contains(p); }

    public boolean hasPermission(String name, String resource) {
        return permissions.stream()
                .anyMatch(p -> p.name()
                .equals(name.toUpperCase()) && p.resource()
                .equals(resource.toLowerCase()));
    }

    public Set<Permission> getPermissions() { return Collections.unmodifiableSet(permissions); }

    public String getId() { return id; }

    public String getName() { return name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Role: %s [ID: %s]\n", name, id));
        sb.append(String.format("Description: %s\n", description));
        sb.append(String.format("Permissions (%d):\n", permissions.size()));
        synchronized (permissions) {
            for (Permission p : permissions) {
                sb.append(" - ").append(p.format()).append("\n");
            }
        }
        return sb.toString();
    }

    public synchronized void setName(String newName) {
        this.name = newName;
    }

    public synchronized void setDescription(String newDescription) {
        this.description = newDescription;
    }
}
