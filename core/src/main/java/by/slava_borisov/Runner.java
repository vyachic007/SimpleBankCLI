package by.slava_borisov;

import by.slava_borisov.service.AccountService;
import by.slava_borisov.service.TransactionService;
import by.slava_borisov.service.UserService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Runner {
    private static Scanner scanner;
    private static AccountService accountService;
    private static TransactionService transactionService;
    private static UserService userService;

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext("by.slava_borisov")) {

            accountService = context.getBean(AccountService.class);
            transactionService = context.getBean(TransactionService.class);
            userService = context.getBean(UserService.class);

            scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit) {
                showMenu();
                try {
                    int choice = getChoice();
                    exit = processChoice(choice);
                } catch (InputMismatchException e) {
                    System.out.println("Ошибка ввода. Пожалуйста, введите номер команды (целое число).\n");
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Произошла ошибка: " + e.getMessage() + "\n");
                    scanner.nextLine();
                }
            }
        } catch (Exception e) {
            System.err.println("Критическая ошибка приложения: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private static boolean processChoice(int choice) {
        try {
            switch (choice) {
                case 1 -> createUser();
                case 2 -> showAllUsers();
                case 3 -> createAccount();
                case 4 -> closeAccount();
                case 5 -> depositAccount();
                case 6 -> transferAccount();
                case 7 -> withDrawAccount();
                case 8 -> showAllTransactions();
                case 9 -> {
                    System.out.println("Выход из программы...");
                    return true;
                }
                default -> System.out.println("Неверный выбор. Пожалуйста, выберите номер от 1 до 8.\n");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при выполнении операции: " + e.getMessage() + "\n");
        }
        return false;
    }


    private static void showAllTransactions() {
        System.out.println("---ВЫВОД ВСЕХ ТРАНЗАКЦИЙ---\n");
        transactionService.showAllTransaction();
        System.out.println();
    }


    private static void withDrawAccount() {
        System.out.println("---СНЯТЬ СУММУ СО СЧЁТА---\n");

        try {
            System.out.println("Введите номер счёта для снятия: ");
            Long accountId = scanner.nextLong();

            System.out.println("Введите сумму: ");
            BigDecimal amount = scanner.nextBigDecimal();

            accountService.accountWithDraw(accountId, amount);
        } catch (InputMismatchException e) {
            System.out.println("Ошибка ввода. Номер счёта и сумма должны быть числами.\n");
            scanner.nextLine();
        }
        System.out.println();
    }

    private static void transferAccount() {
        System.out.println("---ОТПРАВИТЬ ДЕНЬГИ---\n");

        try {
            System.out.println("Введите номер счёта отправителя: ");
            Long senderAccountId = scanner.nextLong();

            System.out.println("Введите номер счёта получателя: ");
            Long recipientAccountId = scanner.nextLong();

            System.out.println("Введите сумму: ");
            BigDecimal amount = scanner.nextBigDecimal();

            accountService.accountTransfer(senderAccountId, recipientAccountId, amount);
        } catch (InputMismatchException e) {
            System.out.println("Ошибка ввода. Номера счетов и сумма должны быть числами.\n");
            scanner.nextLine();
        }
        System.out.println();
    }

    private static void depositAccount() {
        System.out.println("---ПОПОЛНИТЬ СЧЁТ---\n");
        try {
            System.out.println("Введите номер счёта для пополнения: ");
            Long accountId = scanner.nextLong();

            System.out.println("Введите сумму: ");
            BigDecimal amount = scanner.nextBigDecimal();

            accountService.accountDeposit(accountId, amount);
        } catch (InputMismatchException e) {
            System.out.println("Ошибка ввода. Номер счёта и сумма должны быть числами.\n");
            scanner.nextLine();
        }
        System.out.println();
    }

    private static void closeAccount() {
        System.out.println("---ЗАКРЫТИЕ СЧЁТА---\n");
        try {
            System.out.println("Введите номер счёта: ");
            Long accountId = scanner.nextLong();
            accountService.closeAccount(accountId);
        } catch (InputMismatchException e) {
            System.out.println("Ошибка ввода. Номер счёта должен быть числом.\n");
            scanner.nextLine();
        }
        System.out.println();
    }

    private static void createAccount() {
        System.out.println("---СОЗДАНИЕ СЧЁТА---\n");
        try {
            System.out.println("Введите ID пользователя: ");
            Long id = scanner.nextLong();
            accountService.createAccount(id);
        } catch (InputMismatchException e) {
            System.out.println("Ошибка ввода. ID пользователя должен быть числом.\n");
            scanner.nextLine();
        }
        System.out.println();
    }

    private static void showAllUsers() {
        System.out.println("---ВЫВОД ВСЕХ ПОЛЬЗОВАТЕЛЕЙ---\n");
        userService.showAllUsers();
        System.out.println();
    }

    private static void createUser() {
        System.out.println("---СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ---\n");
        System.out.println("Введите логин: ");
        String login = scanner.nextLine().trim();
        if (login.isEmpty()) {
            System.out.println("Логин не может быть пустым.\n");
            return;
        }
        userService.createUser(login);
        System.out.println();
    }


    private static int getChoice() {
        System.out.print("Введите номер команды: ");
        int choice = scanner.nextInt();
        System.out.println("-------------------------------\n");
        scanner.nextLine();
        return choice;
    }

    private static void showMenu() {
        System.out.println("------------МЕНЮ------------");
        System.out.println("1. Создать пользователя.");
        System.out.println("2. Вывести всех пользователей.");
        System.out.println("3. Создать счёт.");
        System.out.println("4. Закрыть счёт");
        System.out.println("5. Внести деньги на счёт");
        System.out.println("6. Отправить деньги");
        System.out.println("7. Снять деньги");
        System.out.println("8. Вывести все транзакции.");
        System.out.println("9. Выход");
        System.out.println("-------------------------------");
    }
}