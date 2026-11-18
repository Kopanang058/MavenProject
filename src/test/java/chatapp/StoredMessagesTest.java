package chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Part 3 unit tests. The five messages below are the test data from the brief:
 * 1 = Sent, 2 = Stored, 3 = Disregard, 4 = Sent, 5 = Stored.
 */
class StoredMessagesTest {

    private static final String SENDER = "Kyle Smith";

    private static final String TEXT_1 = "Did you get the cake?";
    private static final String TEXT_2 = "Where are you? You are late! I have asked you to be on time.";
    private static final String TEXT_3 = "Yohoooo, I am at your gate.";
    private static final String TEXT_4 = "It is dinner time !";
    private static final String TEXT_5 = "Ok, I am leaving without you.";

    @TempDir
    Path tempDir;

    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @BeforeEach
    void setUp() {
        Message.clearAll();
        Message.setStorageFile(tempDir.resolve("storedMessages.json").toString());

        message1 = new Message("0011111111", 0, "+27834557896", TEXT_1);
        message2 = new Message("0122222222", 1, "+27838884567", TEXT_2);
        message3 = new Message("0233333333", 2, "+27834484567", TEXT_3);
        message4 = new Message("0344444444", 3, "0838884567", TEXT_4);
        message5 = new Message("0455555555", 4, "+27838884567", TEXT_5);

        // Fill the arrays the same way the menu does
        message1.sentMessage(1);
        message2.sentMessage(3);
        message3.sentMessage(2);
        message4.sentMessage(1);
        message5.sentMessage(3);
    }

    // ---------- Populating the arrays ----------

    @Test
    @DisplayName("Sent messages array holds Message 1 and Message 4")
    void sentMessagesArray() {
        List<Message> sent = Message.getSentMessages();

        assertEquals(2, sent.size());
        assertEquals(TEXT_1, sent.get(0).getText());
        assertEquals(TEXT_4, sent.get(1).getText());
    }

    @Test
    @DisplayName("Disregarded messages array holds Message 3")
    void disregardedMessagesArray() {
        List<Message> disregarded = Message.getDisregardedMessages();

        assertEquals(1, disregarded.size());
        assertEquals(TEXT_3, disregarded.get(0).getText());
    }

    @Test
    @DisplayName("Stored messages array holds Message 2 and Message 5")
    void storedMessagesArray() {
        List<Message> stored = Message.getStoredMessages();

        assertEquals(2, stored.size());
        assertEquals(TEXT_2, stored.get(0).getText());
        assertEquals(TEXT_5, stored.get(1).getText());
    }

    @Test
    @DisplayName("Message hash array holds all five hashes")
    void messageHashArray() {
        List<String> expected = List.of("00:0:DIDCAKE", "01:1:WHERETIME", "02:2:YOHOOOOGATE",
                "03:3:ITTIME", "04:4:OKYOU");

        assertEquals(expected, Message.getMessageHashes());
    }

    @Test
    @DisplayName("Message ID array holds all five IDs")
    void messageIdArray() {
        List<String> expected = List.of("0011111111", "0122222222", "0233333333",
                "0344444444", "0455555555");

        assertEquals(expected, Message.getMessageIds());
    }

    @Test
    @DisplayName("Message 4's number has no international code, so it is rejected")
    void message4RecipientRejected() {
        assertEquals(Message.CELL_FAILURE, message4.checkRecipientCell());
    }

    // ---------- Reading the JSON file into the array ----------

    @Test
    @DisplayName("Stored messages are read back from the JSON file")
    void loadStoredMessagesFromFile() {
        Message.clearAll();

        assertEquals(2, Message.loadStoredMessages());

        List<Message> stored = Message.getStoredMessages();
        assertEquals(TEXT_2, stored.get(0).getText());
        assertEquals("+27838884567", stored.get(0).getRecipient());
        assertEquals("01:1:WHERETIME", stored.get(0).getMessageHash());
        assertEquals(TEXT_5, stored.get(1).getText());
    }

    @Test
    @DisplayName("Loading from a file that does not exist returns 0")
    void loadMissingFile() {
        Message.clearAll();
        Message.setStorageFile(tempDir.resolve("doesNotExist.json").toString());

        assertEquals(0, Message.loadStoredMessages());
    }

    @Test
    @DisplayName("Loading a damaged JSON file returns 0 and does not crash")
    void loadDamagedFile() throws IOException {
        Path damaged = tempDir.resolve("damaged.json");
        Files.writeString(damaged, "this is not json");
        Message.clearAll();
        Message.setStorageFile(damaged.toString());

        assertEquals(0, Message.loadStoredMessages());
    }

    @Test
    @DisplayName("A message containing quotes survives being stored and loaded")
    void quotesSurviveRoundTrip() {
        Message quoted = new Message("0599999999", 5, "+27834557896", "He said \"hi\" today");
        quoted.storeMessage();

        Message.clearAll();
        Message.loadStoredMessages();

        List<Message> stored = Message.getStoredMessages();
        assertEquals("He said \"hi\" today", stored.get(stored.size() - 1).getText());
    }

    // ---------- Stored Messages menu ----------

    @Test
    @DisplayName("Display the sender and recipient of all stored messages")
    void displaySenderAndRecipient() {
        String expected = "Sender: Kyle Smith, Recipient: +27838884567"
                + "\nSender: Kyle Smith, Recipient: +27838884567";

        assertEquals(expected, Message.displaySenderAndRecipient(SENDER));
    }

    @Test
    @DisplayName("Display the longest stored message")
    void displayLongestMessage() {
        assertEquals("Longest stored message: " + TEXT_2, Message.displayLongestMessage());
    }

    @Test
    @DisplayName("Search for a message ID shows the recipient and message")
    void searchByMessageId() {
        assertEquals("Recipient: +27838884567\nMessage: " + TEXT_2,
                Message.searchByMessageId("0122222222"));
    }

    @Test
    @DisplayName("Search for a message ID that is not stored")
    void searchByMessageIdNotFound() {
        assertEquals("Message ID not found.", Message.searchByMessageId("9999999999"));
    }

    @Test
    @DisplayName("Search for all messages stored for a recipient")
    void searchByRecipient() {
        assertEquals("Message: " + TEXT_2 + "\nMessage: " + TEXT_5,
                Message.searchByRecipient("+27838884567"));
    }

    @Test
    @DisplayName("Search for a recipient with no stored messages")
    void searchByRecipientNotFound() {
        assertEquals("No stored messages found for that recipient.",
                Message.searchByRecipient("+27830000000"));
    }

    @Test
    @DisplayName("Delete a message using its hash")
    void deleteByHash() throws IOException {
        assertEquals("Message \"" + TEXT_2 + "\" successfully deleted.",
                Message.deleteByHash("01:1:WHERETIME"));

        assertEquals(1, Message.getStoredMessages().size());
        assertFalse(Message.getMessageIds().contains("0122222222"));

        String json = Files.readString(tempDir.resolve("storedMessages.json"));
        assertFalse(json.contains("0122222222"));
        assertTrue(json.contains("0455555555"));
    }

    @Test
    @DisplayName("Deleting a hash that does not exist")
    void deleteByHashNotFound() {
        assertEquals("Message hash not found.", Message.deleteByHash("99:9:NOTHING"));
        assertEquals(2, Message.getStoredMessages().size());
    }

    @Test
    @DisplayName("Report lists the full details of all stored messages")
    void displayReport() {
        String expected = "Stored Messages Report"
                + "\n\n" + message2.getDetails()
                + "\n\n" + message5.getDetails();

        assertEquals(expected, Message.displayReport());
    }

    @Test
    @DisplayName("Menu options report when nothing is stored")
    void nothingStored() {
        Message.clearAll();

        assertEquals("No stored messages.", Message.displayLongestMessage());
        assertEquals("No stored messages.", Message.displayReport());
        assertEquals("No stored messages.", Message.displaySenderAndRecipient(SENDER));
    }
}
