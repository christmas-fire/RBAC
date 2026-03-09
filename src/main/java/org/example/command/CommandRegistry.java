package org.example.command;

import org.example.filter.*;
import org.example.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {
    public static void registerAll(CommandParser parser) {
        parser.registerCommand("user-list", "Список пользователей", (scanner, system) -> {
            List<User> users = system.getUserManager().findAll();
            printUserTable(users);
        });

        parser.registerCommand("user-create", "Создать пользователя", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            System.out.print("Full Name: "); String fn = scanner.nextLine();
            System.out.print("Email: "); String em = scanner.nextLine();
            try {
                User user = User.validate(un, fn, em);
                system.getUserManager().add(user);
                system.getAuditLog().log("USER_CREATE", system.getCurrentUser(), un, "Создан новый профиль");
                System.out.println("Пользователь создан.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Просмотр пользователя", (scanner, system) -> {
            System.out.print("Введите username: "); String un = scanner.nextLine();
            system.getUserManager().findById(un).ifPresentOrElse(u -> {
                System.out.println(u.format());
                List<RoleAssignment> as = system.getAssignmentManager().findByUser(u);
                System.out.println("Назначенные роли: " + as.stream()
                        .filter(RoleAssignment::isActive)
                        .map(a -> a.role().getName()).collect(Collectors.joining(", ")));
                System.out.println("Все права: " + system.getAssignmentManager().getUserPermissions(u));
            }, () -> System.out.println("Пользователь не найден."));
        });

        parser.registerCommand("user-update", "Обновить пользователя", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            System.out.print("Новое Full Name: "); String fn = scanner.nextLine();
            System.out.print("Новый Email: "); String em = scanner.nextLine();
            system.getUserManager().update(un, fn, em);
            System.out.println("Данные обновлены.");
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            System.out.print("Username для удаления: "); String un = scanner.nextLine();
            System.out.print("Вы уверены? (введите 'да'): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                User u = system.getUserManager().findById(un).orElseThrow();

                system.getAssignmentManager().findByUser(u).forEach(a -> system.getAssignmentManager().remove(a));
                system.getUserManager().remove(u);
                system.getAuditLog().log("USER_DELETE", system.getCurrentUser(), un, "Пользователь полностью удален");
                System.out.println("Удалено.");
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей", (scanner, system) -> {
            System.out.println("1. По username | 2. По email | 3. По домену | 4. По Full Name");
            String choice = scanner.nextLine();
            System.out.print("Введите строку поиска: "); String query = scanner.nextLine();
            UserFilter filter = switch (choice) {
                case "1" -> UserFilters.byUsernameContains(query);
                case "2" -> u -> u.email().contains(query);
                case "3" -> UserFilters.byEmailDomain(query);
                case "4" -> UserFilters.byFullNameContains(query);
                default -> u -> true;
            };
            printUserTable(system.getUserManager().findByFilter(filter));
        });

        parser.registerCommand("role-list", "Список ролей", (scanner, system) -> {
            System.out.printf("%-15s | %-10s | %-15s\n", "Название", "Прав", "ID");
            system.getRoleManager().findAll().forEach(r ->
                    System.out.printf("%-15s | %-10d | %-15s\n", r.getName(), r.getPermissions().size(), r.getId()));
        });

        parser.registerCommand("role-create", "Создать роль", (scanner, system) -> {
            System.out.print("Название: "); String name = scanner.nextLine();
            System.out.print("Описание: "); String desc = scanner.nextLine();
            Role role = new Role(name, desc);
            while (true) {
                System.out.print("Добавить право? (name resource desc / 'нет'): ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("нет")) break;
                String[] p = input.split(" ", 3);
                if (p.length == 3) role.addPermission(new Permission(p[0], p[1], p[2]));
            }
            system.getRoleManager().add(role);
            system.getAuditLog().log("ROLE_CREATE", system.getCurrentUser(), name, "Создана новая роль в системе");
            System.out.println("Роль создана.");
        });

        parser.registerCommand("role-view", "Просмотр информации о роли", (scanner, system) -> {
            System.out.print("Введите имя роли для просмотра: ");
            String name = scanner.nextLine();

            system.getRoleManager().findByName(name).ifPresentOrElse(
                    role -> {
                        System.out.println("\n--- ИНФОРМАЦИЯ О РОЛИ ---");
                        System.out.println(role.format());
                    },
                    () -> System.out.println("Ошибка: Роль '" + name + "' не найдена.")
            );
        });

        parser.registerCommand("role-update", "Обновить название или описание роли", (scanner, system) -> {
            System.out.print("Введите ТЕКУЩЕЕ имя роли: ");
            String oldName = scanner.nextLine();

            if (!system.getRoleManager().exists(oldName)) {
                System.out.println("Ошибка: Роль не существует.");
                return;
            }

            System.out.print("Введите НОВОЕ имя роли (или Enter чтобы оставить): ");
            String newName = scanner.nextLine();
            if (newName.isBlank()) newName = oldName;

            System.out.print("Введите НОВОЕ описание: ");
            String newDesc = scanner.nextLine();

            try {
                system.getRoleManager().update(oldName, newName, newDesc);
                System.out.println("Успех: Данные роли обновлены.");
            } catch (Exception e) {
                System.out.println("Ошибка обновления: " + e.getMessage());
            }
        });

        parser.registerCommand("role-delete", "Удалить роль", (scanner, system) -> {
            System.out.print("Имя роли: "); String name = scanner.nextLine();
            Role role = system.getRoleManager().findByName(name).orElseThrow();

            List<RoleAssignment> active = system.getAssignmentManager().findByRole(role).stream()
                    .filter(RoleAssignment::isActive).toList();

            if (!active.isEmpty()) {
                System.out.println("ВНИМАНИЕ: Роль назначена пользователям: " +
                        active.stream().map(a -> a.user().username()).toList());
            }
            System.out.print("Подтвердить удаление? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                system.getRoleManager().remove(role);
                system.getAuditLog().log("ROLE_DELETE", system.getCurrentUser(), name, "Роль удалена");
                System.out.println("Роль удалена.");
            }
        });

        parser.registerCommand("role-add-permission", "Добавить право к роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine();

            if (!system.getRoleManager().exists(roleName)) {
                System.out.println("Ошибка: Роль не найдена.");
                return;
            }

            System.out.print("Имя права (например, READ): ");
            String pName = scanner.nextLine();
            System.out.print("Ресурс (например, reports): ");
            String resource = scanner.nextLine();
            System.out.print("Описание права: ");
            String desc = scanner.nextLine();

            try {
                Permission p = new Permission(pName, resource, desc);
                system.getRoleManager().addPermissionToRole(roleName, p);
                System.out.println("Успех: Право добавлено к роли " + roleName);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Удалить право из роли", (scanner, system) -> {
            System.out.print("Введите имя роли: ");
            String roleName = scanner.nextLine();

            Role role = system.getRoleManager().findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Роль не найдена."));

            List<Permission> permissions = new ArrayList<>(role.getPermissions());

            if (permissions.isEmpty()) {
                System.out.println("У этой роли нет назначенных прав.");
                return;
            }

            System.out.println("Список прав роли " + roleName + ":");
            for (int i = 0; i < permissions.size(); i++) {
                System.out.println((i + 1) + ". " + permissions.get(i).format());
            }

            System.out.print("Введите номер права для удаления: ");
            try {
                int index = Integer.parseInt(scanner.nextLine()) - 1;
                if (index >= 0 && index < permissions.size()) {
                    Permission toRemove = permissions.get(index);
                    role.removePermission(toRemove);
                    System.out.println("Успех: Право '" + toRemove.name() + "' удалено.");
                } else {
                    System.out.println("Ошибка: Неверный номер.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите число.");
            }
        });

        parser.registerCommand("role-search", "Поиск ролей по фильтрам", (scanner, system) -> {
            System.out.println("Выберите критерий поиска:");
            System.out.println("1. По имени (содержит)");
            System.out.println("2. По наличию конкретного права");
            System.out.println("3. По минимальному количеству прав");
            System.out.print("> ");

            String choice = scanner.nextLine();
            List<Role> results = new ArrayList<>();

            switch (choice) {
                case "1" -> {
                    System.out.print("Введите часть имени: ");
                    String sub = scanner.nextLine();
                    results = system.getRoleManager().findByFilter(r -> r.getName().toLowerCase().contains(sub.toLowerCase()));
                }
                case "2" -> {
                    System.out.print("Имя права (напр. READ): ");
                    String pName = scanner.nextLine();
                    System.out.print("Ресурс (напр. users): ");
                    String res = scanner.nextLine();
                    results = system.getRoleManager().findRolesWithPermission(pName, res);
                }
                case "3" -> {
                    System.out.print("Минимальное количество прав: ");
                    int min = Integer.parseInt(scanner.nextLine());
                    results = system.getRoleManager().findByFilter(r -> r.getPermissions().size() >= min);
                }
                default -> System.out.println("Неверный выбор.");
            }

            if (results.isEmpty()) {
                System.out.println("Роли не найдены.");
            } else {
                System.out.println("\nРезультаты поиска:");
                results.forEach(r -> System.out.println("- " + r.getName() + " (Прав: " + r.getPermissions().size() + ")"));
            }
        });

        parser.registerCommand("assign-role", "Назначить роль", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            User u = system.getUserManager().findById(un).orElseThrow();
            System.out.println("Доступные роли: " + system.getRoleManager().findAll().stream().map(Role::getName).toList());
            System.out.print("Выберите роль: "); String rn = scanner.nextLine();
            Role r = system.getRoleManager().findByName(rn).orElseThrow();

            System.out.print("Тип (P - постоянное, T - временное): ");
            String type = scanner.nextLine();
            System.out.print("Причина: "); String reason = scanner.nextLine();
            AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);

            if (type.equalsIgnoreCase("T")) {
                System.out.print("Дата истечения (YYYY-MM-DD HH:mm): ");
                String date = scanner.nextLine();
                system.getAssignmentManager().add(new TemporaryAssignment(u, r, meta, date, false));
            } else {
                system.getAssignmentManager().add(new PermanentAssignment(u, r, meta));
            }
            System.out.println("Назначено.");
        });

        parser.registerCommand("assignment-list", "Все назначения", (scanner, system) -> {
            printAssignmentTable(system.getAssignmentManager().findAll());
        });

        parser.registerCommand("revoke-role", "Отозвать роль", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            List<RoleAssignment> active = system.getAssignmentManager().findByUser(system.getUserManager().findById(un).get())
                    .stream().filter(RoleAssignment::isActive).toList();

            for (int i = 0; i < active.size(); i++) {
                System.out.println(i + ". " + active.get(i).role().getName() + " [" + active.get(i).assignmentId() + "]");
            }
            System.out.print("Номер для отзыва: ");
            int idx = Integer.parseInt(scanner.nextLine());
            system.getAssignmentManager().revokeAssignment(active.get(idx).assignmentId());
            system.getAuditLog().log("REVOKE_ROLE", system.getCurrentUser(), un, "Отозвана роль");
            System.out.println("Роль отозвана.");
        });

        parser.registerCommand("assignment-list-user", "Назначения конкретного пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String un = scanner.nextLine();
            system.getUserManager().findById(un).ifPresentOrElse(user -> {
                List<RoleAssignment> userAssignments = system.getAssignmentManager().findByUser(user);
                if (userAssignments.isEmpty()) {
                    System.out.println("У пользователя нет назначений.");
                } else {
                    System.out.println("\nНазначения для " + un + ":");
                    printAssignmentTable(userAssignments);
                }
            }, () -> System.out.println("Ошибка: Пользователь не найден."));
        });

        parser.registerCommand("assignment-list-role", "Кто обладает данной ролью", (scanner, system) -> {
            System.out.print("Введите название роли: ");
            String roleName = scanner.nextLine();
            system.getRoleManager().findByName(roleName).ifPresentOrElse(role -> {
                List<RoleAssignment> roleAssignments = system.getAssignmentManager().findByRole(role);
                if (roleAssignments.isEmpty()) {
                    System.out.println("Эта роль никому не назначена.");
                } else {
                    System.out.println("\nПользователи с ролью " + roleName + ":");
                    roleAssignments.forEach(a -> System.out.println("- " + a.user().username() + " [" + a.assignmentType() + "]"));
                }
            }, () -> System.out.println("Ошибка: Роль не найдена."));
        });

        parser.registerCommand("assignment-active", "Список активных назначений", (scanner, system) -> {
            printAssignmentTable(system.getAssignmentManager().getActiveAssignments());
        });

        parser.registerCommand("assignment-expired", "Список истекших назначений", (scanner, system) -> {
            printAssignmentTable(system.getAssignmentManager().getExpiredAssignments());
        });

        parser.registerCommand("assignment-extend", "Продлить временную роль", (scanner, system) -> {
            System.out.println("Как найти назначение? (1 - по ID, 2 - по username + role)");
            String choice = scanner.nextLine();
            String assignmentId = "";

            if (choice.equals("1")) {
                System.out.print("Введите ID назначения: ");
                assignmentId = scanner.nextLine();
            } else {
                System.out.print("Username: "); String un = scanner.nextLine();
                System.out.print("Роль: "); String rn = scanner.nextLine();

                assignmentId = system.getAssignmentManager().findAll().stream()
                        .filter(a -> a.user().username().equals(un) && a.role().getName().equals(rn))
                        .map(RoleAssignment::assignmentId)
                        .findFirst().orElse("");
            }

            if (assignmentId.isEmpty()) {
                System.out.println("Ошибка: Назначение не найдено.");
                return;
            }

            System.out.print("Введите новую дату истечения (YYYY-MM-DD HH:mm): ");
            String newDate = scanner.nextLine();
            try {
                system.getAssignmentManager().extendTemporaryAssignment(assignmentId, newDate);
                System.out.println("Успех: Срок действия продлен.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-search", "Поиск назначений", (scanner, system) -> {
            System.out.println("Фильтры: 1.По юзеру | 2.По роли | 3.Тип | 4.Статус | 5.После даты | 6.Истекающие до");
            System.out.print("> ");
            String choice = scanner.nextLine();

            AssignmentFilter filter = a -> true;

            switch (choice) {
                case "1" -> {
                    System.out.print("Username: ");
                    filter = AssignmentFilters.byUsername(scanner.nextLine());
                }
                case "2" -> {
                    System.out.print("Название роли: ");
                    filter = AssignmentFilters.byRoleName(scanner.nextLine());
                }
                case "3" -> {
                    System.out.print("Тип (PERMANENT/TEMPORARY): ");
                    filter = AssignmentFilters.byType(scanner.nextLine());
                }
                case "4" -> {
                    System.out.print("Статус (1 - активные, 2 - неактивные): ");
                    filter = scanner.nextLine().equals("1") ? AssignmentFilters.activeOnly() : AssignmentFilters.inactiveOnly();
                }
                case "5" -> {
                    System.out.print("Дата (YYYY-MM-DD): ");
                    filter = AssignmentFilters.assignedAfter(scanner.nextLine());
                }
                case "6" -> {
                    System.out.print("Дата (YYYY-MM-DD): ");
                    filter = AssignmentFilters.expiringBefore(scanner.nextLine());
                }
                default -> { System.out.println("Неверный выбор."); return; }
            }

            printAssignmentTable(system.getAssignmentManager().findByFilter(filter));
        });

        parser.registerCommand("permissions-user", "Права пользователя", (scanner, system) -> {
            System.out.print("Username: "); String un = scanner.nextLine();
            User u = system.getUserManager().findById(un).orElseThrow();
            Set<Permission> perms = system.getAssignmentManager().getUserPermissions(u);

            Map<String, List<Permission>> grouped = perms.stream()
                    .collect(Collectors.groupingBy(Permission::resource));

            grouped.forEach((res, pList) -> {
                System.out.println("Ресурс [" + res + "]: " +
                        pList.stream().map(Permission::name).toList());
            });
        });

        parser.registerCommand("permissions-check", "Проверить наличие права у пользователя", (scanner, system) -> {
            System.out.print("Введите username: ");
            String un = scanner.nextLine();

            User user = system.getUserManager().findById(un)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь '" + un + "' не найден."));

            System.out.print("Имя права (например, READ): ");
            String pName = scanner.nextLine();
            System.out.print("Ресурс (например, users): ");
            String resource = scanner.nextLine();

            boolean hasPermission = system.getAssignmentManager().userHasPermission(user, pName, resource);

            if (hasPermission) {
                System.out.println("\n[+] РЕЗУЛЬТАТ: Доступ РАЗРЕШЕН.");

                List<String> providingRoles = system.getAssignmentManager().findByUser(user).stream()
                        .filter(org.example.model.RoleAssignment::isActive)
                        .filter(a -> a.role().hasPermission(pName, resource))
                        .map(a -> a.role().getName())
                        .distinct()
                        .toList();

                System.out.println("Право предоставлено через роли: " + providingRoles);
            } else {
                System.out.println("\n[-] РЕЗУЛЬТАТ: Доступ ЗАПРЕЩЕН.");
                System.out.println("У пользователя нет активных ролей с правом " + pName.toUpperCase() + " на ресурс " + resource.toLowerCase());
            }
        });

        parser.registerCommand("help", "Справка", (scanner, system) -> parser.printHelp());

        parser.registerCommand("stats", "Статистика", (scanner, system) ->
                System.out.println(system.generateStatistics()));

        parser.registerCommand("clear", "Очистить экран", (scanner, system) -> {
            for(int i=0; i<50; i++) System.out.println();
            System.out.print("\033[H\033[2J"); System.out.flush();
        });

        parser.registerCommand("exit", "Выход", (scanner, system) -> {
            System.out.print("Выйти? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) System.exit(0);
        });

        parser.registerCommand("audit-log", "Просмотр журнала действий (аудит)", (scanner, system) -> {
            System.out.println("\nВыберите режим:");
            System.out.println("1. Показать все записи");
            System.out.println("2. Фильтр по исполнителю (кто делал)");
            System.out.println("3. Фильтр по действию");
            System.out.println("4. Сохранить лог в файл");
            System.out.print("> ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> system.getAuditLog().printLog();
                case "2" -> {
                    System.out.print("Введите имя исполнителя: ");
                    String perf = scanner.nextLine();
                    List<org.example.audit.AuditEntry> entries = system.getAuditLog().getByPerformer(perf);
                    entries.forEach(e -> System.out.println(e));
                }
                case "3" -> {
                    System.out.print("Введите действие (напр. USER_CREATE): ");
                    String act = scanner.nextLine();
                    List<org.example.audit.AuditEntry> entries = system.getAuditLog().getByAction(act);
                    entries.forEach(e -> System.out.println(e));
                }
                case "4" -> {
                    System.out.print("Введите имя файла (напр. audit.log): ");
                    String file = scanner.nextLine();
                    system.getAuditLog().saveToFile(file);
                }
                default -> System.out.println("Неверный выбор.");
            }
        });

        parser.registerCommand("report-users", "Отчёт по пользователям и их ролям", (scanner, system) -> {
            String report = system.getReportGenerator().generateUserReport(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);

            System.out.print("Сохранить в файл? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                System.out.print("Имя файла (напр. users.txt): ");
                system.getReportGenerator().exportToFile(report, scanner.nextLine());
            }
        });

        parser.registerCommand("report-roles", "Отчёт по популярности ролей", (scanner, system) -> {
            String report = system.getReportGenerator().generateRoleReport(system.getRoleManager(), system.getAssignmentManager());
            System.out.println(report);

            System.out.print("Сохранить в файл? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                System.out.print("Имя файла (напр. roles.txt): ");
                system.getReportGenerator().exportToFile(report, scanner.nextLine());
            }
        });

        parser.registerCommand("report-matrix", "Матрица доступа (Юзеры x Ресурсы)", (scanner, system) -> {
            String report = system.getReportGenerator().generatePermissionMatrix(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);

            System.out.print("Сохранить в файл? (да/нет): ");
            if (scanner.nextLine().equalsIgnoreCase("да")) {
                System.out.print("Имя файла (напр. matrix.txt): ");
                system.getReportGenerator().exportToFile(report, scanner.nextLine());
            }
        });
    }

    private static void printUserTable(List<User> users) {
        System.out.printf("%-15s | %-20s | %-25s\n", "Username", "Full Name", "Email");
        System.out.println("-".repeat(65));
        users.forEach(u -> System.out.printf("%-15s | %-20s | %-25s\n", u.username(), u.fullName(), u.email()));
    }

    private static void printAssignmentTable(List<RoleAssignment> list) {
        System.out.printf("%-12s | %-12s | %-10s | %-8s | %-16s\n", "User", "Role", "Type", "Status", "At");
        System.out.println("-".repeat(70));
        list.forEach(a -> System.out.printf("%-12s | %-12s | %-10s | %-8s | %-16s\n",
                a.user().username(), a.role().getName(), a.assignmentType(),
                (a.isActive() ? "ACTIVE" : "INACTIVE"), a.metadata().assignedAt()));
    }
}
