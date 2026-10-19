package chatapp;

import java.util.Scanner;

/**
 * Console entry point for Part 1 of the PoE.
 * No GUI components are used anywhere in this application.
 */
public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Chat Application: Registration ===");

        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();

        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        Login login = new Login(firstName, lastName);

        String username = captureUsername(scanner, login);
        String password = capturePassword(scanner, login);
        String cellPhoneNumber = captureCellPhoneNumber(scanner, login);

        System.out.println();
        System.out.println(login.registerUser(username, password, cellPhoneNumber));

        System.out.println();
        System.out.println("=== Chat Application: Login ===");

        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Username: ");
            String loginUsername = scanner.nextLine();

            System.out.print("Password: ");
            String loginPassword = scanner.nextLine();

            System.out.println(login.returnLoginStatus(loginUsername, loginPassword));
            loggedIn = login.loginUser(loginUsername, loginPassword);
        }

        new QuickChat(login, scanner).start();
        scanner.close();
    }

    private static String captureUsername(Scanner scanner, Login login) {
        while (true) {
            System.out.print("Enter a username: ");
            String username = scanner.nextLine();

            if (login.checkUserName(username)) {
                System.out.println("Username successfully captured.");
                return username;
            }

            System.out.println("Username is not correctly formatted; please ensure that your "
                    + "username contains an underscore and is no more than five characters "
                    + "in length.");
        }
    }

    private static String capturePassword(Scanner scanner, Login login) {
        while (true) {
            System.out.print("Enter a password: ");
            String password = scanner.nextLine();

            if (login.checkPasswordComplexity(password)) {
                System.out.println("Password successfully captured.");
                return password;
            }

            System.out.println("Password is not correctly formatted; please ensure that the "
                    + "password contains at least eight characters, a capital letter, a number, "
                    + "and a special character.");
        }
    }

    private static String captureCellPhoneNumber(Scanner scanner, Login login) {
        while (true) {
            System.out.print("Enter your cell phone number (e.g. +27838968976): ");
            String cellPhoneNumber = scanner.nextLine();

            if (login.checkCellPhoneNumber(cellPhoneNumber)) {
                System.out.println("Cell phone number successfully added.");
                return cellPhoneNumber;
            }

            System.out.println("Cell phone number incorrectly formatted or does not contain "
                    + "international code.");
        }
    }
}
