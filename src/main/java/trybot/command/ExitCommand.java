package trybot.command;

import trybot.storage.Storage;
import trybot.task.TaskList;
import trybot.ui.Ui;

/**
 * Ends the TryBot session.
 */
public class ExitCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showGoodbyeMessage();
    }

    @Override
    public boolean isExit() {
        return true;
    }
}
