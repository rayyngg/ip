/**
 * Deletes one task from the task list.
 */
public class DeleteCommand extends Command {
    private final int taskNumber;

    /**
     * Creates a delete command.
     *
     * @param taskNumber one-based task number
     */
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws TryBotException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new TryBotException("That task number does not exist. Use list to see your task numbers.");
        }

        Task removedTask = tasks.remove(taskNumber - 1);
        saveTasks(tasks, ui, storage);
        ui.showTaskDeleted(removedTask, tasks.size());
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
