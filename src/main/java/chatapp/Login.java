package chatapp;

import java.util.regex.Pattern;

/**
 * Login handles user registration and authentication for the chat application.
 *
 * All validation rules are implemented as small, single-purpose boolean methods
 * so that they can be unit tested independently of any console input/output.
 *
 * Reference for the regular expression approach used in checkCellPhoneNumber():
 * Oracle (2024) "Class Pattern", Java SE 17 API Documentation.
 * Available at: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
 * (Accessed: 17 September 2026).
 */
public class Login {

    // Stored registration details
    private String registeredUsername;
    private String registeredPassword;
    private String registeredCellPhoneNumber;
    private boolean loggedIn = false;
    private String firstName;
    private String lastName;

    /**
     * Matches a South African number in international format:
     * a "+" sign, the country code 27, followed by no more than ten further digits.
     * Example of a valid number: +27838968976
     * Example of an invalid number: 08966553 (no international code)
     */
    private static final Pattern CELL_PHONE_PATTERN =
            Pattern.compile("^\\+27[0-9]{1,10}$");

    public Login() {
    }

    public Login(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Checks that the username contains an underscore and is no more than
     * five characters long.
     *
     * @param username the username to validate
     * @return true if correctly formatted, otherwise false
     */
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.contains("_") && username.length() <= 5;
    }

    /**
     * Checks that the password is at least eight characters long and contains
     * a capital letter, a number and a special character.
     *
     * @param password the password to validate
     * @return true if the password meets the complexity rules, otherwise false
     */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            char currentCharacter = password.charAt(i);

            if (Character.isUpperCase(currentCharacter)) {
                hasCapital = true;
            } else if (Character.isDigit(currentCharacter)) {
                hasNumber = true;
            } else if (!Character.isLetterOrDigit(currentCharacter)
                    && !Character.isWhitespace(currentCharacter)) {
                hasSpecial = true;
            }
        }

        return hasCapital && hasNumber && hasSpecial;
    }

    /**
     * Checks that the cell phone number contains the international country code
     * and is no more than ten characters long after that code.
     *
     * @param cellPhoneNumber the number to validate
     * @return true if correctly formatted, otherwise false
     */
    public boolean checkCellPhoneNumber(String cellPhoneNumber) {
        if (cellPhoneNumber == null) {
            return false;
        }
        return CELL_PHONE_PATTERN.matcher(cellPhoneNumber.trim()).matches();
    }

    /**
     * Registers a user and returns the appropriate registration message.
     * Details are only stored once every condition has been met.
     *
     * @return the registration feedback message
     */
    public String registerUser(String username, String password, String cellPhoneNumber) {
        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username "
                    + "contains an underscore and is no more than five characters in length.";
        }

        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password "
                    + "contains at least eight characters, a capital letter, a number, "
                    + "and a special character.";
        }

        if (!checkCellPhoneNumber(cellPhoneNumber)) {
            return "Cell number is incorrectly formatted or does not contain an "
                    + "international code; please correct the number and try again.";
        }

        this.registeredUsername = username;
        this.registeredPassword = password;
        this.registeredCellPhoneNumber = cellPhoneNumber.trim();

        return "Welcome " + firstName + " ," + lastName + " it is great to see you.";
    }

    /**
     * Verifies that the login details entered match the details stored at registration.
     *
     * @return true when both the username and the password match
     */
    public boolean loginUser(String username, String password) {
        if (registeredUsername == null || registeredPassword == null) {
            return false;
        }
        boolean detailsMatch = registeredUsername.equals(username)
                && registeredPassword.equals(password);

        if (detailsMatch) {
            loggedIn = true;
        }
        return detailsMatch;
    }

    /**
     * Part 2 only lets users send messages once this returns true.
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Returns the messaging for a successful or a failed login attempt.
     */
    public String returnLoginStatus(String username, String password) {
        if (loginUser(username, password)) {
            return "Welcome " + firstName + " ," + lastName + " it is great to see you.";
        }
        return "Username or password incorrect, please try again.";
    }

    // Getters and setters

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }

    public String getRegisteredCellPhoneNumber() {
        return registeredCellPhoneNumber;
    }
}
