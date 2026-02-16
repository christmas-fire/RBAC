package org.example.model;

public record Permission(String name, String resource, String description) {
    public Permission(String name, String resource, String description) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        if (name == null || name.contains(" ")) {
            throw new IllegalArgumentException("Имя права не должно содержать пробелов");
        }
        this.name = name.toUpperCase();
        this.resource = resource != null ? resource.toLowerCase() : "";
        this.description = description;
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        return this.name.contains(namePattern.toUpperCase()) &&
                this.resource.contains(resourcePattern.toLowerCase());
    }
}
