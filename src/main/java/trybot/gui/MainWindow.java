package trybot.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import trybot.TryBot;

/** Controller for the main TryBot conversation window. */
public class MainWindow {
    @FXML
    private AnchorPane root;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;

    private TryBot tryBot;
    private final Image userImage = new Image(MainWindow.class.getResourceAsStream("/images/human.png"));
    private final Image tryBotImage = new Image(MainWindow.class.getResourceAsStream("/images/trybot.png"));
    private final Image backgroundImage = new Image(MainWindow.class
            .getResourceAsStream("/images/background.png"));

    /** Connects the window to the application logic. */
    public void setTryBot(TryBot tryBot) {
        this.tryBot = tryBot;
        addBotMessage("Hello! I'm TryBot.\nWhat can I do for you?");
    }

    /** Scrolls the conversation to the newest message after the window is loaded. */
    @FXML
    private void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        root.setBackground(new Background(new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                new BackgroundSize(100, 100, true, true, false, true))));
    }

    /** Sends the text in the input field to TryBot and displays both sides of the exchange. */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText().trim();
        if (input.isEmpty() || tryBot == null) {
            return;
        }

        dialogContainer.getChildren().add(DialogBox.getUserDialog(input, userImage));
        String response = tryBot.getResponse(input);
        addBotMessage(response);
        userInput.clear();
    }

    private void addBotMessage(String response) {
        DialogBox dialogBox = DialogBox.getTryBotDialog(response, tryBotImage);
        dialogBox.setAlignment(Pos.TOP_LEFT);
        dialogContainer.getChildren().add(dialogBox);
    }
}
