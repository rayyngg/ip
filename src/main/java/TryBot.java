import java.io.IOException;
import java.util.Scanner;

/**
 * A simple chatbot that stores tasks until the user says goodbye.
 */
public class TryBot {
    public static void main(String[] args) {
        Ui ui = new Ui();
        TaskList tasks = loadTasks(ui);

        ui.showWelcome();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            String trimmedCommand = command.trim();
            ui.showLine();

            if (trimmedCommand.equalsIgnoreCase("bye") || trimmedCommand.equalsIgnoreCase("bye!")) {
                ui.showGoodbye();
                break;
            }

            try {
                if (trimmedCommand.isEmpty()) {
                    throw new TryBotException("I need a command. Try todo, list, or bye.");
                }

                if (trimmedCommand.equalsIgnoreCase("list")) {
                    ui.showTaskList(tasks);
                } else if (isMarkCommand(trimmedCommand)) {
                    markTaskAsDone(trimmedCommand, tasks, ui);
                } else if (isUnmarkCommand(trimmedCommand)) {
                    unmarkTask(trimmedCommand, tasks, ui);
                } else if (isDeleteCommand(trimmedCommand)) {
                    deleteTask(trimmedCommand, tasks, ui);
                } else if (isTodoCommand(trimmedCommand)) {
                    String description = getCommandBody(trimmedCommand, "todo");
                    if (description.isEmpty()) {
                        throw new TryBotException("A todo needs a description. Try: todo read book.");
                    }
                    addTask(new Todo(description), tasks, ui);
                } else if (isDeadlineCommand(trimmedCommand)) {
                    addDeadlineTask(trimmedCommand, tasks, ui);
                } else if (isEventCommand(trimmedCommand)) {
                    addEventTask(trimmedCommand, tasks, ui);
                } else {
                    throw new TryBotException("I do not recognise that command. Try todo, list, or bye.");
                }
            } catch (TryBotException exception) {
                ui.showError(exception.getMessage());
            }

            ui.showLine();
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
    private static void addDeadlineTask(String command, TaskList tasks, Ui ui) throws TryBotException {
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
            addTask(new Deadline(description, by), tasks, ui);
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
    private static void addEventTask(String command, TaskList tasks, Ui ui) throws TryBotException {
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
            addTask(new Event(description, from, to), tasks, ui);
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
    private static void addTask(Task task, TaskList tasks, Ui ui) {
        tasks.add(task);
        saveTasks(tasks, ui);
        ui.showTaskAdded(task, tasks.size());
    }

    /**
     * Marks the task number in a mark command as done.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void markTaskAsDone(String command, TaskList tasks, Ui ui) throws TryBotException {
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
        saveTasks(tasks, ui);
        ui.showTaskMarkedDone(tasks.get(taskIndex));
    }

    /**
     * Reverses the done status of the task number in an unmark command.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void unmarkTask(String command, TaskList tasks, Ui ui) throws TryBotException {
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
        saveTasks(tasks, ui);
        ui.showTaskMarkedNotDone(tasks.get(taskIndex));
    }

    /**
     * Deletes the task number supplied in a delete command.
     *
     * @param command trimmed user command
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void deleteTask(String command, TaskList tasks, Ui ui) throws TryBotException {
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
        saveTasks(tasks, ui);
        ui.showTaskDeleted(removedTask, tasks.size());
    }

    /**
     * Saves the current task list and turns file-system failures into a clear program error.
     *
     * @param tasks tasks currently stored by TryBot
     */
    private static void saveTasks(TaskList tasks, Ui ui) {
        try {
            Storage.saveTasks(tasks.toList());
        } catch (IOException exception) {
            ui.showSavingError();
        }
    }

    /**
     * Loads the task list when TryBot starts.
     *
     * @return tasks saved by an earlier TryBot session
     */
    private static TaskList loadTasks(Ui ui) {
        try {
            return new TaskList(Storage.loadTasks());
        } catch (IOException exception) {
            ui.showLoadingError();
            return new TaskList();
        }
    }
}
