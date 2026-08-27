package trybot.task;

/**
 * Represents a task stored by TryBot.
 */
public class Task {
    protected String description;
    protected boolean isDone;

    /**
     * Creates a task that is initially not done.
     *
     * @param description text describing the task
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the symbol used to display this task's completion status.
     *
     * @return X for a completed task, or a space for an incomplete task
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /**
     * Marks this task as done.
     */
    public void markAsDone() {
        isDone = true;
    }

    /**
     * Marks this task as not done.
     */
    public void markAsNotDone() {
        isDone = false;
    }

    /**
     * Returns the task description.
     *
     * @return task description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Formats this task for the task data file.
     *
     * @return the task type, completion status, and description
     */
    public String toStorageString() {
        return "T | " + (isDone ? "1" : "0") + " | " + escapeStorageField(description);
    }

    /**
     * Escapes characters that have a special meaning in the task data file.
     *
     * @param field field text to escape
     * @return escaped field text
     */
    protected String escapeStorageField(String field) {
        if (field == null) {
            throw new IllegalArgumentException("Task data fields cannot be null.");
        }
        return field.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    /**
     * Formats this task with its completion status.
     *
     * @return status icon and task description
     */
    @Override
    public String toString() {
        return "[" + getStatusIcon() + "] " + description;
    }
}
