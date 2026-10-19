package quickchat;

import java.util.Scanner;

/**
 * Console entry point for MessageMenu: registration, login and sending messages.
 */
public class MessageMenu {

    private final Scanner scanner;
    private int messagesEntered = 0;
    private int messageLimit = 0;

    public MessageMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    /** Stand-alone test entry point; in your project call run() after a successful login. */
    public static void main(String[] args) {
        try {
            new MessageMenu(new Scanner(System.in)).run();
        } catch (IllegalStateException e) {
            System.out.println("\nInput ended - closing MessageMenu.");
        }
    }

    /** Call this once the user has logged in successfully. */
    public void run() {
        System.out.println("Welcome to QuickChat.");
        messageLimit = readPositiveInt("How many messages do you wish to enter? ");
        menuLoop();
    }

    // ---------- Part 2: sending messages ----------

    private void menuLoop() {
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            String choice = prompt("Choose an option: ");

            switch (choice) {
                case "1":
                    sendMessages();
                    break;
                case "2":
                    System.out.println("Coming Soon.");
                    break;
                case "3":
                    System.out.println("Total messages sent: " + Message.returnTotalMessages());
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option. Please choose 1, 2 or 3.");
            }
        }
    }

    private void sendMessages() {
        if (messagesEntered >= messageLimit) {
            System.out.println("You have already entered all " + messageLimit + " of your messages.");
            return;
        }

        while (messagesEntered < messageLimit) {
            System.out.println("\nMessage " + (messagesEntered + 1) + " of " + messageLimit);
            enterOneMessage();
            messagesEntered++;
        }

        System.out.println("\nAll messages have been processed.");
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
    }

    private void enterOneMessage() {
        String recipient;
        while (true) {
            recipient = prompt("Recipient cell number: ");
            String result = Message.recipientMessage(recipient);
            System.out.println(result);
            if (result.equals(Message.CELL_SUCCESS)) {
                break;
            }
        }

        String text;
        while (true) {
            text = prompt("Message (max 250 characters): ");
            String result = Message.checkMessageLength(text);
            System.out.println(result);
            if (!text.isBlank() && text.length() <= Message.MAX_LENGTH) {
                break;
            }
        }

        Message message = new Message(messagesEntered, recipient, text);
        System.out.println("Message ID generated: " + message.getMessageId());

        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        int choice = readChoice();
        String result = message.sentMessage(choice);
        System.out.println(result);

        if (choice == Message.SEND) {
            System.out.println(message.getDetails());
        } else if (choice == Message.DISREGARD) {
            while (!prompt("").equals("0")) {
                System.out.println("Press 0 to delete the message.");
            }
            System.out.println("Message deleted.");
        }
    }

    // ---------- input helpers ----------

    private String prompt(String text) {
        System.out.print(text);
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("Input ended.");
        }
        return scanner.nextLine().trim();
    }

    private int readChoice() {
        while (true) {
            String input = prompt("Choose 1, 2 or 3: ");
            if (input.equals("1") || input.equals("2") || input.equals("3")) {
                return Integer.parseInt(input);
            }
            System.out.println("Invalid option. Please choose 1, 2 or 3.");
        }
    }

    private int readPositiveInt(String text) {
        while (true) {
            String input = prompt(text);
            try {
                int value = Integer.parseInt(input);
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // fall through to the error message
            }
            System.out.println("Please enter a whole number greater than 0.");
        }
    }
}
