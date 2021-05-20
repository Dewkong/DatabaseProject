package agh.cs.projekt;

import agh.cs.projekt.models.ApplicationUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SomeFXMLController implements Initializable {

    @FXML
    private Label label;
    @FXML
    private Button logoutButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UserHolder userHolder = UserHolder.getInstance();
        ApplicationUser user = userHolder.getUser();
        label.setText("This is a different scene.\n" +
                "There's not much to it yet.\n" +
                "But one day, there might be.\n" +
                "\n" +
                "And the currently logged in user is the one with\n" +
                "Login: " + user.getLogin() + "\n" +
                "CustomerID: " + user.getCustomer().getId());
        label.setTextAlignment(TextAlignment.CENTER);
        logoutButton.setText("Wyloguj");
    }

    public void logout(ActionEvent actionEvent) throws IOException {
        UserHolder userHolder = UserHolder.getInstance();
        userHolder.removeUser();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login_screen.fxml"));
        logoutButton.getScene().setRoot(root);
    }
}
