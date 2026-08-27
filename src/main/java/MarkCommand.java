/**
 * Marks one task as done.
 */
public class MarkCommand extends Command {
    private final int taskNumber;

    /**
     * Creates a mark command.
     *
     * @param taskNumber one-based task number
     */
    public MarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws TryBotException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        Task task = tasks.get(taskNumber - 1);
        task.markAsDone();
        saveTasks(tasks, ui, storage);
        ui.showTaskMarkedDone(task);
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
