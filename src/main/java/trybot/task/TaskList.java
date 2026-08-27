package trybot.task;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the ordered collection of tasks managed by TryBot.
 */
public class TaskList {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing a copy of the supplied tasks.
     *
     * @param tasks initial tasks.
     * @throws IllegalArgumentException if the supplied list or one of its tasks is null
     */
    public TaskList(List<Task> tasks) {
        if (tasks == null || tasks.stream().anyMatch(task -> task == null)) {
            throw new IllegalArgumentException("A task list cannot contain null tasks.");
        }
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task task to add.
     * @throws IllegalArgumentException if the task is null
     */
    public void add(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("A task list cannot contain null tasks.");
        }
        tasks.add(task);
    }

    /**
     * Gets a task at a zero-based index.
     *
     * @param index zero-based task index.
     * @return task at the requested index
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Removes and returns a task at a zero-based index.
     *
     * @param index zero-based task index.
     * @return removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns an immutable snapshot for persistence.
     *
     * @return immutable copy of the current tasks
     */
    public List<Task> toList() {
        return List.copyOf(tasks);
    }
}
