package org.example.manager;

import org.example.model.*;
import org.example.filter.AssignmentFilter;
import org.example.repository.Repository;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AssignmentManager implements Repository<RoleAssignment>, Serializable {
    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();

    private Predicate<User> userExistsChecker = u -> true;
    private Predicate<Role> roleExistsChecker = r -> true;

    public void setExistenceCheckers(Predicate<User> userChecker, Predicate<Role> roleChecker) {
        this.userExistsChecker = userChecker;
        this.roleExistsChecker = roleChecker;
    }

    @Override
    public synchronized void add(RoleAssignment assignment) {
        if (assignment == null) throw new IllegalArgumentException("Назначение не может быть null");

        if (!userExistsChecker.test(assignment.user())) {
            throw new IllegalArgumentException("Пользователь " + assignment.user().username() + " не существует в системе.");
        }
        if (!roleExistsChecker.test(assignment.role())) {
            throw new IllegalArgumentException("Роль " + assignment.role().getName() + " не существует в системе.");
        }

        boolean hasActive = assignments.values().stream()
                .anyMatch(a -> a.user().equals(assignment.user()) &&
                        a.role().equals(assignment.role()) &&
                        a.isActive());

        if (hasActive) {
            throw new IllegalStateException("У пользователя " + assignment.user().username() +
                    " уже есть активное назначение роли " + assignment.role().getName());
        }

        assignments.put(assignment.assignmentId(), assignment);
    }

    @Override
    public boolean remove(RoleAssignment assignment) {
        return assignments.remove(assignment.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByRole(Role role) {
        return assignments.values().stream()
                .filter(a -> a.role().equals(role))
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        return assignments.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        return assignments.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(a -> !a.isActive() && a instanceof TemporaryAssignment)
                .collect(Collectors.toList());
    }

    public boolean userHasRole(User user, Role role) {
        return assignments.values().stream()
                .anyMatch(a -> a.user().equals(user) && a.role().equals(role) && a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) &&
                        p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        return assignments.values().stream()
                .filter(a -> a.user().equals(user) && a.isActive())
                .flatMap(a -> a.role().getPermissions().stream())
                .collect(Collectors.toSet());
    }

    public synchronized void revokeAssignment(String assignmentId) {
        RoleAssignment a = findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Назначение не найдено: " + assignmentId));

        if (a instanceof PermanentAssignment pa) {
            pa.revoke();
        } else {
            throw new UnsupportedOperationException("Отозвать (revoke) можно только постоянное назначение.");
        }
    }

    public synchronized void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment a = findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Назначение не найдено: " + assignmentId));

        if (a instanceof TemporaryAssignment ta) {
            ta.extend(newExpirationDate);
        } else {
            throw new UnsupportedOperationException("Продлить можно только временное назначение.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignmentManager that)) return false;
        return Objects.equals(assignments, that.assignments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignments);
    }
}
