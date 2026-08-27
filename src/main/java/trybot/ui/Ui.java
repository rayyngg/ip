package trybot.ui;

import java.util.List;
import java.util.Scanner;

import trybot.task.Task;
import trybot.task.TaskList;

/**
 * Handles all console interactions with the TryBot user.
 */
public class Ui {
    private static final String SEPARATOR = "____________________________________________________________";
    private static final String BANNER = " _____             ____        _\n"
            + "|_   _| _ __ _   _ | __ )  ___ | |_\n"
            + "  | |  | '__| | | ||  _ \\ / _ \\| __|\n"
            + "  | |  | |  | |_| || |_) | (_) | |_\n"
            + "  |_|  |_|   \\__, ||____/ \\___/ \\__|\n"
            + "              |___/\n";
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Displays TryBot's welcome message.
     */
    public void showWelcome() {
        showLine();
        System.out.print(BANNER);
        System.out.println("Hello! I'm TryBot.");
        System.out.println("What can I do for you?");
        showLine();
    }

    /**
     * Displays the standard separator used between console interactions.
     */
    public void showLine() {
        System.out.println(SEPARATOR);
    }

    /**
     * Checks whether another command is available from the user.
     *
     * @return true when another input line can be read
     */
    public boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    /**
     * Reads one complete command from the user.
     *
     * @return raw command text
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Displays TryBot's goodbye message.
     */
    public void showGoodbye() {
        showGoodbyeMessage();
        showLine();
    }

    /**
     * Displays only TryBot's goodbye message, without a separator.
     *
     * The command loop displays the final separator in its {@code finally} block.
     */
    public void showGoodbyeMessage() {
        System.out.println("Bye. Hope to see you again soon!");
    }

    /**
     * Displays an input or task-management error.
     *
     * @param message user-friendly error message.
     */
    public void showError(String message) {
        System.out.println(message);
    }

    /**
     * Displays a confirmation after adding a task.
     *
     * @param task task that was added.
     * @param taskCount number of tasks after the addition.
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("Got it. I've added this task:");
        System.out.println(task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Displays all tasks in their current list order.
     *
     * @param tasks tasks to display.
     */
    public void showTaskList(TaskList tasks) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Displays tasks matching a search keyword in their original task-list order.
     *
     * @param matchingTasks tasks whose descriptions matched the search.
     */
    public void showMatchingTasks(List<Task> matchingTasks) {
        if (matchingTasks.isEmpty()) {
            System.out.println("No tasks match that keyword.");
            return;
        }

        System.out.println("Here are the matching tasks in your list:");
        for (int i = 0; i < matchingTasks.size(); i++) {
            System.out.println((i + 1) + "." + matchingTasks.get(i));
        }
    }

    /**
     * Displays a confirmation after marking a task as done.
     *
     * @param task task that was marked as done.
     */
    public void showTaskMarkedDone(Task task) {
        System.out.println("Good work!! I've marked this task as done:");
        System.out.println(task);
    }

    /**
     * Displays a confirmation after marking a task as not done.
     *
     * @param task task that was marked as not done.
     */
    public void showTaskMarkedNotDone(Task task) {
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println(task);
    }

    /**
     * Displays a confirmation after deleting a task.
     *
     * @param task task that was deleted.
     * @param taskCount number of tasks after the deletion.
     */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println("Noted. I've removed this task:");
        System.out.println(task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }

    /**
     * Reports that the saved task list could not be loaded.
     */
    public void showLoadingError() {
        System.err.println("Warning: TryBot could not load the saved task list. "
                + "Starting with an empty list.");
    }

    /**
     * Reports that the current task list could not be saved.
     */
    public void showSavingError() {
        System.err.println("Warning: TryBot could not save the task list. "
                + "Your changes will be lost when TryBot exits.");
    }
}
