package trybot.ui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import trybot.task.Task;
import trybot.task.TaskList;

/**
 * Formats friendly graphical replies and tracks errors independently of their wording.
 */
public class ChatUi extends Ui {
    private final ByteArrayOutputStream buffer;
    private final PrintStream output;
    private boolean hasError;
    private boolean hasWarning;

    /**
     * Creates an empty reply collector for one command.
     */
    public ChatUi() {
        this(new ByteArrayOutputStream());
    }

    private ChatUi(ByteArrayOutputStream buffer) {
        super(new PrintStream(buffer, true, StandardCharsets.UTF_8),
                new PrintStream(buffer, true, StandardCharsets.UTF_8));
        this.buffer = buffer;
        output = new PrintStream(buffer, true, StandardCharsets.UTF_8);
    }

    @Override
    public void showLine() {
        // Conversation cards provide separation without console divider characters.
    }

    @Override
    public void showError(String message) {
        hasError = true;
        output.println(message);
        output.println("Let's try again. You can edit your command below, or choose Commands for examples.");
    }

    @Override
    public void showSavingError() {
        hasWarning = true;
        super.showSavingError();
    }

    @Override
    public void showTaskAdded(Task task, int taskCount) {
        output.println("Got it! I've added this to your list:");
        output.println(task);
        output.println("You now have " + taskCount + (taskCount == 1 ? " task." : " tasks."));
        output.println("Want to tell me where it fits? Try: tag " + taskCount + " school (or a tag of your own).");
    }

    @Override
    public void showTaskList(TaskList tasks) {
        if (tasks.size() == 0) {
            output.println("Your list is a fresh start. What's one thing you'd like to get done?");
            output.println("Try: todo read a chapter");
            return;
        }
        output.println("Here's what we've got on your list:");
        for (int i = 0; i < tasks.size(); i++) {
            output.println((i + 1) + ". " + tasks.get(i));
        }
        output.println("Finished something? Tell me with mark <number>.");
    }

    @Override
    public void showTaskMarkedDone(Task task) {
        output.println("Nice work! That's one more thing done:");
        output.println(task);
    }

    @Override
    public void showTaskMarkedNotDone(Task task) {
        output.println("No rush. I've put this back on your to-do list:");
        output.println(task);
    }

    @Override
    public void showTagCreated(int taskNumber, String tagName) {
        output.println("Thanks! I've tagged task " + taskNumber + " with #" + tagName + ".");
        output.println("That gives your list a little more context. What else is on your mind?");
    }

    @Override
    public void showGoodbyeMessage() {
        output.println("Thank you for sharing your to-do list with me! See you soon.");
    }

    public String getText() {
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n").strip();
    }

    public boolean hasError() {
        return hasError;
    }

    public boolean hasWarning() {
        return hasWarning;
    }
}
