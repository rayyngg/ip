/**
 * Adds a deadline task.
 */
public class AddDeadlineCommand extends AddCommand {
    private final String description;
    private final String by;

    /**
     * Creates a deadline command.
     *
     * @param deadline parsed deadline fields
     */
    public AddDeadlineCommand(Parser.ParsedDeadline deadline) {
        this.description = deadline.description();
        this.by = deadline.by();
    }

    @Override
    protected Task createTask() throws TryBotException {
        try {
            return new Deadline(description, by);
        } catch (IllegalArgumentException exception) {
            throw new TryBotException(exception.getMessage() == null
                    ? "The deadline date or time is invalid." : exception.getMessage());
        }
    }
}
