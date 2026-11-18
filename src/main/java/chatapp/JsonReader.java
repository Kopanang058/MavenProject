package chatapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the JSON file that Message.storeMessage() writes: an array of objects whose
 * values are either strings or whole numbers. Every value is returned as text.
 *
 * Reference:
 * Bray, T. (2017) The JavaScript Object Notation (JSON) Data Interchange Format, RFC 8259.
 * Internet Engineering Task Force. Available at: https://www.rfc-editor.org/rfc/rfc8259
 * (Accessed: 18 November 2026) - used for the structure of arrays, objects and string escapes.
 */
class JsonReader {

    private final String text;
    private int position = 0;

    JsonReader(String text) {
        this.text = text;
    }

    /**
     * Reads a JSON array of objects, for example [ { "a": "b" }, { "a": "c" } ].
     *
     * @throws IllegalArgumentException if the text is not in the expected format
     */
    List<Map<String, String>> readArrayOfObjects() {
        List<Map<String, String>> objects = new ArrayList<>();

        skipWhitespace();
        expect('[');
        skipWhitespace();

        if (peek() == ']') {
            position++;
            return objects;
        }

        while (true) {
            objects.add(readObject());
            skipWhitespace();

            char next = next();
            if (next == ']') {
                break;
            }
            if (next != ',') {
                throw new IllegalArgumentException("Expected ',' or ']' at position " + (position - 1));
            }
        }
        return objects;
    }

    private Map<String, String> readObject() {
        Map<String, String> object = new LinkedHashMap<>();

        skipWhitespace();
        expect('{');
        skipWhitespace();

        if (peek() == '}') {
            position++;
            return object;
        }

        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();

            String value = (peek() == '"') ? readString() : readNumber();
            object.put(key, value);

            skipWhitespace();
            char next = next();
            if (next == '}') {
                break;
            }
            if (next != ',') {
                throw new IllegalArgumentException("Expected ',' or '}' at position " + (position - 1));
            }
        }
        return object;
    }

    private String readString() {
        expect('"');
        StringBuilder value = new StringBuilder();

        while (true) {
            char c = next();

            if (c == '"') {
                break;
            }

            if (c == '\\') {
                char escaped = next();
                switch (escaped) {
                    case '"':
                        value.append('"');
                        break;
                    case '\\':
                        value.append('\\');
                        break;
                    case '/':
                        value.append('/');
                        break;
                    case 'n':
                        value.append('\n');
                        break;
                    case 'r':
                        value.append('\r');
                        break;
                    case 't':
                        value.append('\t');
                        break;
                    case 'b':
                        value.append('\b');
                        break;
                    case 'f':
                        value.append('\f');
                        break;
                    case 'u':
                        value.append(readUnicodeEscape());
                        break;
                    default:
                        throw new IllegalArgumentException("Bad escape \\" + escaped);
                }
            } else {
                value.append(c);
            }
        }
        return value.toString();
    }

    private char readUnicodeEscape() {
        if (position + 4 > text.length()) {
            throw new IllegalArgumentException("Incomplete unicode escape");
        }
        String hex = text.substring(position, position + 4);
        position += 4;
        return (char) Integer.parseInt(hex, 16);
    }

    private String readNumber() {
        int start = position;

        while (position < text.length()) {
            char c = text.charAt(position);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                break;
            }
            position++;
        }

        if (start == position) {
            throw new IllegalArgumentException("Expected a value at position " + position);
        }
        return text.substring(start, position);
    }

    private void skipWhitespace() {
        while (position < text.length() && Character.isWhitespace(text.charAt(position))) {
            position++;
        }
    }

    private char peek() {
        if (position >= text.length()) {
            throw new IllegalArgumentException("Unexpected end of JSON");
        }
        return text.charAt(position);
    }

    private char next() {
        char c = peek();
        position++;
        return c;
    }

    private void expect(char expected) {
        char actual = next();
        if (actual != expected) {
            throw new IllegalArgumentException("Expected '" + expected + "' but found '"
                    + actual + "' at position " + (position - 1));
        }
    }
}
