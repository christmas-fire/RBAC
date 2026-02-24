package org.example.manager;

import org.example.filter.UserFilter;
import org.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {
    private UserManager userManager;
    private User testUser;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        testUser = User.validate("ivan_99", "Ivan Ivanov", "ivan@mail.ru");
    }

    @Test
    @DisplayName("Добавление пользователя: успешный сценарий")
    void testAddUserSuccess() {
        userManager.add(testUser);
        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("ivan_99"));
    }

    @Test
    @DisplayName("Добавление пользователя: ошибка при дублировании username")
    void testAddUserDuplicateThrowsException() {
        userManager.add(testUser);
        User duplicate = User.validate("ivan_99", "Another Name", "another@mail.ru");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                userManager.add(duplicate)
        );
        assertTrue(exception.getMessage().contains("уже существует"));
    }

    @Test
    @DisplayName("Удаление пользователя: успешный сценарий")
    void testRemoveUserSuccess() {
        userManager.add(testUser);
        boolean removed = userManager.remove(testUser);

        assertTrue(removed);
        assertEquals(0, userManager.count());
    }

    @Test
    @DisplayName("Удаление пользователя: ошибка если пользователя нет")
    void testRemoveUserNotFoundThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                userManager.remove(testUser)
        );
    }

    @Test
    @DisplayName("Обновление данных: успешный сценарий")
    void testUpdateUserSuccess() {
        userManager.add(testUser);
        userManager.update("ivan_99", "New Full Name", "new@email.com");

        User updated = userManager.findByUsername("ivan_99").get();
        assertEquals("New Full Name", updated.fullName());
        assertEquals("new@email.com", updated.email());
    }

    @Test
    @DisplayName("Поиск по Email: игнорирование регистра")
    void testFindByEmailIgnoreCase() {
        userManager.add(testUser);
        Optional<User> found = userManager.findByEmail("IVAN@MAIL.RU");

        assertTrue(found.isPresent());
        assertEquals("ivan_99", found.get().username());
    }

    @Test
    @DisplayName("Фильтрация: поиск пользователей по части имени")
    void testFindByFilter() {
        userManager.add(User.validate("user1", "Alex Smith", "alex@mail.ru"));
        userManager.add(User.validate("user2", "John Smith", "john@mail.ru"));
        userManager.add(User.validate("user3", "Alice Brown", "alice@mail.ru"));

        UserFilter smithFilter = u -> u.fullName().contains("Smith");
        List<User> results = userManager.findByFilter(smithFilter);

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Комплексный поиск: фильтрация + сортировка")
    void testFindAllWithFilterAndSorter() {
        userManager.add(User.validate("cat", "Cat", "cat@mail.ru"));
        userManager.add(User.validate("apple", "Apple", "apple@mail.ru"));
        userManager.add(User.validate("banana", "Banana", "banana@mail.ru"));

        UserFilter longNameFilter = u -> u.username().length() > 3;
        Comparator<User> alphabetSorter = Comparator.comparing(User::username);

        List<User> results = userManager.findAll(longNameFilter, alphabetSorter);

        assertEquals(2, results.size());
        assertEquals("apple", results.get(0).username());
        assertEquals("banana", results.get(1).username());
    }

    @Test
    @DisplayName("Метод clear: полная очистка хранилища")
    void testClear() {
        userManager.add(testUser);
        userManager.clear();
        assertEquals(0, userManager.count());
    }

    @Test
    @DisplayName("Equals и HashCode: сравнение двух менеджеров")
    void testEqualsAndHashCode() {
        UserManager anotherManager = new UserManager();

        userManager.add(testUser);
        anotherManager.add(testUser);

        assertEquals(userManager, anotherManager);
        assertEquals(userManager.hashCode(), anotherManager.hashCode());
    }
}
