package org.example.audit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void log(String action, String performer, String target, String details) {
        String now = LocalDateTime.now().format(FORMATTER);
        entries.add(new AuditEntry(now, action, performer, target, details));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(e -> e.performer().equalsIgnoreCase(performer))
                .collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(e -> e.action().equalsIgnoreCase(action))
                .collect(Collectors.toList());
    }

    public void printLog() {
        System.out.println("\n--- ЖУРНАЛ АУДИТА ---");
        for (AuditEntry e : entries) {
            System.out.printf("[%s] ДЕЙСТВИЕ: %s | КТО: %s | ЦЕЛЬ: %s | ДЕТАЛИ: %s\n",
                    e.timestamp(), e.action(), e.performer(), e.target(), e.details());
        }
    }

    public void saveToFile(String filename) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            for (AuditEntry e : entries) {
                out.printf("%s|%s|%s|%s|%s\n",
                        e.timestamp(), e.action(), e.performer(), e.target(), e.details());
            }
            System.out.println("Лог успешно сохранен в " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения файла: " + e.getMessage());
        }
    }
}
