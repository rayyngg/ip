/**
 * A task that must be completed by a specified date or time.
 */
public class Deadline extends Task {
    protected String by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description text describing the task
     * @param by date or time by which the task should be completed
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Formats this deadline for the task data file.
     *
     * @return the task type, completion status, description, and deadline
     */
    @Override
    public String toStorageString() {
        return "D | " + (isDone ? "1" : "0") + " | " + escapeStorageField(description)
                + " | " + escapeStorageField(by);
    }

    /**
     * Formats this task with its deadline.
     *
     * @return formatted deadline task
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
