package chatapp;

import java.util.Scanner;

/**
 * QuickChat is the console menu for Part 2. It only runs for a user who has
 * logged in successfully. No GUI components are used.
 */
public class QuickChat {

    private final Login login;
    private final Scanner scanner;

    public QuickChat(Login login, Scanner scanner) {
        this.login = login;
        this.scanner = scanner;
    }

    public void start() {
        if (!login.isLoggedIn()) {
            System.out.println("You must log in successfully before you can send messages.");
            return;
        }

        System.out.println();
        System.out.println("Welcome to QuickChat.");

        int maxMessages = readInt("How many messages do you wish to enter? ", 1);
        int messagesEntered = 0;
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");

            int choice = readInt("Choose an option: ", 1);

            switch (choice) {
                case 1:
                    messagesEntered = sendMessages(maxMessages, messagesEntered);
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1, 2 or 3.");
            }
        }

        System.out.println();
        System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
        System.out.println("Goodbye.");
    }

    /**
     * Lets the user enter messages until the number they chose at the start is reached.
     *
     * @return how many messages have been entered so far
     */
    private int sendMessages(int maxMessages, int messagesEntered) {
        if (messagesEntered >= maxMessages) {
            System.out.println("You have already entered all " + maxMessages + " of your messages.");
            return messagesEntered;
        }

        while (messagesEntered < maxMessages) {
            System.out.println();
            System.out.println("Message " + (messagesEntered + 1) + " of " + maxMessages);
            enterOneMessage();
            messagesEntered++;
        }

        System.out.println();
        System.out.println("All " + maxMessages + " messages have been entered.");
        System.out.println("Total number of messages sent: " + Message.returnTotalMessages());
        return messagesEntered;
    }

    private void enterOneMessage() {
        String recipient = captureRecipient();
        String text = captureMessageText();

        Message message = new Message(recipient, text);
        System.out.println(message.messageIdGenerated());

        int option = 0;
        while (option < 1 || option > 3) {
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            option = readInt("Choose an option: ", 1);
        }

        System.out.println(message.sentMessage(option));

        if (option == 1) {
            System.out.println();
            System.out.println(message.getDetails());
        } else if (option == 2) {
            String input = "";
            while (!input.equals("0")) {
                input = readLine("");
            }
            System.out.println("Message deleted.");
        }
    }

    private String captureRecipient() {
        while (true) {
            String recipient = readLine("Enter the recipient's cell number (e.g. +27718693002): ");
            String result = Message.validateRecipientCell(recipient);
            System.out.println(result);

            if (result.equals(Message.CELL_SUCCESS)) {
                return recipient.trim();
            }
        }
    }

    private String captureMessageText() {
        while (true) {
            String text = readLine("Enter your message (maximum 250 characters): ");

            if (text.isBlank()) {
                System.out.println("A message cannot be empty.");
                continue;
            }

            String result = Message.checkMessageLength(text);
            if (result.equals(Message.LENGTH_OK)) {
                System.out.println(result);
                return text;
            }

            System.out.println("Please enter a message of less than 250 characters.");
            System.out.println(result);
        }
    }

    private String readLine(String prompt) {
        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }
        return scanner.nextLine().trim();
    }

    /**
     * Keeps asking until the user types a whole number that is at least the minimum.
     * This stops the program crashing if someone types letters.
     */
    private int readInt(String prompt, int minimum) {
        while (true) {
            String input = readLine(prompt);
            try {
                int value = Integer.parseInt(input);
                if (value >= minimum) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through and ask again
            }
            System.out.println("Please enter a whole number of " + minimum + " or more.");
        }
    }
}
