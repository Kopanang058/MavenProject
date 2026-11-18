package chatapp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message represents a single chat message and keeps track of every message
 * sent, disregarded or stored while the program is running.
 *
 * References:
 * Bray, T. (2017) The JavaScript Object Notation (JSON) Data Interchange Format, RFC 8259.
 * Internet Engineering Task Force. Available at: https://www.rfc-editor.org/rfc/rfc8259
 * (Accessed: 19 October 2026) - used for the JSON layout and character escaping rules.
 *
 * Oracle (2024) "Class Files", Java SE 17 API Documentation. Available at:
 * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
 * (Accessed: 19 October 2026) - used for writing and reading the JSON file.
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
    public static final String NO_STORED_MESSAGES = "No stored messages.";

    // The arrays (resizable lists) that are filled while the program runs
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIds = new ArrayList<>();

    private static final Login validator = new Login();
    private static String storageFile = "storedMessages.json";

    private final String messageId;
    private final int messageNumber;
    private final String recipient;
    private final String text;
    private final String messageHash;

    /**
     * Creates a message with a known ID and number. Used mainly by the unit tests
     * and when stored messages are read back from the JSON file.
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
     * Words made only of punctuation (such as a lone "!") are skipped.
     * Example: 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String idPrefix = messageId.length() >= 2 ? messageId.substring(0, 2) : messageId;

        String firstWord = "";
        String lastWord = "";

        String[] words = text.trim().split("\\s+");
        for (String word : words) {
            String cleaned = stripPunctuation(word);
            if (!cleaned.isEmpty()) {
                if (firstWord.isEmpty()) {
                    firstWord = cleaned;
                }
                lastWord = cleaned;
            }
        }

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
     * Carries out the option the user chose for this message and records it in the
     * matching array (sent, disregarded or stored).
     *
     * @param option 1 = send, 2 = disregard, 3 = store to send later
     * @return the message to display for that option
     */
    public String sentMessage(int option) {
        switch (option) {
            case 1:
                sentMessages.add(this);
                recordIdAndHash();
                return MESSAGE_SENT;
            case 2:
                disregardedMessages.add(this);
                recordIdAndHash();
                return MESSAGE_DISREGARDED;
            case 3:
                return storeMessage();
            default:
                return INVALID_OPTION;
        }
    }

    /**
     * Adds this message's ID and hash to the two matching arrays. The two arrays stay
     * in step, so position 3 in one belongs to the same message as position 3 in the other.
     */
    private void recordIdAndHash() {
        if (!messageIds.contains(messageId)) {
            messageIds.add(messageId);
            messageHashes.add(messageHash);
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
            writeStoredFile();
            recordIdAndHash();
            return MESSAGE_STORED;
        } catch (IOException e) {
            storedMessages.remove(this);
            return "Message could not be stored: " + e.getMessage();
        }
    }

    // ---------- Reading the JSON file back in ----------

    /**
     * Reads the JSON file into the stored messages array. Any messages already in
     * that array are replaced by what is in the file.
     *
     * @return how many messages were loaded, or 0 if the file is missing or unreadable
     */
    public static int loadStoredMessages() {
        Path path = Path.of(storageFile);
        if (!Files.exists(path)) {
            return 0;
        }

        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            List<Map<String, String>> records = new JsonReader(json).readArrayOfObjects();

            List<Message> loaded = new ArrayList<>();
            for (Map<String, String> record : records) {
                loaded.add(new Message(
                        requireField(record, "messageId"),
                        Integer.parseInt(requireField(record, "messageNumber")),
                        requireField(record, "recipient"),
                        requireField(record, "message")));
            }

            storedMessages.clear();
            for (Message message : loaded) {
                storedMessages.add(message);
                message.recordIdAndHash();
            }
            return loaded.size();
        } catch (IOException | IllegalArgumentException e) {
            return 0;
        }
    }

    private static String requireField(Map<String, String> record, String key) {
        String value = record.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing field: " + key);
        }
        return value;
    }

    // ---------- Stored messages menu operations ----------

    /**
     * Shows the sender and recipient of every stored message.
     */
    public static String displaySenderAndRecipient(String sender) {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < storedMessages.size(); i++) {
            if (i > 0) {
                output.append("\n");
            }
            output.append("Sender: ").append(sender)
                    .append(", Recipient: ").append(storedMessages.get(i).recipient);
        }
        return output.toString();
    }

    /**
     * Finds the stored message with the most characters. If two are equally long,
     * the one stored first is shown.
     */
    public static String displayLongestMessage() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }

        Message longest = storedMessages.get(0);
        for (int i = 1; i < storedMessages.size(); i++) {
            if (storedMessages.get(i).text.length() > longest.text.length()) {
                longest = storedMessages.get(i);
            }
        }
        return "Longest stored message: " + longest.text;
    }

    /**
     * Searches the stored messages for a message ID and shows its recipient and message.
     */
    public static String searchByMessageId(String id) {
        if (id != null) {
            for (Message message : storedMessages) {
                if (message.messageId.equals(id.trim())) {
                    return "Recipient: " + message.recipient + "\nMessage: " + message.text;
                }
            }
        }
        return "Message ID not found.";
    }

    /**
     * Shows every stored message for one recipient.
     */
    public static String searchByRecipient(String recipient) {
        StringBuilder output = new StringBuilder();

        if (recipient != null) {
            for (Message message : storedMessages) {
                if (message.recipient.equals(recipient.trim())) {
                    if (output.length() > 0) {
                        output.append("\n");
                    }
                    output.append("Message: ").append(message.text);
                }
            }
        }

        if (output.length() == 0) {
            return "No stored messages found for that recipient.";
        }
        return output.toString();
    }

    /**
     * Deletes the stored message with the given hash, and updates the JSON file.
     */
    public static String deleteByHash(String hash) {
        if (hash != null) {
            for (int i = 0; i < storedMessages.size(); i++) {
                Message message = storedMessages.get(i);

                if (message.messageHash.equalsIgnoreCase(hash.trim())) {
                    storedMessages.remove(i);

                    try {
                        writeStoredFile();
                    } catch (IOException e) {
                        storedMessages.add(i, message);
                        return "Message could not be deleted: " + e.getMessage();
                    }

                    int index = messageIds.indexOf(message.messageId);
                    if (index >= 0) {
                        messageIds.remove(index);
                        messageHashes.remove(index);
                    }
                    return "Message \"" + message.text + "\" successfully deleted.";
                }
            }
        }
        return "Message hash not found.";
    }

    /**
     * Builds a report with the full details of every stored message.
     */
    public static String displayReport() {
        if (storedMessages.isEmpty()) {
            return NO_STORED_MESSAGES;
        }

        StringBuilder report = new StringBuilder("Stored Messages Report");
        for (Message message : storedMessages) {
            report.append("\n\n").append(message.getDetails());
        }
        return report.toString();
    }

    // ---------- JSON writing ----------

    private static void writeStoredFile() throws IOException {
        Files.writeString(Path.of(storageFile), buildJson(), StandardCharsets.UTF_8);
    }

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

    public static List<Message> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public static List<Message> getDisregardedMessages() {
        return new ArrayList<>(disregardedMessages);
    }

    public static List<Message> getStoredMessages() {
        return new ArrayList<>(storedMessages);
    }

    public static List<String> getMessageHashes() {
        return new ArrayList<>(messageHashes);
    }

    public static List<String> getMessageIds() {
        return new ArrayList<>(messageIds);
    }

    public static void setStorageFile(String path) {
        storageFile = path;
    }

    /**
     * Empties every array. Used so each unit test starts clean.
     */
    public static void clearAll() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageHashes.clear();
        messageIds.clear();
    }
}
