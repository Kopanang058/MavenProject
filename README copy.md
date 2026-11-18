# Chat Application — PoE Part 1 (Registration and Login)

A console-only Java application that registers a user and logs them in, with
JUnit 5 unit tests and automated testing on every push via GitHub Actions.

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

## Suggested commit sequence (minimum six commits)

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

---

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

## Feature branch workflow

```
git checkout -b part2-sending-messages
# ...commit your work (minimum six commits)...
git push -u origin part2-sending-messages
# open a Pull Request on GitHub, wait for the green tick, then merge into main
```

## References

Bray, T. (2017) *The JavaScript Object Notation (JSON) Data Interchange Format*, RFC 8259.
Available at: https://www.rfc-editor.org/rfc/rfc8259 (Accessed: 19 October 2026).

Oracle (2024) *Class Files*, Java SE 17 API Documentation. Available at:
https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
(Accessed: 19 October 2026).

---

# Part 3 — Store Data and Display Task Report (Final PoE)

Everything from Parts 1 and 2 still works. Part 3 adds arrays and a fourth main menu option.

## New and changed files

```
src/main/java/chatapp/JsonReader.java            <- reads the JSON file back in
src/main/java/chatapp/Message.java               <- now fills the arrays and has the stored-message operations
src/main/java/chatapp/QuickChat.java             <- new option 4: Stored Messages
src/test/java/chatapp/StoredMessagesTest.java    <- tests using the five test messages
```

## Arrays (filled by the user, nothing hard-coded)

| Array | Holds |
|---|---|
| Sent messages | Every message the user chose to send |
| Disregarded messages | Every message the user chose to disregard |
| Stored messages | Read from the JSON file when QuickChat starts, and added to when a message is stored |
| Message hashes | The hash of every message |
| Message IDs | The ID of every message (same positions as the hashes) |

## Stored Messages menu (option 4)

| Option | What it does |
|---|---|
| a | Sender and recipient of all stored messages |
| b | The longest stored message |
| c | Search for a message ID, show its recipient and message |
| d | Search for all stored messages for one recipient |
| e | Delete a message using its hash (the JSON file is updated too) |
| f | Full report of all stored messages |
| g | Back to the main menu |

## Feature branch workflow

```
git checkout -b part3-stored-messages
# ...commit your work (minimum six commits)...
git push -u origin part3-stored-messages
# open a Pull Request, wait for the green tick, then merge into main
```

## References

Bray, T. (2017) *The JavaScript Object Notation (JSON) Data Interchange Format*, RFC 8259.
Available at: https://www.rfc-editor.org/rfc/rfc8259 (Accessed: 18 November 2026).
