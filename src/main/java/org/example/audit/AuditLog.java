package org.example.audit;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = Collections.synchronizedList(new ArrayList<>());
    private final BlockingQueue<AuditEntry> logQueue = new LinkedBlockingQueue<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "AuditLog-Writer");
        t.setDaemon(true);
        return t;
    });

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditLog() {
        start();
    }

    private void start() {
        executor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    AuditEntry entry = logQueue.take();
                    entries.add(entry);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Поток аудита прерван.");
            }
        });
    }

    public void stop() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            // Process any remaining entries in the queue
            processRemainingQueueItems();
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private void processRemainingQueueItems() {
        System.out.println("Сохранение оставшихся записей аудита...");
        while (!logQueue.isEmpty()) {
            entries.add(logQueue.poll());
        }
        System.out.println("Журнал аудита полностью сохранен.");
    }


    public void log(String action, String performer, String target, String details) {
        String now = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(now, action, performer, target, details);
        logQueue.offer(entry);
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.performer().equalsIgnoreCase(performer))
                    .collect(Collectors.toList());
        }
    }

    public List<AuditEntry> getByAction(String action) {
        synchronized (entries) {
            return entries.stream()
                    .filter(e -> e.action().equalsIgnoreCase(action))
                    .collect(Collectors.toList());
        }
    }

    public void printLog() {
        System.out.println("\n--- ЖУРНАЛ АУДИТА ---");
        synchronized (entries) {
            for (AuditEntry e : entries) {
                System.out.printf("[%s] ДЕЙСТВИЕ: %s | КТО: %s | ЦЕЛЬ: %s | ДЕТАЛИ: %s\n",
                        e.timestamp(), e.action(), e.performer(), e.target(), e.details());
            }
        }
    }

    public void saveToFile(String filename) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filename))) {
            synchronized (entries) {
                for (AuditEntry e : entries) {
                    out.printf("%s|%s|%s|%s|%s\n",
                            e.timestamp(), e.action(), e.performer(), e.target(), e.details());
                }
            }
            System.out.println("Лог успешно сохранен в " + filename);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения файла: " + e.getMessage());
        }
    }
}
