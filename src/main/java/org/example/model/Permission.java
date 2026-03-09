package org.example.model;

import org.example.util.ValidationUtils;

public record Permission(String name, String resource, String description) {
    public Permission(String name, String resource, String description) {
        ValidationUtils.requireNonEmpty(name, "Имя права");
        ValidationUtils.requireNonEmpty(resource, "Ресурс");
        ValidationUtils.requireNonEmpty(description, "Описание");

        this.name = ValidationUtils.normalizeString(name).toUpperCase();
        this.resource = ValidationUtils.normalizeString(resource).toLowerCase();
        this.description = ValidationUtils.normalizeString(description);
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        return this.name.contains(namePattern.toUpperCase()) &&
                this.resource.contains(resourcePattern.toLowerCase());
    }
}
