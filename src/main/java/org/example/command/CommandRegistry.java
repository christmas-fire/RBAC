package org.example.command;

import org.example.filter.*;
import org.example.model.*;
import org.example.util.ConsoleUtils;
import org.example.util.DateUtils;
import org.example.util.FormatUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CommandRegistry {
    public static void registerAll(CommandParser parser) {
        parser.registerCommand("user-list", "Список всех пользователей", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Список пользователей"));
            List<User> users = system.getUserManager().findAll();
            printUserTable(users);
        });

        parser.registerCommand("user-create", "Создать пользователя", (scanner, system) -> {
            String un = ConsoleUtils.promptString(scanner, "Введите username", true);
            String fn = ConsoleUtils.promptString(scanner, "Введите полное имя", true);
            String em = ConsoleUtils.promptString(scanner, "Введите email", true);

            try {
                User user = User.validate(un, fn, em);
                system.getUserManager().add(user);
                system.getAuditLog().log("USER_CREATE", system.getCurrentUser(), un, "Успех");
                System.out.println("Пользователь успешно создан.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "Детальная информация о пользователе", (scanner, system) -> {
            String un = ConsoleUtils.promptString(scanner, "Введите username", true);
            system.getUserManager().findById(un).ifPresentOrElse(u -> {
                System.out.println(FormatUtils.formatHeader("Профиль: " + u.username()));
                System.out.println(u.format());

                List<RoleAssignment> as = system.getAssignmentManager().findByUser(u);
                String roles = as.stream().filter(RoleAssignment::isActive)
                        .map(a -> a.role().getName()).collect(Collectors.joining(", "));

                System.out.println("Активные роли: " + (roles.isEmpty() ? "нет" : roles));
                System.out.println("Доступные права: " + system.getAssignmentManager().getUserPermissions(u));
            }, () -> System.out.println("Ошибка: Пользователь не найден."));
        });

        parser.registerCommand("user-update", "Обновить данные пользователя", (scanner, system) -> {
            String un = ConsoleUtils.promptString(scanner, "Введите username пользователя", true);
            if (!system.getUserManager().exists(un)) {
                System.out.println("Ошибка: Пользователь не найден.");
                return;
            }
            String fn = ConsoleUtils.promptString(scanner, "Новое полное имя", true);
            String em = ConsoleUtils.promptString(scanner, "Новый email", true);

            try {
                system.getUserManager().update(un, fn, em);
                system.getAuditLog().log("USER_UPDATE", system.getCurrentUser(), un, "Обновлены данные");
                System.out.println("Данные успешно обновлены.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Удалить пользователя", (scanner, system) -> {
            String un = ConsoleUtils.promptString(scanner, "Username для удаления", true);
            if (ConsoleUtils.promptYesNo(scanner, "Вы уверены, что хотите удалить пользователя " + un + " и все его роли?")) {
                User user = system.getUserManager().findById(un).orElseThrow();
                system.getAssignmentManager().findByUser(user).forEach(a -> system.getAssignmentManager().remove(a));
                system.getUserManager().remove(user);
                system.getAuditLog().log("USER_DELETE", system.getCurrentUser(), un, "Удален из системы");
                System.out.println("Пользователь удален.");
            }
        });

        parser.registerCommand("user-search", "Поиск пользователей по фильтрам", (scanner, system) -> {
            List<String> criteria = List.of("Username (содержит)", "Email (содержит)", "Домен email", "Полное имя (содержит)");
            String choice = ConsoleUtils.promptChoice(scanner, "Выберите критерий поиска", criteria);
            String query = ConsoleUtils.promptString(scanner, "Введите поисковый запрос", true);

            UserFilter filter = switch (criteria.indexOf(choice)) {
                case 0 -> UserFilters.byUsernameContains(query);
                case 1 -> u -> u.email().contains(query);
                case 2 -> UserFilters.byEmailDomain(query);
                case 3 -> UserFilters.byFullNameContains(query);
                default -> u -> true;
            };
            printUserTable(system.getUserManager().findByFilter(filter));
        });

        parser.registerCommand("role-list", "Список всех ролей", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Список ролей"));
            printRoleTable(system.getRoleManager().findAll());
        });

        parser.registerCommand("role-create", "Создать новую роль", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Название роли", true);
            String desc = ConsoleUtils.promptString(scanner, "Описание роли", true);
            Role role = new Role(name, desc);

            while (ConsoleUtils.promptYesNo(scanner, "Добавить право к этой роли?")) {
                String pName = ConsoleUtils.promptString(scanner, "Имя права (напр. READ)", true);
                String res = ConsoleUtils.promptString(scanner, "Ресурс (напр. users)", true);
                String pDesc = ConsoleUtils.promptString(scanner, "Описание", true);
                role.addPermission(new Permission(pName, res, pDesc));
            }

            system.getRoleManager().add(role);
            system.getAuditLog().log("ROLE_CREATE", system.getCurrentUser(), name, "Создана новая роль");
            System.out.println("Роль успешно создана.");
        });

        parser.registerCommand("role-view", "Просмотр роли", (scanner, system) -> {
            String name = ConsoleUtils.promptString(scanner, "Имя роли", true);
            system.getRoleManager().findByName(name).ifPresentOrElse(
                    r -> {
                        System.out.println(FormatUtils.formatBox("РОЛЬ: " + r.getName()));
                        System.out.println(r.format());
                    },
                    () -> System.out.println("Роль не найдена.")
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
            String name = ConsoleUtils.promptString(scanner, "Имя роли для удаления", true);
            Role role = system.getRoleManager().findByName(name).orElseThrow();

            List<RoleAssignment> active = system.getAssignmentManager().findByRole(role).stream()
                    .filter(RoleAssignment::isActive).toList();

            if (!active.isEmpty()) {
                System.out.println("ВНИМАНИЕ! Роль назначена активным пользователям: " +
                        active.stream().map(a -> a.user().username()).collect(Collectors.joining(", ")));
            }

            if (ConsoleUtils.promptYesNo(scanner, "Подтверждаете удаление роли " + name + "?")) {
                system.getRoleManager().remove(role);
                system.getAuditLog().log("ROLE_DELETE", system.getCurrentUser(), name, "Удалена");
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

        parser.registerCommand("assign-role", "Назначить роль пользователю", (scanner, system) -> {
            String un = ConsoleUtils.promptString(scanner, "Username пользователя", true);
            User user = system.getUserManager().findById(un).orElseThrow();

            Role role = ConsoleUtils.promptChoice(scanner, "Выберите роль", system.getRoleManager().findAll());
            List<String> types = List.of("Постоянное", "Временное");
            String type = ConsoleUtils.promptChoice(scanner, "Тип назначения", types);

            String reason = ConsoleUtils.promptString(scanner, "Причина назначения", false);
            AssignmentMetadata meta = AssignmentMetadata.now(system.getCurrentUser(), reason);

            try {
                if (type.equals("Временное")) {
                    String date = ConsoleUtils.promptString(scanner, "Дата истечения (YYYY-MM-DD HH:mm)", true);
                    system.getAssignmentManager().add(new TemporaryAssignment(user, role, meta, date, false));
                } else {
                    system.getAssignmentManager().add(new PermanentAssignment(user, role, meta));
                }
                system.getAuditLog().log("ASSIGN_ROLE", system.getCurrentUser(), un, "Назначена роль " + role.getName());
                System.out.println("Успешно назначено.");
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-list", "Список всех назначений", (scanner, system) -> {
            System.out.println(FormatUtils.formatHeader("Все назначения системы"));
            printAssignmentTable(system.getAssignmentManager().findAll());
        });

        parser.registerCommand("revoke-role", "Отозвать роль у пользователя", (scanner, system) -> {
            String un = ConsoleUtils.promptString(scanner, "Username", true);
            User user = system.getUserManager().findById(un).orElseThrow();

            List<RoleAssignment> active = system.getAssignmentManager().findByUser(user)
                    .stream().filter(RoleAssignment::isActive).toList();

            if (active.isEmpty()) {
                System.out.println("У пользователя нет активных ролей.");
                return;
            }

            RoleAssignment toRevoke = ConsoleUtils.promptChoice(scanner, "Выберите назначение для отзыва", active);
            system.getAssignmentManager().revokeAssignment(toRevoke.assignmentId());
            system.getAuditLog().log("REVOKE_ROLE", system.getCurrentUser(), un, "Отозвана роль " + toRevoke.role().getName());
            System.out.println("Роль успешно отозвана.");
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

        parser.registerCommand("help", "Справка по командам", (scanner, system) -> parser.printHelp());

        parser.registerCommand("stats", "Статистика системы", (scanner, system) -> {
            System.out.println(FormatUtils.formatBox("СИСТЕМНАЯ СТАТИСТИКА"));
            System.out.println(system.generateStatistics());
        });

        parser.registerCommand("clear", "Очистить консоль", (scanner, system) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        });

        parser.registerCommand("exit", "Выход из программы", (scanner, system) -> {
            if (ConsoleUtils.promptYesNo(scanner, "Вы действительно хотите выйти?")) {
                System.out.println("Завершение работы...");
                System.exit(0);
            }
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

        parser.registerCommand("report-users", "Отчет по пользователям", (scanner, system) -> {
            String report = system.getReportGenerator().generateUserReport(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);
            if (ConsoleUtils.promptYesNo(scanner, "Сохранить этот отчет в файл?")) {
                String file = ConsoleUtils.promptString(scanner, "Имя файла", true);
                system.getReportGenerator().exportToFile(report, file);
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

        parser.registerCommand("report-matrix", "Матрица доступа", (scanner, system) -> {
            String report = system.getReportGenerator().generatePermissionMatrix(system.getUserManager(), system.getAssignmentManager());
            System.out.println(report);
        });
    }

    private static void printUserTable(List<User> users) {
        String[] headers = {"Username", "Full Name", "Email"};
        List<String[]> rows = users.stream()
                .map(u -> new String[]{u.username(), u.fullName(), u.email()})
                .toList();
        System.out.println(FormatUtils.formatTable(headers, rows));
    }

    private static void printRoleTable(List<Role> roles) {
        String[] headers = {"Role Name", "Perms Count", "Internal ID"};
        List<String[]> rows = roles.stream()
                .map(r -> new String[]{r.getName(), String.valueOf(r.getPermissions().size()), r.getId()})
                .toList();
        System.out.println(FormatUtils.formatTable(headers, rows));
    }

    private static void printAssignmentTable(List<RoleAssignment> assignments) {
        String[] headers = {"USER", "ROLE", "TYPE", "STATUS", "REMAINING / AGO"};

        List<String[]> rows = assignments.stream()
                .map(a -> {
                    String timeInfo;
                    if (a instanceof TemporaryAssignment ta) {
                        timeInfo = ta.getTimeRemaining();
                    } else {
                        timeInfo = DateUtils.formatRelativeTime(a.metadata().assignedAt());
                    }

                    return new String[]{
                            a.user().username(),
                            a.role().getName(),
                            a.assignmentType(),
                            (a.isActive() ? "ACTIVE" : "INACTIVE"),
                            timeInfo
                    };
                })
                .collect(Collectors.toList());

        System.out.println(FormatUtils.formatTable(headers, rows));
    }
}
