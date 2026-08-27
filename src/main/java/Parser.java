/**
 * Interprets raw user input as one of TryBot's supported command types.
 */
public class Parser {
    /**
     * The command categories recognized by TryBot.
     */
    public enum CommandType {
        BYE, LIST, MARK, UNMARK, DELETE, TODO, DEADLINE, EVENT, EMPTY, UNKNOWN
    }

    /**
     * The normalized meaning of one user command.
     *
     * @param type recognized command category
     * @param body text after the command keyword, with an optional colon removed
     */
    public record ParsedCommand(CommandType type, String body) {
    }

    /**
     * Parses one line of user input.
     *
     * @param input raw user input
     * @return parsed command and its normalized body
     */
    public ParsedCommand parse(String input) {
        if (input == null) {
            return new ParsedCommand(CommandType.EMPTY, "");
        }

        String command = input.trim();
        if (command.isEmpty()) {
            return new ParsedCommand(CommandType.EMPTY, "");
        }
        if (command.equalsIgnoreCase("bye") || command.equalsIgnoreCase("bye!")) {
            return new ParsedCommand(CommandType.BYE, "");
        }
        if (command.equalsIgnoreCase("list")) {
            return new ParsedCommand(CommandType.LIST, "");
        }
        if (startsWithKeyword(command, "mark")) {
            return new ParsedCommand(CommandType.MARK, getCommandBody(command, "mark"));
        }
        if (startsWithKeyword(command, "unmark")) {
            return new ParsedCommand(CommandType.UNMARK, getCommandBody(command, "unmark"));
        }
        if (startsWithKeyword(command, "delete")) {
            return new ParsedCommand(CommandType.DELETE, getCommandBody(command, "delete"));
        }
        if (startsWithKeyword(command, "todo", true)) {
            return new ParsedCommand(CommandType.TODO, getCommandBody(command, "todo"));
        }
        if (startsWithKeyword(command, "deadline", true)) {
            return new ParsedCommand(CommandType.DEADLINE, getCommandBody(command, "deadline"));
        }
        if (startsWithKeyword(command, "event", true)) {
            return new ParsedCommand(CommandType.EVENT, getCommandBody(command, "event"));
        }
        return new ParsedCommand(CommandType.UNKNOWN, "");
    }

    /**
     * Checks whether a command starts with a keyword.
     *
     * @param command trimmed command text
     * @param keyword keyword to find
     * @return true when the first command word matches the keyword
     */
    private boolean startsWithKeyword(String command, String keyword) {
        return startsWithKeyword(command, keyword, false);
    }

    /**
     * Checks whether a command starts with a keyword, optionally allowing a colon.
     *
     * @param command trimmed command text
     * @param keyword keyword to find
     * @param allowColon whether the keyword may be followed by a colon
     * @return true when the first command word matches the keyword
     */
    private boolean startsWithKeyword(String command, String keyword, boolean allowColon) {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length == 0) {
            return false;
        }
        return commandParts[0].equalsIgnoreCase(keyword)
                || (allowColon && commandParts[0].equalsIgnoreCase(keyword + ":"));
    }

    /**
     * Extracts the text after a command keyword and removes an optional colon.
     *
     * @param command command text beginning with the keyword
     * @param keyword command keyword to remove
     * @return text after the keyword
     */
    private String getCommandBody(String command, String keyword) {
        String body = command.substring(keyword.length()).trim();
        if (body.startsWith(":")) {
            body = body.substring(1).trim();
        }
        return body;
    }
}
