package trybot.command;

import trybot.exception.TryBotException;
import trybot.storage.Storage;
import trybot.task.TaskList;
import trybot.ui.Ui;

/**
 * Reports that the user entered a blank command.
 */
public class EmptyCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws TryBotException {
        throw new TryBotException("I need a command. Try todo, list, or bye.");
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
