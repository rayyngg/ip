package trybot.gui;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/** A conversation message containing a speaker icon and text. */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image image) {
        try {
            FXMLLoader loader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load a conversation message.", exception);
        }

        dialog.setText(text);
        displayPicture.setImage(image);
    }

    /** Creates a right-aligned message for the user. */
    public static DialogBox getUserDialog(String text, Image image) {
        return new DialogBox(text, image);
    }

    /** Creates a left-aligned message for TryBot. */
    public static DialogBox getTryBotDialog(String text, Image image) {
        DialogBox dialogBox = new DialogBox(text, image);
        dialogBox.getChildren().removeAll(dialogBox.dialog, dialogBox.displayPicture);
        dialogBox.getChildren().addAll(dialogBox.displayPicture, dialogBox.dialog);
        dialogBox.setAlignment(Pos.TOP_LEFT);
        return dialogBox;
    }
}
