package org.example.model;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    public static User validate(String username, String fullName, String email) {
        if (username == null || username.isBlank() ||
                fullName == null || fullName.isBlank() ||
                email == null || email.isBlank()) {
            throw new IllegalArgumentException("Все поля обязательны и не могут быть пустыми");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username должен быть от 3 до 20 символов (латиница, цифры, подчёркивание)");
        }
        if (!email.contains("@") || !email.substring(email.indexOf("@")).contains(".")) {
            throw new IllegalArgumentException("Некорректный формат email");
        }
        return new User(username, fullName, email);
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}