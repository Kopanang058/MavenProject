package quickchat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * A single QuickChat message (Part 2). Static lists keep track of every message
 * sent or stored while the program is running.
 */
public class Message {

    public static final int MAX_LENGTH = 250;
    public static final int SEND = 1;
    public static final int DISREGARD = 2;
    public static final int STORE = 3;
    public static final Path DEFAULT_STORAGE_FILE = Path.of("storedMessages.json");

    public static final String CELL_SUCCESS = "Cell phone number successfully captured.";
    public static final String CELL_FAILURE = "Cell phone number is incorrectly formatted or does not "
            + "contain an international code. Please correct the number and try again.";

    /*
     * Same rule as the cell number check in Part 1: "+", a country code of 1-3
     * digits, then no more than ten digits.
     * Reference: ITU-T Recommendation E.164 (international numbering plan).
     * TODO: replace with the source you researched. If your Part 1 Login class
     * already has this check, call that method from isValidCellNumber() instead.
     */
    private static final Pattern CELL_PATTERN = Pattern.compile("^\\+[1-9]\\d{0,2}\\d{1,10}$");

    private static Path storageFile = DEFAULT_STORAGE_FILE;
    private static final List<Message> SENT_MESSAGES = new ArrayList<>();
    private static final List<Message> STORED_MESSAGES = new ArrayList<>();

    private final String messageId;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String messageHash;

    /** Creates a message with an auto-generated ten-digit ID. */
    public Message(int messageNumber, String recipient, String text) {
        this(generateMessageId(), messageNumber, recipient, text);
    }

    /** Creates a message with a known ID (used by the unit tests). */
    public Message(String messageId, int messageNumber, String recipient, String text) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    /** Randomly generates a ten-digit number (leading zeros are kept). */
    public static String generateMessageId() {
        long id = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", id);
    }

    /** The message ID must be no more than ten characters long. */
    public boolean checkMessageID() {
        return messageId != null && messageId.matches("\\d{1,10}");
    }

    /** Validates the recipient cell number and returns the matching message. */
    public String checkRecipientCell() {
        return recipientMessage(recipient);
    }

    /** Same check for a number that has not been put into a Message yet. */
    public static String recipientMessage(String number) {
        return isValidCellNumber(number) ? CELL_SUCCESS : CELL_FAILURE;
    }

    public static boolean isValidCellNumber(String number) {
        return number != null && CELL_PATTERN.matcher(number).matches();
    }

    /** Returns the error or success message for the message length. */
    public static String checkMessageLength(String text) {
        if (text == null || text.length() > MAX_LENGTH) {
            return "Please enter a message of less than 250 characters.";
        }
        return "Message sent";
    }

    /**
     * Hash = first two digits of the ID : message number : first and last words,
     * in capitals. Punctuation is stripped from the words, e.g. 00:0:HITONIGHT.
     */
    public String createMessageHash() {
        String[] words = text.trim().split("\\s+");
        String first = words[0].replaceAll("[^A-Za-z0-9]", "");
        String last = words[words.length - 1].replaceAll("[^A-Za-z0-9]", "");
        String prefix = messageId.length() >= 2 ? messageId.substring(0, 2) : messageId;
        return (prefix + ":" + messageNumber + ":" + first + last).toUpperCase(Locale.ROOT);
    }

    /**
     * Carries out the user's choice: 1 = send, 2 = disregard, 3 = store.
     * Returns the matching system message.
     */
    public String sentMessage(int choice) {
        switch (choice) {
            case SEND:
                SENT_MESSAGES.add(this);
                return "Message successfully sent.";
            case DISREGARD:
                return "Press 0 to delete the message.";
            case STORE:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option. Please choose 1, 2 or 3.";
        }
    }

    /** Returns every message sent while the program has been running. */
    public static String printMessages() {
        StringBuilder sb = new StringBuilder();
        for (Message m : SENT_MESSAGES) {
            sb.append(m.getDetails()).append(System.lineSeparator());
        }
        return sb.toString();
    }

    /** Returns the total number of messages sent. */
    public static int returnTotalMessages() {
        return SENT_MESSAGES.size();
    }

    /** Message ID, Message Hash, Recipient, Message - in that order. */
    public String getDetails() {
        return "Message ID: " + messageId
                + System.lineSeparator() + "Message Hash: " + messageHash
                + System.lineSeparator() + "Recipient: " + recipient
                + System.lineSeparator() + "Message: " + text
                + System.lineSeparator();
    }

    /** Stores this message in storedMessages.json. */
    public void storeMessage() {
        storeMessage(storageFile);
    }

    /** Changes where stored messages are written (used by the unit tests). */
    public static void setStorageFile(Path file) {
        storageFile = file;
    }

    /**
     * Adds this message to the stored list and rewrites the JSON file.
     * The JSON is built by hand so the project needs no extra libraries.
     */
    public void storeMessage(Path file) {
        STORED_MESSAGES.add(this);
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < STORED_MESSAGES.size(); i++) {
            json.append(STORED_MESSAGES.get(i).toJson());
            json.append(i < STORED_MESSAGES.size() - 1 ? ",\n" : "\n");
        }
        json.append("]\n");
        try {
            Files.writeString(file, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not write to " + file + ": " + e.getMessage());
        }
    }

    private String toJson() {
        return "  {\n"
                + "    \"messageID\": \"" + escape(messageId) + "\",\n"
                + "    \"messageHash\": \"" + escape(messageHash) + "\",\n"
                + "    \"recipient\": \"" + escape(recipient) + "\",\n"
                + "    \"message\": \"" + escape(text) + "\"\n"
                + "  }";
    }

    private static String escape(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /** Clears the static lists so each unit test starts clean. */
    public static void resetState() {
        SENT_MESSAGES.clear();
        STORED_MESSAGES.clear();
        storageFile = DEFAULT_STORAGE_FILE;
    }

    public String getMessageId() { return messageId; }
    public int getMessageNumber() { return messageNumber; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
    public String getMessageHash() { return messageHash; }
}
