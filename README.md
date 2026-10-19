# Chat Application — PoE Part 1 (Registration and Login)

A console-only Java application that registers a user and logs them in.

## Project structure

```
pom.xml
.github/workflows/maven.yml
src/main/java/chatapp/Login.java   <- all validation and login logic
src/main/java/chatapp/App.java     <- console interface (no GUI)
src/test/java/chatapp/LoginTest.java
```

## Running

```
mvn test                 # run the unit tests
mvn compile exec:java    # run the console application
```

## Validation rules

| Rule | Requirement |
|---|---|
| Username | Contains an underscore, no more than 5 characters |
| Password | At least 8 characters, a capital letter, a number, a special character |
| Cell number | International code `+27` followed by no more than 10 digits |

## Suggested commit sequence 
1. `Initial commit: Maven project setup`
2. `Add Login class with checkUserName method`
3. `Add password complexity validation`
4. `Add regex-based cell phone number validation`
5. `Add registerUser, loginUser and returnLoginStatus methods`
6. `Add JUnit unit tests for all validation rules`
7. `Add GitHub Actions workflow for automated testing`
8. `Add console application and README`

## Reference

Oracle (2024) *Class Pattern*, Java SE 17 API Documentation. Available at:
https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/regex/Pattern.html
(Accessed: 17 September 2026).
# Part 2 — Sending Messages (QuickChat)

Built on top of Part 1. After a successful login the app shows **"Welcome to QuickChat."**
and a menu: 1) Send Messages, 2) Show recently sent messages ("Coming Soon."), 3) Quit.

## New files

```
src/main/java/chatapp/Message.java       <- message rules, hash, send/store, JSON
src/main/java/chatapp/QuickChat.java     <- console menu and message entry
src/test/java/chatapp/MessageTest.java   <- unit tests using the brief's test data
```

Part 1 files changed slightly: `Login` now remembers a successful login (`isLoggedIn()`),
and `App` hands over to `QuickChat` after login.

## Message rules

| Item | Rule |
|---|---|
| Message ID | Random ten-digit number |
| Message number | Starts at 0, increases with each sent message |
| Recipient | Same international-code check as Part 1 (reused from `Login`) |
| Message | No more than 250 characters |
| Message hash | First two ID digits : message number : first + last word, in capitals (e.g. `00:0:HITONIGHT`) |
| Storage | Stored messages are written to `storedMessages.json` |

## References

Bray, T. (2017) *The JavaScript Object Notation (JSON) Data Interchange Format*, RFC 8259.
Available at: https://www.rfc-editor.org/rfc/rfc8259 (Accessed: 19 October 2026).

Oracle (2024) *Class Files*, Java SE 17 API Documentation. Available at:
https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
(Accessed: 19 October 2026).
