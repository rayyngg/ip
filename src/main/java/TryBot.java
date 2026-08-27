import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A simple chatbot that stores tasks until the user says goodbye.
 */
public class TryBot {
    private static final String SEPARATOR = "____________________________________________________________";

    public static void main(String[] args) {
        String banner = " _____             ____        _\n"
                + "|_   _| _ __ _   _ | __ )  ___ | |_\n"
                + "  | |  | '__| | | ||  _ \\ / _ \\| __|\n"
                + "  | |  | |  | |_| || |_) | (_) | |_\n"
                + "  |_|  |_|   \\__, ||____/ \\___/ \\__|\n"
                + "              |___/\n";

        List<Task> tasks = loadTasks();

        System.out.println(SEPARATOR);
        System.out.print(banner);
        System.out.println("Hello! I'm TryBot.");
        System.out.println("What can I do for you?");
        System.out.println(SEPARATOR);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            String trimmedCommand = command.trim();
            System.out.println(SEPARATOR);

            if (trimmedCommand.equalsIgnoreCase("bye") || trimmedCommand.equalsIgnoreCase("bye!")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(SEPARATOR);
                break;
            }

            try {
                if (trimmedCommand.isEmpty()) {
                    throw new TryBotException("I need a command. Try todo, list, or bye.");
                }

                if (trimmedCommand.equalsIgnoreCase("list")) {
                    printTaskList(tasks);
                } else if (isMarkCommand(trimmedCommand)) {
                    markTaskAsDone(trimmedCommand, tasks);
                } else if (isUnmarkCommand(trimmedCommand)) {
                    unmarkTask(trimmedCommand, tasks);
                } else if (isDeleteCommand(trimmedCommand)) {
                    deleteTask(trimmedCommand, tasks);
                } else if (isTodoCommand(trimmedCommand)) {
                    String description = getCommandBody(trimmedCommand, "todo");
                    if (description.isEmpty()) {
                        throw new TryBotException("A todo needs a description. Try: todo read book.");
                    }
                    addTask(new Todo(description), tasks);
                } else if (isDeadlineCommand(trimmedCommand)) {
                    addDeadlineTask(trimmedCommand, tasks);
                } else if (isEventCommand(trimmedCommand)) {
                    addEventTask(trimmedCommand, tasks);
                } else {
                    throw new TryBotException("I do not recognise that command. Try todo, list, or bye.");
                }
            } catch (TryBotException exception) {
                System.out.println(exception.getMessage());
            }

            System.out.println(SEPARATOR);
        }
    }

    /**
     * Checks whether a command starts with the mark keyword.
     *
     * @param command trimmed user command
     * @return true when the first command word is mark
     */
    private static boolean isMarkCommand(String command) {
        String[] commandParts = command.split("\\s+");
        return commandParts.length > 0 && commandParts[0].equalsIgnoreCase("mark");
    }

    /**
     * Checks whether a command starts with the unmark keyword.
     *
     * @param command trimmed user command
     * @return true when the first command word is unmark
     */
    private static boolean isUnmarkCommand(String command) {
        String[] commandParts = command.split("\\s+");
        return commandParts.length > 0 && commandParts[0].equalsIgnoreCase("unmark");
    }

    /**
     * Checks whether a command starts with the delete keyword.
     *
     * @param command trimmed user command
     * @return true when the first command word is delete
     */
    private static boolean isDeleteCommand(String command) {
        String[] commandParts = command.split("\\s+");
        return commandParts.length > 0 && commandParts[0].equalsIgnoreCase("delete");
    }

    /**
     * Checks whether a command starts with the todo keyword.
     *
     * @param command trimmed user command
     * @return true when the first command word is todo
     */
    private static boolean isTodoCommand(String command) {
        String[] commandParts = command.split("\\s+");
        return commandParts.length > 0 && (commandParts[0].equalsIgnoreCase("todo") || commandParts[0].equalsIgnoreCase("todo:"));
    }

    /**
     * Checks whether a command starts with the deadline keyword.
     *
     * @param command trimmed user command
     * @return true when the first command word is deadline
     */
    private static boolean isDeadlineCommand(String command) {
        String[] commandParts = command.split("\\s+");
        return commandParts.length > 0 && (commandParts[0].equalsIgnoreCase("deadline") ||  commandParts[0].equalsIgnoreCase("deadline:"));
    }

    /**
     * Checks whether a command starts with the event keyword.
     *
     * @param command trimmed user command
     * @return true when the first command word is event
     */
    private static boolean isEventCommand(String command) {
        String[] commandParts = command.split("\\s+");
        return commandParts.length > 0 && (commandParts[0].equalsIgnoreCase("event") || commandParts[0].equalsIgnoreCase("event:"));
    }

    /**
     * Extracts the text after a command keyword and removes an optional colon.
     *
     * @param command command text beginning with the keyword
     * @param keyword command keyword to remove
     * @return text after the keyword
     */
    private static String getCommandBody(String command, String keyword) {
        String body = command.substring(keyword.length()).trim();
        if (body.startsWith(":")) {
            body = body.substring(1).trim();
        }
        return body;
    }

    /**
     * Parses and adds a deadline command.
     *
     * @param command trimmed deadline command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the deadline format is invalid
     */
    private static void addDeadlineTask(String command, List<Task> tasks) throws TryBotException {
        int byIndex = command.toLowerCase().indexOf("/by");
        if (byIndex < 0) {
            throw new TryBotException("A deadline needs /by followed by a date or time. Example: deadline report /by Friday.");
        }

        String description = getCommandBody(command.substring(0, byIndex), "deadline");
        String by = command.substring(byIndex + "/by".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new TryBotException("A deadline needs both a description and a date or time. Example: deadline report /by Friday.");
        }

        try {
            addTask(new Deadline(description, by), tasks);
        } catch (IllegalArgumentException exception) {
            throw new TryBotException(exception.getMessage() == null
                    ? "The deadline date or time is invalid." : exception.getMessage());
        }
    }

    /**
     * Parses and adds an event command.
     *
     * @param command trimmed event command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the event format is invalid
     */
    private static void addEventTask(String command, List<Task> tasks) throws TryBotException {
        String lowerCaseCommand = command.toLowerCase();
        int fromIndex = lowerCaseCommand.indexOf("/from");
        int toIndex = lowerCaseCommand.indexOf("/to", fromIndex + "/from".length());
        if (fromIndex < 0 || toIndex < 0) {
            throw new TryBotException("An event needs /from and /to time details. Example: event meeting /from Monday /to Tuesday.");
        }

        String description = getCommandBody(command.substring(0, fromIndex), "event");
        String from = command.substring(fromIndex + "/from".length(), toIndex).trim();
        String to = command.substring(toIndex + "/to".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            throw new TryBotException("An event needs a description, start time, and end time. Example: event meeting /from Monday /to Tuesday.");
        }

        try {
            addTask(new Event(description, from, to), tasks);
        } catch (IllegalArgumentException exception) {
            throw new TryBotException(exception.getMessage() == null
                    ? "The event date or time is invalid." : exception.getMessage());
        }
    }

    /**
     * Adds a task and prints the standard confirmation message.
     *
     * @param task task to add
     * @param tasks tasks currently stored by TryBot
     */
    private static void addTask(Task task, List<Task> tasks) {
        tasks.add(task);
        saveTasks(tasks);
        System.out.println("Got it. I've added this task:");
        System.out.println(task);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Prints every task together with its completion status.
     *
     * @param tasks tasks currently stored by TryBot
     */
    private static void printTaskList(List<Task> tasks) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Marks the task number in a mark command as done.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void markTaskAsDone(String command, List<Task> tasks) throws TryBotException {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            throw new TryBotException("Mark needs one task number. Example: mark 1.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            throw new TryBotException("The task number must be a whole number. Example: mark 1.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        int taskIndex = taskNumber - 1;
        tasks.get(taskIndex).markAsDone();
        saveTasks(tasks);
        System.out.println("Good work!! I've marked this task as done:");
        System.out.println(tasks.get(taskIndex));
    }

    /**
     * Reverses the done status of the task number in an unmark command.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void unmarkTask(String command, List<Task> tasks) throws TryBotException {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            throw new TryBotException("Unmark needs one task number. Example: unmark 1.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            throw new TryBotException("The task number must be a whole number. Example: unmark 1.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        int taskIndex = taskNumber - 1;
        tasks.get(taskIndex).markAsNotDone();
        saveTasks(tasks);
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println(tasks.get(taskIndex));
    }

    /**
     * Deletes the task number supplied in a delete command.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void deleteTask(String command, List<Task> tasks) throws TryBotException {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            throw new TryBotException("Delete needs one task number. Example: delete 1.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            throw new TryBotException("The task number must be a whole number. Example: delete 1.");
        }

        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        Task removedTask = tasks.remove(taskNumber - 1);
        saveTasks(tasks);
        System.out.println("Noted. I've removed this task:");
        System.out.println(removedTask);
        System.out.println("Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Saves the current task list and turns file-system failures into a clear program error.
     *
     * @param tasks tasks currently stored by TryBot
     */
    private static void saveTasks(List<Task> tasks) {
        try {
            Storage.saveTasks(tasks);
        } catch (IOException exception) {
            System.err.println("Warning: TryBot could not save the task list. "
                    + "Your changes will be lost when TryBot exits.");
        }
    }

    /**
     * Loads the task list when TryBot starts.
     *
     * @return tasks saved by an earlier TryBot session
     */
    private static List<Task> loadTasks() {
        try {
            return Storage.loadTasks();
        } catch (IOException exception) {
            System.err.println("Warning: TryBot could not load the saved task list. "
                    + "Starting with an empty list.");
            return new ArrayList<>();
        }
    }
}
