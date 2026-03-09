package org.example;

import org.example.command.CommandParser;
import org.example.command.CommandRegistry;
import org.example.command.RBACSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        RBACSystem system = new RBACSystem();
        system.initialize();

        CommandParser parser = new CommandParser();
        CommandRegistry.registerAll(parser);

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== RBAC ===");
        System.out.println("Logged in as: " + system.getCurrentUser());
        System.out.println("Type 'help' to see commands.");

        while (true) {
            System.out.print("\n[" + system.getCurrentUser() + "] > ");
            String input = scanner.nextLine();
            parser.parseAndExecute(input, scanner, system);
        }
    }
}
