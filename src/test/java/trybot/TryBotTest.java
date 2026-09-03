package trybot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/** Tests the application entry point used by the JavaFX client. */
public class TryBotTest {
    @Test
    public void getResponse_todoCommand_returnsTaskConfirmation() throws Exception {
        Path dataFile = Files.createTempFile("trybot-test", ".txt");
        try {
            TryBot tryBot = new TryBot(dataFile.toString());
            assertEquals("Got it. I've added this task:\n[T][ ] read book\n"
                    + "Now you have 1 tasks in the list.", tryBot.getResponse("todo read book"));
        } finally {
            Files.deleteIfExists(dataFile);
        }
    }

    @Test
    public void getResponse_invalidCommand_returnsParserError() throws Exception {
        Path dataFile = Files.createTempFile("trybot-test", ".txt");
        try {
            TryBot tryBot = new TryBot(dataFile.toString());
            assertEquals("I do not recognise that command. Try todo, list, or bye.",
                    tryBot.getResponse("unknown"));
        } finally {
            Files.deleteIfExists(dataFile);
        }
    }
}
