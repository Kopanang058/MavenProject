package chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message represents a single chat message and keeps track of every message
 * sent or stored while the program is running.
 *
 * References:
 * Bray, T. (2017) The JavaScript Object Notation (JSON) Data Interchange Format, RFC 8259.
 * Internet Engineering Task Force. Available at: https://www.rfc-editor.org/rfc/rfc8259
 * (Accessed: 19 October 2026) - used for the JSON layout and character escaping rules.
 *
 * Oracle (2024) "Class Files", Java SE 17 API Documentation. Available at:
 * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
 * (Accessed: 19 October 2026) - used for writing the JSON file.
 */
public class Message {

    public static final int MAX_MESSAGE_LENGTH = 250;

    public static final String LENGTH_OK = "Message ready to send.";
    public static final String CELL_SUCCESS = "Cell phone number successfully captured.";
    public static final String CELL_FAILURE = "Cell phone number is incorrectly formatted or does "
            + "not contain an international code. Please correct the number and try again.";
    public static final String MESSAGE_SENT = "Message successfully sent.";
    public static final String MESSAGE_DISREGARDED = "Press 0 to delete the message.";
    public static final String MESSAGE_STORED = "Message successfully stored.";
    public static final String INVALID_OPTION = "Invalid option. Please choose 1, 2 or 3.";

    // Shared across all messages while the program runs
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final Login validator = new Login();
    private static String storageFile = "storedMessages.json";

    private final String messageId;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String messageHash;

    /**
     * Creates a message with a known ID and number. Used mainly by the unit tests
     * so that the hash can be predicted.
     */
    public Message(String messageId, int messageNumber, String recipient, String text) {
        this.messageId = messageId;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
        this.messageHash = createMessageHash();
    }

    /**
     * Creates a message with an auto-generated ten-digit ID. The message number is the
     * count of messages sent so far, so the first message is number 0.
     */
    public Message(String recipient, String text) {
        this(generateMessageId(), sentMessages.size(), recipient, text);
    }

    // ---------- Validation ----------

    /**
     * Ensures that the message ID is not more than ten characters long.
     */
    public boolean checkMessageID() {
        return messageId != null && !messageId.isEmpty() && messageId.length() <= 10;
    }

    /**
     * Checks that a message is not longer than 250 characters.
     *
     * @return "Message ready to send." or how many characters the message is over by
     */
    public static String checkMessageLength(String text) {
        int length = (text == null) ? 0 : text.length();
        if (length <= MAX_MESSAGE_LENGTH) {
            return LENGTH_OK;
        }
        return "Message exceeds 250 characters by " + (length - MAX_MESSAGE_LENGTH)
                + "; please reduce the size.";
    }

    /**
     * Checks any cell phone number by reusing the Part 1 validation in Login.
     */
    public static String validateRecipientCell(String cellNumber) {
        if (validator.checkCellPhoneNumber(cellNumber)) {
            return CELL_SUCCESS;
        }
        return CELL_FAILURE;
    }

    /**
     * Checks this message's recipient number.
     */
    public String checkRecipientCell() {
        return validateRecipientCell(recipient);
    }

    // ---------- Hash and ID ----------

    /**
     * Creates the hash: first two digits of the ID, a colon, the message number,
     * a colon, then the first and last words of the message, all in capitals.
     * Example: 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String idPrefix = messageId.length() >= 2 ? messageId.substring(0, 2) : messageId;

        String[] words = text.trim().split("\\s+");
        String firstWord = stripPunctuation(words[0]);
        String lastWord = stripPunctuation(words[words.length - 1]);

        return (idPrefix + ":" + messageNumber + ":" + firstWord + lastWord)
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Removes punctuation from the start and end of a word, so "tonight?" becomes "tonight".
     */
    private static String stripPunctuation(String word) {
        return word.replaceAll("^[^A-Za-z0-9]+|[^A-Za-z0-9]+$", "");
    }

    /**
     * Generates a random ten-digit number, padded with leading zeros if needed.
     */
    public static String generateMessageId() {
        long number = ThreadLocalRandom.current().nextLong(10_000_000_000L);
        return String.format("%010d", number);
    }

    public String messageIdGenerated() {
        return "Message ID generated: " + messageId;
    }

    // ---------- Sending, storing and printing ----------

    /**
     * Carries out the option the user chose for this message.
     *
     * @param option 1 = send, 2 = disregard, 3 = store to send later
     * @return the message to display for that option
     */
    public String sentMessage(int option) {
        switch (option) {
            case 1:
                sentMessages.add(this);
                return MESSAGE_SENT;
            case 2:
                return MESSAGE_DISREGARDED;
            case 3:
                return storeMessage();
            default:
                return INVALID_OPTION;
        }
    }

    /**
     * Returns the full details of every message sent while the program is running,
     * in the order: Message ID, Message Hash, Recipient, Message.
     */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            if (i > 0) {
                output.append("\n\n");
            }
            output.append(sentMessages.get(i).getDetails());
        }
        return output.toString();
    }

    /**
     * Returns the total number of messages sent.
     */
    public static int returnTotalMessages() {
        return sentMessages.size();
    }

    /**
     * Stores this message, and every other stored message, in a JSON file.
     */
    public String storeMessage() {
        if (!storedMessages.contains(this)) {
            storedMessages.add(this);
        }

        try {
            Files.writeString(Path.of(storageFile), buildJson(), StandardCharsets.UTF_8);
            return MESSAGE_STORED;
        } catch (IOException e) {
            storedMessages.remove(this);
            return "Message could not be stored: " + e.getMessage();
        }
    }

    // ---------- JSON helpers ----------

    private static String buildJson() {
        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < storedMessages.size(); i++) {
            Message m = storedMessages.get(i);

            json.append("  {\n");
            json.append("    \"messageId\": \"").append(escapeJson(m.messageId)).append("\",\n");
            json.append("    \"messageNumber\": ").append(m.messageNumber).append(",\n");
            json.append("    \"messageHash\": \"").append(escapeJson(m.messageHash)).append("\",\n");
            json.append("    \"recipient\": \"").append(escapeJson(m.recipient)).append("\",\n");
            json.append("    \"message\": \"").append(escapeJson(m.text)).append("\"\n");
            json.append("  }");

            if (i < storedMessages.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("]\n");
        return json.toString();
    }

    /**
     * Escapes characters that are not allowed to appear raw inside a JSON string.
     */
    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }
        return escaped.toString();
    }

    // ---------- Getters and test helpers ----------

    public String getDetails() {
        return "Message ID: " + messageId
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + text;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getText() {
        return text;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public static void setStorageFile(String path) {
        storageFile = path;
    }

    /**
     * Empties the sent and stored lists. Used so each unit test starts clean.
     */
    public static void clearAll() {
        sentMessages.clear();
        storedMessages.clear();
    }
}
