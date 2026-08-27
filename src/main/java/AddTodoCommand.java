/**
 * Adds a todo task.
 */
public class AddTodoCommand extends AddCommand {
    private final String description;

    /**
     * Creates a todo command.
     *
     * @param description todo description
     */
    public AddTodoCommand(String description) {
        this.description = description;
    }

    @Override
    protected Task createTask() throws TryBotException {
        if (description.isEmpty()) {
            throw new TryBotException("A todo needs a description. Try: todo read book.");
        }
        return new Todo(description);
    }
}
