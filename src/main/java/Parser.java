import java.util.Locale;

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
     * The fields needed to create a deadline task.
     *
     * @param description deadline description
     * @param by date or time by which the task should be completed
     */
    public record ParsedDeadline(String description, String by) {
    }

    /**
     * The fields needed to create an event task.
     *
     * @param description event description
     * @param from event start date or time
     * @param to event end date or time
     */
    public record ParsedEvent(String description, String from, String to) {
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
     * Parses the structured fields of a deadline command body.
     *
     * @param body normalized text after the deadline keyword
     * @return parsed deadline fields
     * @throws TryBotException if the body does not contain valid deadline fields
     */
    public ParsedDeadline parseDeadline(String body) throws TryBotException {
        String lowerCaseBody = body.toLowerCase(Locale.ROOT);
        int byIndex = lowerCaseBody.indexOf("/by");
        if (byIndex < 0) {
            throw new TryBotException("A deadline needs /by followed by a date or time. Example: deadline report /by Friday.");
        }

        String description = body.substring(0, byIndex).trim();
        String by = body.substring(byIndex + "/by".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new TryBotException("A deadline needs both a description and a date or time. Example: deadline report /by Friday.");
        }
        return new ParsedDeadline(description, by);
    }

    /**
     * Parses the structured fields of an event command body.
     *
     * @param body normalized text after the event keyword
     * @return parsed event fields
     * @throws TryBotException if the body does not contain valid event fields
     */
    public ParsedEvent parseEvent(String body) throws TryBotException {
        String lowerCaseBody = body.toLowerCase(Locale.ROOT);
        int fromIndex = lowerCaseBody.indexOf("/from");
        int toIndex = lowerCaseBody.indexOf("/to", fromIndex + "/from".length());
        if (fromIndex < 0 || toIndex < 0) {
            throw new TryBotException("An event needs /from and /to time details. Example: event meeting /from Monday /to Tuesday.");
        }

        String description = body.substring(0, fromIndex).trim();
        String from = body.substring(fromIndex + "/from".length(), toIndex).trim();
        String to = body.substring(toIndex + "/to".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new TryBotException("An event needs a description, start time, and end time. Example: event meeting /from Monday /to Tuesday.");
        }
        return new ParsedEvent(description, from, to);
    }

    /**
     * Parses the single task number used by mark, unmark, and delete commands.
     *
     * @param body normalized text after the command keyword
     * @param commandName lowercase command name used in error messages
     * @return parsed task number
     * @throws TryBotException if the body is missing, has extra values, or is not numeric
     */
    public int parseTaskNumber(String body, String commandName) throws TryBotException {
        String[] commandParts = body.split("\\s+");
        if (body.isEmpty() || commandParts.length != 1) {
            String displayName = Character.toUpperCase(commandName.charAt(0)) + commandName.substring(1);
            throw new TryBotException(displayName + " needs one task number. Example: " + commandName + " 1.");
        }

        try {
            return Integer.parseInt(commandParts[0]);
        } catch (NumberFormatException exception) {
            throw new TryBotException("The task number must be a whole number. Example: " + commandName + " 1.");
        }
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
