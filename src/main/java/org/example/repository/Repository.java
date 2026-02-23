package org.example.repository;

import java.util.*;

public interface Repository<T> {
    void add(T item);
    boolean remove(T item);
    Optional<T> findById(String id);
    List<T> findAll();
    int count();
    void clear();
}
