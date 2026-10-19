package quickchat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessageTest {

    // Test data for message 1 and message 2 from the brief
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String TEXT_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String TEXT_2 = "Hi Keegan, did you receive the payment?";

    private Message message1;
    private Message message2;

    @BeforeEach
    void setUp() {
        Message.resetState();
        // Known IDs so that the hashes can be predicted
        message1 = new Message("0012345678", 0, RECIPIENT_1, TEXT_1);
        message2 = new Message("5598765432", 1, RECIPIENT_2, TEXT_2);
    }

    @Test
    void recipientNumberCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.", message1.checkRecipientCell());
    }

    @Test
    void recipientNumberIncorrectlyFormatted() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an "
                + "international code. Please correct the number and try again.",
                message2.checkRecipientCell());
    }

    @Test
    void messageHashIsCorrectForTestCase1() {
        assertEquals("00:0:HITONIGHT", message1.createMessageHash());
    }

    @Test
    void messageHashesAreCorrectInALoop() {
        Message[] messages = { message1, message2 };
        String[] expectedHashes = { "00:0:HITONIGHT", "55:1:HIPAYMENT" };

        for (int i = 0; i < messages.length; i++) {
            assertEquals(expectedHashes[i], messages[i].createMessageHash());
        }
    }

    @Test
    void messageIdIsCreated() {
        Message generated = new Message(0, RECIPIENT_1, TEXT_1);
        assertEquals(10, generated.getMessageId().length());
        assertTrue(generated.checkMessageID());
    }

    @Test
    void messageIdLongerThanTenCharactersFails() {
        assertFalse(new Message("12345678901", 0, RECIPIENT_1, TEXT_1).checkMessageID());
    }

    @Test
    void messageSentReturnsCorrectMessagesForEachChoice() {
        assertEquals("Message successfully sent.", message1.sentMessage(Message.SEND));
        assertEquals("Press 0 to delete the message.", message2.sentMessage(Message.DISREGARD));
    }

    @Test
    void messageStoredReturnsCorrectMessage(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("stored.json");
        Message.setStorageFile(file);

        assertEquals("Message successfully stored.", message1.sentMessage(Message.STORE));
        assertTrue(Files.readString(file).contains("00:0:HITONIGHT"));
    }

    @Test
    void storedMessageIsWrittenAsJson(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("stored.json");
        message1.storeMessage(file);
        message2.storeMessage(file);
        String json = Files.readString(file);

        assertTrue(json.startsWith("["));
        assertTrue(json.contains("\"messageID\": \"0012345678\""));
        assertTrue(json.contains("\"messageHash\": \"55:1:HIPAYMENT\""));
    }

    @Test
    void messageTooLongIsRejected() {
        String tooLong = "a".repeat(251);
        assertEquals("Please enter a message of less than 250 characters.",
                Message.checkMessageLength(tooLong));
    }

    @Test
    void messageWithinLimitIsAccepted() {
        assertEquals("Message sent", Message.checkMessageLength(TEXT_1));
    }

    @Test
    void totalMessagesSentCountsOnlySentMessages() {
        message1.sentMessage(Message.SEND);      // sent
        message2.sentMessage(Message.DISREGARD); // discarded, so not counted
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    void printMessagesListsSentMessagesInOrder() {
        message1.sentMessage(Message.SEND);
        String printed = Message.printMessages();

        assertTrue(printed.indexOf("Message ID") < printed.indexOf("Message Hash"));
        assertTrue(printed.indexOf("Message Hash") < printed.indexOf("Recipient"));
        assertTrue(printed.indexOf("Recipient") < printed.indexOf("Message: Hi Mike"));
    }
}
