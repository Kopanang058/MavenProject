package chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Message class, using the test data from the Part 2 brief.
 */
class MessageTest {

    @TempDir
    Path tempDir;

    private Message message1;
    private Message message2;

    @BeforeEach
    void setUp() {
        // Start every test with empty sent/stored lists and a temporary JSON file
        Message.clearAll();
        Message.setStorageFile(tempDir.resolve("storedMessages.json").toString());

        // Test Case 1: sent
        message1 = new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");

        // Test Case 2: discarded
        message2 = new Message("0098765432", 1, "08575975889",
                "Hi Keegan, did you receive the payment?");
    }

    // ---------- Message length ----------

    @Test
    @DisplayName("Message under 250 characters is ready to send")
    void messageLengthSuccess() {
        assertEquals("Message ready to send.", Message.checkMessageLength(message1.getText()));
    }

    @Test
    @DisplayName("Message of exactly 250 characters is accepted")
    void messageLengthBoundary() {
        assertEquals("Message ready to send.", Message.checkMessageLength("a".repeat(250)));
    }

    @Test
    @DisplayName("Message over 250 characters reports how many characters it is over by")
    void messageLengthFailure() {
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                Message.checkMessageLength("a".repeat(260)));
    }

    // ---------- Recipient number ----------

    @Test
    @DisplayName("Recipient number correctly formatted")
    void recipientCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
    }

    @Test
    @DisplayName("Recipient number with no international code is rejected")
    void recipientIncorrectlyFormatted() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                        + "international code. Please correct the number and try again.",
                message2.checkRecipientCell());
    }

    // ---------- Message hash ----------

    @Test
    @DisplayName("Message hash for Test Case 1 is 00:0:HITONIGHT")
    void messageHashTestCase1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
    }

    @Test
    @DisplayName("Message hashes are correct for every test message (loop)")
    void messageHashesInLoop() {
        Message[] messages = {message1, message2};
        String[] expectedHashes = {"00:0:HITONIGHT", "00:1:HIPAYMENT"};

        for (int i = 0; i < messages.length; i++) {
            assertEquals(expectedHashes[i], messages[i].createMessageHash());
        }
    }

    @Test
    @DisplayName("A one-word message uses the same word as first and last word")
    void messageHashOneWord() {
        Message oneWord = new Message("0012345678", 0, "+27718693002", "Thanks");
        assertEquals("00:0:THANKSTHANKS", oneWord.createMessageHash());
    }

    // ---------- Message ID ----------

    @Test
    @DisplayName("Message ID is created and reported")
    void messageIdCreated() {
        assertEquals("Message ID generated: 0012345678", message1.messageIdGenerated());
    }

    @Test
    @DisplayName("Auto-generated message ID is ten digits and passes checkMessageID")
    void generatedIdIsTenDigits() {
        Message generated = new Message("+27718693002", "Hello");

        assertTrue(generated.checkMessageID());
        assertEquals(10, generated.getMessageId().length());
        assertTrue(generated.getMessageId().matches("[0-9]{10}"));
    }

    @Test
    @DisplayName("Message ID longer than ten characters fails checkMessageID")
    void messageIdTooLong() {
        Message tooLong = new Message("12345678901", 0, "+27718693002", "Hello");
        assertFalse(tooLong.checkMessageID());
    }

    // ---------- Send, disregard, store ----------

    @Test
    @DisplayName("Option 1: Send Message")
    void sendMessageOption() {
        assertEquals("Message successfully sent.", message1.sentMessage(1));
    }

    @Test
    @DisplayName("Option 2: Disregard Message")
    void disregardMessageOption() {
        assertEquals("Press 0 to delete the message.", message2.sentMessage(2));
    }

    @Test
    @DisplayName("Option 3: Store Message")
    void storeMessageOption() {
        assertEquals("Message successfully stored.", message1.sentMessage(3));
    }

    @Test
    @DisplayName("An invalid option is rejected")
    void invalidOption() {
        assertEquals("Invalid option. Please choose 1, 2 or 3.", message1.sentMessage(7));
    }

    // ---------- Totals and printing ----------

    @Test
    @DisplayName("Total sent is 1 after sending Message 1 and discarding Message 2")
    void totalMessagesSent() {
        message1.sentMessage(1);
        message2.sentMessage(2);

        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    @DisplayName("printMessages shows ID, hash, recipient and message in that order")
    void printMessagesFormat() {
        message1.sentMessage(1);

        String expected = "Message ID: 0012345678"
                + "\nMessage Hash: 00:0:HITONIGHT"
                + "\nRecipient: +27718693002"
                + "\nMessage: Hi Mike, can you join us for dinner tonight?";

        assertEquals(expected, Message.printMessages());
    }

    @Test
    @DisplayName("printMessages with nothing sent")
    void printMessagesEmpty() {
        assertEquals("No messages have been sent.", Message.printMessages());
    }

    // ---------- JSON storage ----------

    @Test
    @DisplayName("Stored message is written to the JSON file")
    void storedMessageWrittenToJson() throws IOException {
        message1.storeMessage();

        String json = Files.readString(tempDir.resolve("storedMessages.json"));

        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"messageId\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"00:0:HITONIGHT\""));
        assertTrue(json.contains("\"recipient\": \"+27718693002\""));
    }

    @Test
    @DisplayName("Quotes inside a stored message are escaped so the JSON stays valid")
    void jsonEscapesQuotes() throws IOException {
        Message quoted = new Message("0012345678", 0, "+27718693002", "He said \"hi\" today");
        quoted.storeMessage();

        String json = Files.readString(tempDir.resolve("storedMessages.json"));

        assertTrue(json.contains("He said \\\"hi\\\" today"));
    }

    @Test
    @DisplayName("Storing two messages keeps both in the JSON file")
    void twoStoredMessages() throws IOException {
        message1.storeMessage();
        message2.storeMessage();

        String json = Files.readString(tempDir.resolve("storedMessages.json"));

        assertTrue(json.contains("0012345678"));
        assertTrue(json.contains("0098765432"));
    }
}
