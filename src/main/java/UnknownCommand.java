/**
 * Reports that the user entered an unsupported command.
 */
public class UnknownCommand extends Command {
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws TryBotException {
        throw new TryBotException("I do not recognise that command. Try todo, list, or bye.");
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
