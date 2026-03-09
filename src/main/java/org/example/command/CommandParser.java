package org.example.command;

import java.util.*;

public class CommandParser {
    private final Map<String, Command> commands = new TreeMap<>(); // TreeMap для алфавитного порядка в help
    private final Map<String, String> commandDescriptions = new TreeMap<>();

    public void registerCommand(String name, String description, Command command) {
        commands.put(name.toLowerCase(), command);
        commandDescriptions.put(name.toLowerCase(), description);
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        Command cmd = commands.get(commandName.toLowerCase());

        if (cmd != null) {
            try {
                cmd.execute(scanner, system);
            } catch (Exception e) {
                System.out.println("Ошибка выполнения [" + commandName + "]: " + e.getMessage());
            }
        } else {
            System.out.println("Ошибка: Команда '" + commandName + "' не существует. Введите 'help' для справки.");
        }
    }

    public void printHelp() {
        System.out.println("\n===== СПРАВКА ПО КОМАНДАМ =====");
        for (Map.Entry<String, String> entry : commandDescriptions.entrySet()) {
            System.out.printf("%-20s — %s\n", entry.getKey(), entry.getValue());
        }
        System.out.println("===============================");
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.isBlank()) {
            return;
        }

        String[] parts = input.trim().split("\\s+");
        String commandName = parts[0];

        executeCommand(commandName, scanner, system);
    }
}
