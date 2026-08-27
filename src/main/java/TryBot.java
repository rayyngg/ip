import java.io.IOException;

/**
 * Coordinates TryBot's user interface, parser, task list, and storage.
 */
public class TryBot {
    private final Ui ui;
    private final Storage storage;
    private final Parser parser;
    private final TaskList tasks;

    /**
     * Creates TryBot using the default task data file.
     */
    public TryBot() {
        this("data/trybot.txt");
    }

    /**
     * Creates TryBot using the specified task data file.
     *
     * @param filePath path to the task data file
     */
    public TryBot(String filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        parser = new Parser();
        TaskList loadedTasks;
        try {
            loadedTasks = new TaskList(storage.loadTasks());
        } catch (IOException exception) {
            ui.showLoadingError();
            loadedTasks = new TaskList();
        }
        tasks = loadedTasks;
    }

    /**
     * Runs the command loop until the user exits or input ends.
     */
    public void run() {
        ui.showWelcome();
        while (ui.hasMoreCommands()) {
            String fullCommand = ui.readCommand();
            ui.showLine();
            Command command = null;
            try {
                command = parser.parse(fullCommand);
                command.execute(tasks, ui, storage);
            } catch (TryBotException exception) {
                ui.showError(exception.getMessage());
            } finally {
                ui.showLine();
            }
            if (command != null && command.isExit()) {
                break;
            }
        }
    }

    /**
     * Starts TryBot with its default task file.
     *
     * @param args command-line arguments, currently unused
     */
    public static void main(String[] args) {
        new TryBot().run();
    }
}
