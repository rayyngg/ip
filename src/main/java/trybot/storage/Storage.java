package trybot.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import trybot.task.Deadline;
import trybot.task.Event;
import trybot.task.Task;
import trybot.task.Todo;

/**
 * Saves TryBot's task list to a file on disk.
 */
public class Storage {
    private final Path taskFile;

    /**
     * Creates storage using TryBot's default task file.
     */
    public Storage() {
        this(Path.of("data", "trybot.txt"));
    }

    /**
     * Creates storage using the specified task file.
     *
     * @param filePath path to the task file.
     */
    public Storage(String filePath) {
        this(Path.of(filePath));
    }

    /**
     * Creates storage using the specified task file.
     *
     * @param taskFile path to the task file.
     */
    public Storage(Path taskFile) {
        if (taskFile == null) {
            throw new IllegalArgumentException("The task file path cannot be null.");
        }
        this.taskFile = taskFile;
    }

    /**
     * Replaces the saved task data with the current task list.
     *
     * @param tasks tasks to save.
     * @throws IOException if the task file cannot be created or written
     */
    public void saveTasks(List<Task> tasks) throws IOException {
        if (tasks == null) {
            throw new IllegalArgumentException("Tasks cannot be null.");
        }
        if (tasks.stream().anyMatch(task -> task == null)) {
            throw new IllegalArgumentException("The task list cannot contain null tasks.");
        }

        Files.createDirectories(taskFile.getParent());

        List<String> fileLines = tasks.stream()
                .map(Task::toStorageString)
                .toList();
        Path temporaryFile = Files.createTempFile(taskFile.getParent(), "trybot-", ".tmp");
        boolean moved = false;
        try {
            Files.write(temporaryFile, fileLines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporaryFile, taskFile, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, taskFile, StandardCopyOption.REPLACE_EXISTING);
            }
            moved = true;
        } finally {
            if (!moved) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // Preserve the original write failure when temporary-file cleanup also fails.
                }
            }
        }
    }

    /**
     * Loads all valid tasks from the saved task data.
     *
     * @return tasks restored from disk, or an empty list when no file exists
     * @throws IOException if the task file cannot be read
     */
    public List<Task> loadTasks() throws IOException {
        List<Task> tasks = new ArrayList<>();
        if (Files.notExists(taskFile)) {
            return tasks;
        }
        if (!Files.isRegularFile(taskFile)) {
            throw new IOException("The task data path is not a regular file.");
        }

        for (String line : Files.readAllLines(taskFile, StandardCharsets.UTF_8)) {
            Task task = parseTask(line);
            if (task != null) {
                tasks.add(task);
            }
        }
        return tasks;
    }

    /**
     * Converts one saved line into a task.
     *
     * @param line saved task data.
     * @return the parsed task, or null when the line is blank or malformed
     */
    private static Task parseTask(String line) {
        if (line.isBlank()) {
            return null;
        }

        List<String> fields = splitFields(line);
        if (fields == null || fields.size() < 3) {
            return null;
        }

        String taskType = unescapeField(fields.get(0));
        String status = unescapeField(fields.get(1));
        String description = unescapeField(fields.get(2));
        if (taskType == null || status == null || description == null) {
            return null;
        }
        taskType = taskType.trim().replace("\uFEFF", "").toUpperCase(Locale.ROOT);
        status = status.trim();
        description = description.trim();
        if (!(status.equals("0") || status.equals("1"))) {
            return null;
        }

        Task task;
        try {
            switch (taskType) {
                case "T":
                    task = fields.size() == 3 && !description.isEmpty() ? new Todo(description) : null;
                    break;
                case "D":
                    String by = fields.size() == 4 ? unescapeField(fields.get(3)) : null;
                    task = by != null && !description.isEmpty() && !by.trim().isEmpty()
                            ? new Deadline(description, by.trim()) : null;
                    break;
                case "E":
                    String from = fields.size() == 5 ? unescapeField(fields.get(3)) : null;
                    String to = fields.size() == 5 ? unescapeField(fields.get(4)) : null;
                    task = from != null && to != null && !description.isEmpty()
                            && !from.trim().isEmpty() && !to.trim().isEmpty()
                            ? new Event(description, from.trim(), to.trim()) : null;
                    break;
                default:
                    task = null;
            }
        } catch (IllegalArgumentException exception) {
            return null;
        }

        if (task != null && status.equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Splits a saved line at unescaped field separators.
     *
     * @param line saved task data.
     * @return fields, or null when the line ends with an incomplete escape
     */
    private static List<String> splitFields(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (escaped) {
                currentField.append('\\').append(character);
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '|') {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(character);
            }
        }
        if (escaped) {
            return null;
        }
        fields.add(currentField.toString());
        return fields;
    }

    /**
     * Decodes an escaped field from the task data file.
     *
     * @param field escaped field text.
     * @return decoded field, or null when the escape sequence is incomplete
     */
    private static String unescapeField(String field) {
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < field.length(); i++) {
            char character = field.charAt(i);
            if (character != '\\') {
                decoded.append(character);
                continue;
            }
            if (i + 1 >= field.length()) {
                return null;
            }
            char escapedCharacter = field.charAt(++i);
            switch (escapedCharacter) {
                case '\\':
                    // Fallthrough
                case '|':
                    decoded.append(escapedCharacter);
                    break;
                case 'n':
                    decoded.append('\n');
                    break;
                case 'r':
                    decoded.append('\r');
                    break;
                default:
                    decoded.append('\\').append(escapedCharacter);
            }
        }
        return decoded.toString();
    }
}
