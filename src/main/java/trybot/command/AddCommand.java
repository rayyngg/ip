package trybot.command;

import trybot.exception.TryBotException;
import trybot.storage.Storage;
import trybot.task.Task;
import trybot.task.TaskList;
import trybot.ui.Ui;

/**
 * Base class for commands that create and add one task.
 */
public abstract class AddCommand extends Command {
    /**
     * Creates the task represented by this command.
     *
     * @return task to add
     * @throws TryBotException if the task fields are invalid
     */
    protected abstract Task createTask() throws TryBotException;

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws TryBotException {
        Task task = createTask();
        tasks.add(task);
        saveTasks(tasks, ui, storage);
        ui.showTaskAdded(task, tasks.size());
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
