package org.example.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidationUtilsTest {
    @Test
    @DisplayName("Валидация: проверка корректности username и email")
    void testValidation() {
        assertTrue(ValidationUtils.isValidUsername("admin_2025"));
        assertFalse(ValidationUtils.isValidUsername("ab")); // слишком короткий

        assertTrue(ValidationUtils.isValidEmail("test@mail.com"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
    }

    @Test
    @DisplayName("Нормализация: удаление лишних пробелов")
    void testNormalization() {
        String input = "  Ivan    Ivanov  ";
        assertEquals("Ivan Ivanov", ValidationUtils.normalizeString(input));
    }
}
