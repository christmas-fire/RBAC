package org.example.model;

import java.io.Serializable;
import org.example.util.ValidationUtils;

public record User(String username, String fullName, String email) implements Serializable {

    public static User validate(String username, String fullName, String email) {
        ValidationUtils.requireNonEmpty(username, "Username");
        ValidationUtils.requireNonEmpty(fullName, "Full Name");
        ValidationUtils.requireNonEmpty(email, "Email");

        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("Username должен быть от 3 до 20 символов (буквы, цифры, подчёркивание)");
        }
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("Некорректный формат Email (отсутствует @ или точка)");
        }

        return new User(
                ValidationUtils.normalizeString(username),
                ValidationUtils.normalizeString(fullName),
                ValidationUtils.normalizeString(email).toLowerCase()
        );
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}
