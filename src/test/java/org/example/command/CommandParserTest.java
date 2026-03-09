package org.example.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

class CommandParserTest {
    private CommandParser parser;
    private RBACSystem system;
    private boolean commandExecuted;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        commandExecuted = false;
    }

    @Test
    @DisplayName("Регистрация и выполнение: команда должна запускаться по имени")
    void testRegisterAndExecute() {
        parser.registerCommand("test-cmd", "Description", (scanner, sys) -> {
            commandExecuted = true;
        });

        parser.executeCommand("test-cmd", new Scanner(""), system);
        assertTrue(commandExecuted, "Команда должна была выполниться");
    }

    @Test
    @DisplayName("Парсинг: извлечение имени команды из строки ввода")
    void testParseAndExecute() {
        parser.registerCommand("run", "Desc", (scanner, sys) -> {
            commandExecuted = true;
        });

        parser.parseAndExecute("  run  some args ", new Scanner(""), system);
        assertTrue(commandExecuted);
    }

    @Test
    @DisplayName("Регистронезависимость: парсер должен понимать RUN и run")
    void testCaseInsensitivity() {
        parser.registerCommand("save", "Desc", (scanner, sys) -> {
            commandExecuted = true;
        });

        parser.executeCommand("SAVE", new Scanner(""), system);
        assertTrue(commandExecuted);
    }

    @Test
    @DisplayName("Несуществующая команда: система не должна падать")
    void testUnknownCommand() {
        assertDoesNotThrow(() -> {
            parser.executeCommand("ghost-command", new Scanner(""), system);
        });
    }
}
