package org.example.util;


import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {
    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message + (required ? " (обязательно): " : ": "));
            String input = scanner.nextLine().trim();

            if (required && input.isEmpty()) {
                System.out.println("\u001B[31mОшибка: Это поле нельзя оставлять пустым.\u001B[0m");
                continue;
            }
            return input;
        }
    }

    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(String.format("%s [%d..%d]: ", message, min, max));
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("\u001B[31mОшибка: Число должно быть в диапазоне от %d до %d.\u001B[0m\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("\u001B[31mОшибка: Пожалуйста, введите целое число.\u001B[0m");
            }
        }
    }

    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (да/нет): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("да") || input.equals("д") || input.equals("yes") || input.equals("y")) {
                return true;
            }
            if (input.equals("нет") || input.equals("н") || input.equals("no") || input.equals("n")) {
                return false;
            }
            System.out.println("\u001B[31mОшибка: Введите 'да' или 'нет'.\u001B[0m");
        }
    }

    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            System.out.println("Список для выбора пуст.");
            return null;
        }

        System.out.println("\n--- " + message + " ---");
        for (int i = 0; i < options.size(); i++) {
            System.out.printf("%d. %s\n", i + 1, options.get(i).toString());
        }

        int choice = promptInt(scanner, "Ваш выбор", 1, options.size());
        return options.get(choice - 1);
    }
}
