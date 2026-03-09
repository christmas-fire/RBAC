package org.example.audit;

public record AuditEntry(
        String timestamp,
        String action,
        String performer,
        String target,
        String details
) {}
