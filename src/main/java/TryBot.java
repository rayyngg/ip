import java.io.IOException;
import java.util.Scanner;

/**
 * A simple chatbot that stores tasks until the user says goodbye.
 */
public class TryBot {
    public static void main(String[] args) {
        Ui ui = new Ui();
        Parser parser = new Parser();
        TaskList tasks = loadTasks(ui);

        ui.showWelcome();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();
            ui.showLine();

            Parser.ParsedCommand parsedCommand = parser.parse(command);
            if (parsedCommand.type() == Parser.CommandType.BYE) {
                ui.showGoodbye();
                break;
            }

            try {
                if (parsedCommand.type() == Parser.CommandType.EMPTY) {
                    throw new TryBotException("I need a command. Try todo, list, or bye.");
                }

                if (parsedCommand.type() == Parser.CommandType.LIST) {
                    ui.showTaskList(tasks);
                } else if (parsedCommand.type() == Parser.CommandType.MARK) {
                    markTaskAsDone(parsedCommand.body(), tasks, ui);
                } else if (parsedCommand.type() == Parser.CommandType.UNMARK) {
                    unmarkTask(parsedCommand.body(), tasks, ui);
                } else if (parsedCommand.type() == Parser.CommandType.DELETE) {
                    deleteTask(parsedCommand.body(), tasks, ui);
                } else if (parsedCommand.type() == Parser.CommandType.TODO) {
                    String description = parsedCommand.body();
                    if (description.isEmpty()) {
                        throw new TryBotException("A todo needs a description. Try: todo read book.");
                    }
                    addTask(new Todo(description), tasks, ui);
                } else if (parsedCommand.type() == Parser.CommandType.DEADLINE) {
                    addDeadlineTask(parsedCommand.body(), tasks, ui);
                } else if (parsedCommand.type() == Parser.CommandType.EVENT) {
                    addEventTask(parsedCommand.body(), tasks, ui);
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
     * Parses and adds a deadline command.
     *
     * @param body normalized text after the deadline keyword
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the deadline format is invalid
     */
    private static void addDeadlineTask(String body, TaskList tasks, Ui ui) throws TryBotException {
        int byIndex = body.toLowerCase().indexOf("/by");
        if (byIndex < 0) {
            throw new TryBotException("A deadline needs /by followed by a date or time. Example: deadline report /by Friday.");
        }

        String description = body.substring(0, byIndex).trim();
        String by = body.substring(byIndex + "/by".length()).trim();
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
     * @param body normalized text after the event keyword
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the event format is invalid
     */
    private static void addEventTask(String body, TaskList tasks, Ui ui) throws TryBotException {
        String lowerCaseCommand = body.toLowerCase();
        int fromIndex = lowerCaseCommand.indexOf("/from");
        int toIndex = lowerCaseCommand.indexOf("/to", fromIndex + "/from".length());
        if (fromIndex < 0 || toIndex < 0) {
            throw new TryBotException("An event needs /from and /to time details. Example: event meeting /from Monday /to Tuesday.");
        }

        String description = body.substring(0, fromIndex).trim();
        String from = body.substring(fromIndex + "/from".length(), toIndex).trim();
        String to = body.substring(toIndex + "/to".length()).trim();
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
     * @param body normalized text after the mark keyword
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void markTaskAsDone(String body, TaskList tasks, Ui ui) throws TryBotException {
        String[] commandParts = body.split("\\s+");
        if (body.isEmpty() || commandParts.length != 1) {
            throw new TryBotException("Mark needs one task number. Example: mark 1.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[0]);
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
     * @param body normalized text after the unmark keyword
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void unmarkTask(String body, TaskList tasks, Ui ui) throws TryBotException {
        String[] commandParts = body.split("\\s+");
        if (body.isEmpty() || commandParts.length != 1) {
            throw new TryBotException("Unmark needs one task number. Example: unmark 1.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[0]);
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
     * @param body normalized text after the delete keyword
     * @param tasks tasks currently stored by TryBot
     * @throws TryBotException if the command has no valid task number
     */
    private static void deleteTask(String body, TaskList tasks, Ui ui) throws TryBotException {
        String[] commandParts = body.split("\\s+");
        if (body.isEmpty() || commandParts.length != 1) {
            throw new TryBotException("Delete needs one task number. Example: delete 1.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(commandParts[0]);
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
