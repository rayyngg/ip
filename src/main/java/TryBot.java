import java.util.Scanner;

/**
 * A simple chatbot that stores tasks until the user says goodbye.
 */
public class TryBot {
    private static final int MAX_TASKS = 100;
    private static final String SEPARATOR = "____________________________________________________________";

    public static void main(String[] args) {
        String banner = " _____             ____        _\n"
                + "|_   _| _ __ _   _ | __ )  ___ | |_\n"
                + "  | |  | '__| | | ||  _ \\ / _ \\| __|\n"
                + "  | |  | |  | |_| || |_) | (_) | |_\n"
                + "  |_|  |_|   \\__, ||____/ \\___/ \\__|\n"
                + "              |___/\n";

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

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
                    printTaskList(tasks, taskCount);
                } else if (isMarkCommand(trimmedCommand)) {
                    markTaskAsDone(trimmedCommand, tasks, taskCount);
                } else if (isUnmarkCommand(trimmedCommand)) {
                    unmarkTask(trimmedCommand, tasks, taskCount);
                } else if (isTodoCommand(trimmedCommand)) {
                    String description = getCommandBody(trimmedCommand, "todo");
                    if (description.isEmpty()) {
                        throw new TryBotException("A todo needs a description. Try: todo read book.");
                    }
                    taskCount = addTask(new Todo(description), tasks, taskCount);
                } else if (isDeadlineCommand(trimmedCommand)) {
                    taskCount = addDeadlineTask(trimmedCommand, tasks, taskCount);
                } else if (isEventCommand(trimmedCommand)) {
                    taskCount = addEventTask(trimmedCommand, tasks, taskCount);
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
     * @param taskCount number of stored tasks
     * @return updated number of stored tasks
     */
    private static int addDeadlineTask(String command, Task[] tasks, int taskCount) throws TryBotException {
        int byIndex = command.toLowerCase().indexOf("/by");
        if (byIndex < 0) {
            throw new TryBotException("A deadline needs /by followed by a date or time. Example: deadline report /by Friday.");
        }

        String description = getCommandBody(command.substring(0, byIndex), "deadline");
        String by = command.substring(byIndex + "/by".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            throw new TryBotException("A deadline needs both a description and a date or time. Example: deadline report /by Friday.");
        }

        return addTask(new Deadline(description, by), tasks, taskCount);
    }

    /**
     * Parses and adds an event command.
     *
     * @param command trimmed event command
     * @param tasks tasks currently stored by TryBot
     * @param taskCount number of stored tasks
     * @return updated number of stored tasks
     */
    private static int addEventTask(String command, Task[] tasks, int taskCount) throws TryBotException {
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

        return addTask(new Event(description, from, to), tasks, taskCount);
    }

    /**
     * Adds a task and prints the standard confirmation message.
     *
     * @param task task to add
     * @param tasks tasks currently stored by TryBot
     * @param taskCount number of stored tasks
     * @return updated number of stored tasks
     */
    private static int addTask(Task task, Task[] tasks, int taskCount) throws TryBotException {
        if (taskCount >= MAX_TASKS) {
            throw new TryBotException("I cannot add another task because your task list is full.");
        }

        tasks[taskCount] = task;
        int updatedTaskCount = taskCount + 1;
        System.out.println("Got it. I've added this task:");
        System.out.println(task);
        System.out.println("Now you have " + updatedTaskCount + " tasks in the list.");
        return updatedTaskCount;
    }

    /**
     * Prints every task together with its completion status.
     *
     * @param tasks tasks currently stored by TryBot
     * @param taskCount number of stored tasks
     */
    private static void printTaskList(Task[] tasks, int taskCount) {
        System.out.println("Here are the tasks in your list:");
        for (int i = 0; i < taskCount; i++) {
            System.out.println((i + 1) + "." + tasks[i]);
        }
    }

    /**
     * Marks the task number in a mark command as done.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @param taskCount number of stored tasks
     */
    private static void markTaskAsDone(String command, Task[] tasks, int taskCount) throws TryBotException {
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

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        int taskIndex = taskNumber - 1;
        tasks[taskIndex].markAsDone();
        System.out.println("Good work!! I've marked this task as done:");
        System.out.println(tasks[taskIndex]);
    }

    /**
     * Reverses the done status of the task number in an unmark command.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @param taskCount number of stored tasks
     */
    private static void unmarkTask(String command, Task[] tasks, int taskCount) throws TryBotException {
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

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        int taskIndex = taskNumber - 1;
        tasks[taskIndex].markAsNotDone();
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println(tasks[taskIndex]);
    }
}
