package trybot.command;

import trybot.exception.TryBotException;
import trybot.storage.Storage;
import trybot.task.Task;
import trybot.task.TaskList;
import trybot.ui.Ui;

/**
 * Marks one task as not done.
 */
public class UnmarkCommand extends Command {
    private final int taskNumber;

    /**
     * Creates an unmark command.
     *
     * @param taskNumber one-based task number
     */
    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws TryBotException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        Task task = tasks.get(taskNumber - 1);
        task.markAsNotDone();
        saveTasks(tasks, ui, storage);
        ui.showTaskMarkedNotDone(task);
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
