package chatapp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the Login class.
 * Each test maps directly to a row in the PoE Part 1 test table.
 */
class LoginTest {

    private Login login;

    @BeforeEach
    void setUp() {
        login = new Login("Kyle", "Smith");
    }

    // ---------- Username ----------

    @Test
    @DisplayName("Username correctly formatted returns true")
    void usernameCorrectlyFormatted() {
        assertTrue(login.checkUserName("kyl_1"));
    }

    @Test
    @DisplayName("Username incorrectly formatted returns false")
    void usernameIncorrectlyFormatted() {
        assertFalse(login.checkUserName("kyle!!!!!!!"));
    }

    @Test
    @DisplayName("Correctly formatted username produces the welcome message")
    void usernameCorrectlyFormattedMessage() {
        assertEquals("Welcome Kyle ,Smith it is great to see you.",
                login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976"));
    }

    @Test
    @DisplayName("Incorrectly formatted username produces the error message")
    void usernameIncorrectlyFormattedMessage() {
        assertEquals("Username is not correctly formatted; please ensure that your username "
                        + "contains an underscore and is no more than five characters in length.",
                login.registerUser("kyle!!!!!!!", "Ch&&sec@ke99!", "+27838968976"));
    }

    // ---------- Password ----------

    @Test
    @DisplayName("Password meeting the complexity requirements returns true")
    void passwordMeetsComplexity() {
        assertTrue(login.checkPasswordComplexity("Ch&&sec@ke99!"));
    }

    @Test
    @DisplayName("Password not meeting the complexity requirements returns false")
    void passwordDoesNotMeetComplexity() {
        assertFalse(login.checkPasswordComplexity("password"));
    }

    @Test
    @DisplayName("Password not meeting the complexity requirements produces the error message")
    void passwordComplexityMessage() {
        assertEquals("Password is not correctly formatted; please ensure that the password "
                        + "contains at least eight characters, a capital letter, a number, "
                        + "and a special character.",
                login.registerUser("kyl_1", "password", "+27838968976"));
    }

    // ---------- Cell phone number ----------

    @Test
    @DisplayName("Cell phone number correctly formatted returns true")
    void cellPhoneCorrectlyFormatted() {
        assertTrue(login.checkCellPhoneNumber("+27838968976"));
    }

    @Test
    @DisplayName("Cell phone number incorrectly formatted returns false")
    void cellPhoneIncorrectlyFormatted() {
        assertFalse(login.checkCellPhoneNumber("08966553"));
    }

    @Test
    @DisplayName("Incorrectly formatted cell phone number produces the error message")
    void cellPhoneMessage() {
        assertEquals("Cell number is incorrectly formatted or does not contain an "
                        + "international code; please correct the number and try again.",
                login.registerUser("kyl_1", "Ch&&sec@ke99!", "08966553"));
    }

    // ---------- Login ----------

    @Test
    @DisplayName("Login successful returns true")
    void loginSuccessful() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertTrue(login.loginUser("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    @DisplayName("Login failed returns false")
    void loginFailed() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.loginUser("kyl_1", "wrongPassword1!"));
    }

    @Test
    @DisplayName("Successful login returns the welcome status message")
    void successfulLoginStatus() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals("Welcome Kyle ,Smith it is great to see you.",
                login.returnLoginStatus("kyl_1", "Ch&&sec@ke99!"));
    }

    @Test
    @DisplayName("Failed login returns the failure status message")
    void failedLoginStatus() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertEquals("Username or password incorrect, please try again.",
                login.returnLoginStatus("kyl_1", "wrongPassword1!"));
    }

    // ---------- Logged-in state (needed for Part 2) ----------

    @Test
    @DisplayName("User is not logged in before logging in")
    void notLoggedInInitially() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        assertFalse(login.isLoggedIn());
    }

    @Test
    @DisplayName("User is logged in after a successful login")
    void loggedInAfterSuccess() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        login.loginUser("kyl_1", "Ch&&sec@ke99!");
        assertTrue(login.isLoggedIn());
    }

    @Test
    @DisplayName("User is not logged in after a failed login")
    void notLoggedInAfterFailure() {
        login.registerUser("kyl_1", "Ch&&sec@ke99!", "+27838968976");
        login.loginUser("kyl_1", "wrongPassword1!");
        assertFalse(login.isLoggedIn());
    }
}
