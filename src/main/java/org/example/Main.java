package org.example;

import org.example.model.User;

public class Main {
    public static void main(String[] args) {
        try {
            User admin = User.validate("Alexander_05", "Alexander Babeshko", "alexander.babeshko@mail.com");
            System.out.println("User created: " + admin.format());

            User.validate("абвгд", "", "12345email");

        } catch (IllegalArgumentException e) {
            System.err.println("\nValidation error: " + e.getMessage());
        }
    }
}
