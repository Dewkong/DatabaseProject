package agh.cs.projekt.utils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;

//an javafx alert which can't be closed unless enableClose() is called
public class PersistentAlert extends Alert {

    Node okButton;

    public PersistentAlert(AlertType alertType, String title, String text) {
        super(alertType);

        this.setTitle(title);
        this.setHeaderText(text);

        this.initStyle(StageStyle.UNDECORATED); //remove close button

        okButton = this.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);
        okButton.setVisible(false);
    }

    public void enableClose(){
        okButton.setDisable(false);
        okButton.setVisible(true);
    }

}
