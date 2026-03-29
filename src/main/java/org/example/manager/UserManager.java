package org.example.manager;

import org.example.filter.UserFilter;
import org.example.model.User;
import org.example.repository.Repository;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class UserManager implements Repository<User>, Serializable {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public synchronized void add(User user) {
        if (exists(user.username())) {
            throw new IllegalArgumentException("Пользователь с именем " + user.username() + " уже существует.");
        }
        User.validate(user.username(), user.fullName(), user.email());
        users.put(user.username(), user);
    }

    @Override
    public synchronized boolean remove(User user) {
        if (!exists(user.username())) {
            throw new IllegalArgumentException("Пользователь не найден: " + user.username());
        }
        return users.remove(user.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        return findById(username);
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.email().equalsIgnoreCase(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        return users.values().stream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findByFilterParallel(UserFilter filter) {
        return users.values().parallelStream()
                .filter(filter::test)
                .collect(Collectors.toList());
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        return users.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .collect(Collectors.toList());
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public synchronized void update(String username, String newFullName, String newEmail) {
        if (!exists(username)) {
            throw new IllegalArgumentException("Ошибка обновления. Пользователь " + username + " не существует.");
        }
        User updatedUser = User.validate(username, newFullName, newEmail);
        users.put(username, updatedUser);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserManager that)) return false;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}
