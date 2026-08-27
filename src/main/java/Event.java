/**
 * A task with a specified start and end date or time.
 */
public class Event extends Task {
    protected String from;
    protected String to;

    /**
     * Creates an incomplete event task.
     *
     * @param description text describing the event
     * @param from date or time when the event starts
     * @param to date or time when the event ends
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Formats this event for the task data file.
     *
     * @return the task type, completion status, description, and event times
     */
    @Override
    public String toStorageString() {
        return "E | " + (isDone ? "1" : "0") + " | " + escapeStorageField(description)
                + " | " + escapeStorageField(from) + " | " + escapeStorageField(to);
    }

    /**
     * Formats this task with its start and end times.
     *
     * @return formatted event task
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }
}
