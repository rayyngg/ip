package trybot.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import trybot.task.Deadline;
import trybot.task.Event;
import trybot.task.Task;
import trybot.task.Todo;

/**
 * Tests persistence, round-trip escaping, and malformed-record handling.
 */
class StorageTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void loadTasks_missingFile_returnsEmptyList() throws IOException {
        Storage storage = new Storage(temporaryDirectory.resolve("missing.txt"));

        assertTrue(storage.loadTasks().isEmpty());
    }

    @Test
    void saveTasks_andLoadTasks_roundTripsAllTaskTypesAndEscaping() throws IOException {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        Storage storage = new Storage(taskFile);
        Todo todo = new Todo("read \\ book | notes\nnow");
        todo.markAsDone();
        Deadline deadline = new Deadline("submit report", "2024-02-29 1830");
        Event event = new Event("team meeting", "Monday", "Tuesday");
        List<Task> original = List.of(todo, deadline, event);

        storage.saveTasks(original);
        List<Task> loaded = storage.loadTasks();

        assertEquals(3, loaded.size());
        assertEquals(original.get(0).toString(), loaded.get(0).toString());
        assertEquals(original.get(1).toStorageString(), loaded.get(1).toStorageString());
        assertEquals(original.get(2).toStorageString(), loaded.get(2).toStorageString());
        assertEquals("T | 1 | read \\\\ book \\| notes\\nnow", Files.readAllLines(taskFile).get(0));
    }

    @Test
    void loadTasks_malformedAndInvalidRecords_ignoresThoseRecords() throws IOException {
        Path taskFile = temporaryDirectory.resolve("tasks.txt");
        Files.write(taskFile, List.of(
                "",
                "not a task record",
                "T | 2 | invalid status",
                "D | 0 | missing date",
                "E | 1 | missing end | Monday",
                "T | 1 | valid todo",
                "D | 0 | valid deadline | Friday",
                "E | 1 | valid event | Monday | Tuesday"));

        List<Task> loaded = new Storage(taskFile).loadTasks();

        assertEquals(3, loaded.size());
        assertEquals("[T][X] valid todo", loaded.get(0).toString());
        assertEquals("[D][ ] valid deadline (by: Friday)", loaded.get(1).toString());
        assertEquals("[E][X] valid event (from: Monday to: Tuesday)", loaded.get(2).toString());
    }
}
