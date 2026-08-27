package trybot.command;

import trybot.storage.Storage;
import trybot.task.TaskList;
import trybot.ui.Ui;

/**
 * Displays the current task list.
 */
public class ListCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskList(tasks);
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
