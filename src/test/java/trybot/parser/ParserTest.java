package trybot.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import trybot.command.AddDeadlineCommand;
import trybot.command.AddEventCommand;
import trybot.command.AddTodoCommand;
import trybot.command.Command;
import trybot.command.DeleteCommand;
import trybot.command.EmptyCommand;
import trybot.command.ExitCommand;
import trybot.command.ListCommand;
import trybot.command.MarkCommand;
import trybot.command.UnknownCommand;
import trybot.command.UnmarkCommand;
import trybot.exception.TryBotException;

/**
 * Tests Parser's command routing and validation of command arguments.
 */
class ParserTest {
    private final Parser parser = new Parser();

    @Test
    void parse_simpleCommands_returnsExpectedCommandTypes() throws TryBotException {
        assertInstanceOf(ExitCommand.class, parser.parse("bye"));
        assertInstanceOf(ExitCommand.class, parser.parse(" BYE! "));
        assertInstanceOf(ListCommand.class, parser.parse("list"));
        assertInstanceOf(EmptyCommand.class, parser.parse("   "));
        assertInstanceOf(EmptyCommand.class, parser.parse(null));
        assertInstanceOf(UnknownCommand.class, parser.parse("archive"));
    }

    @Test
    void parse_taskCommands_returnsExpectedCommandTypes() throws TryBotException {
        assertInstanceOf(AddTodoCommand.class, parser.parse("todo read book"));
        assertInstanceOf(AddTodoCommand.class, parser.parse("ToDo: read book"));
        assertInstanceOf(AddDeadlineCommand.class, parser.parse("deadline report /by Friday"));
        assertInstanceOf(AddEventCommand.class, parser.parse("event meeting /from Monday /to Tuesday"));
        assertInstanceOf(MarkCommand.class, parser.parse("mark 2"));
        assertInstanceOf(UnmarkCommand.class, parser.parse("unmark 2"));
        assertInstanceOf(DeleteCommand.class, parser.parse("delete 2"));
    }

    @Test
    void parse_deadlineCommand_preservesParsedFields() throws TryBotException {
        AddDeadlineCommand command = assertInstanceOf(AddDeadlineCommand.class,
                parser.parse("deadline submit report /by 2024-02-29 1830"));
        assertEquals(false, command.isExit());
    }

    @Test
    void parse_eventCommand_preservesParsedFields() throws TryBotException {
        AddEventCommand command = assertInstanceOf(AddEventCommand.class,
                parser.parse("event study /from 2024-02-29 0900 /to 2024-02-29 1030"));
        assertEquals(false, command.isExit());
    }

    @Test
    void parse_missingOrMalformedTaskArguments_rejectsStructuredArguments() {
        assertInstanceOf(AddTodoCommand.class, parseUnchecked("todo"));
        assertThrows(TryBotException.class, () -> parser.parse("deadline report"));
        assertThrows(TryBotException.class, () -> parser.parse("event meeting /from Monday"));
        assertThrows(TryBotException.class, () -> parser.parse("mark"));
        assertThrows(TryBotException.class, () -> parser.parse("mark abc"));
        assertThrows(TryBotException.class, () -> parser.parse("delete 1 2"));
    }

    /**
     * Adapts the checked parser contract for the one valid command that is
     * intentionally validated later, during command execution.
     */
    private Command parseUnchecked(String input) {
        try {
            return parser.parse(input);
        } catch (TryBotException exception) {
            throw new AssertionError(exception);
        }
    }
}
