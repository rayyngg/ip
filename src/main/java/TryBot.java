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

            if (trimmedCommand.equalsIgnoreCase("list")) {
                printTaskList(tasks, taskCount);
            } else if (isMarkCommand(trimmedCommand)) {
                markTaskAsDone(trimmedCommand, tasks, taskCount);
            } else if (isUnmarkCommand(trimmedCommand)) {
                unmarkTask(trimmedCommand, tasks, taskCount);
            } else if (isTodoCommand(trimmedCommand)) {
                String description = getTodoDescription(trimmedCommand);
                if (description.isEmpty()) {
                    System.out.println("A todo needs a description.");
                } else {
                    taskCount = addTask(new Todo(description), tasks, taskCount);
                }
            } else if (isDeadlineCommand(trimmedCommand)) {
                taskCount = addDeadlineTask(trimmedCommand, tasks, taskCount);
            } else if (isEventCommand(trimmedCommand)) {
                taskCount = addEventTask(trimmedCommand, tasks, taskCount);
            } else if (taskCount < MAX_TASKS) {
                tasks[taskCount] = new Task(command);
                taskCount++;
                System.out.println("TryBot has added the task: " + command);
            } else {
                System.out.println("Sorry, your task list is full.");
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
     * Extracts the description from a todo command.
     *
     * @param command trimmed todo command
     * @return todo description
     */
    private static String getTodoDescription(String command) {
        return command.substring("todo".length()).trim();
    }

    /**
     * Parses and adds a deadline command.
     *
     * @param command trimmed deadline command
     * @param tasks tasks currently stored by TryBot
     * @param taskCount number of stored tasks
     * @return updated number of stored tasks
     */
    private static int addDeadlineTask(String command, Task[] tasks, int taskCount) {
        int byIndex = command.toLowerCase().indexOf("/by");
        if (byIndex < 0) {
            System.out.println("A deadline must include /by followed by a date or time.");
            return taskCount;
        }

        String description = command.substring("deadline".length(), byIndex).trim();
        String by = command.substring(byIndex + "/by".length()).trim();
        if (description.isEmpty() || by.isEmpty()) {
            System.out.println("A deadline needs both a description and a date or time.");
            return taskCount;
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
    private static int addEventTask(String command, Task[] tasks, int taskCount) {
        String lowerCaseCommand = command.toLowerCase();
        int fromIndex = lowerCaseCommand.indexOf("/from");
        int toIndex = lowerCaseCommand.indexOf("/to", fromIndex + "/from".length());
        if (fromIndex < 0 || toIndex < 0) {
            System.out.println("An event must include /from and /to followed by date or time details.");
            return taskCount;
        }

        String description = command.substring("event".length(), fromIndex).trim();
        String from = command.substring(fromIndex + "/from".length(), toIndex).trim();
        String to = command.substring(toIndex + "/to".length()).trim();
        if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
            System.out.println("An event needs a description, start time, and end time.");
            return taskCount;
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
    private static int addTask(Task task, Task[] tasks, int taskCount) {
        if (taskCount >= MAX_TASKS) {
            System.out.println("Sorry, your task list is full.");
            return taskCount;
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
    private static void markTaskAsDone(String command, Task[] tasks, int taskCount) {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            System.out.println("Maybe try telling me a number?");
            return;
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            System.out.println("Maybe try telling me a number?");
            return;
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            System.out.println("Hmm. That task number does not seem to exist.");
            return;
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
    private static void unmarkTask(String command, Task[] tasks, int taskCount) {
        String[] commandParts = command.split("\\s+");
        if (commandParts.length != 2) {
            System.out.println("Maybe try telling me a number?");
            return;
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[1]);
        } catch (NumberFormatException exception) {
            System.out.println("Maybe try telling me a number?");
            return;
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            System.out.println("Hmm. That task number does not seem to exist.");
            return;
        }

        int taskIndex = taskNumber - 1;
        tasks[taskIndex].markAsNotDone();
        System.out.println("OK, I've marked this task as not done yet:");
        System.out.println(tasks[taskIndex]);
    }
}
