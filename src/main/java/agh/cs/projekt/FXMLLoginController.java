package agh.cs.projekt;

import agh.cs.projekt.models.ApplicationUser;
import agh.cs.projekt.utils.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import org.hibernate.Session;

import javax.persistence.Query;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLLoginController implements Initializable {

    @FXML
    private Label title;
    @FXML
    private Label loginLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private TextField loginTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Button loginButton;
    @FXML
    private Label registerLabel;
    @FXML
    private Button registerButton;
    @FXML
    private Label loginStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        title.setText("Witaj w [wstawic nazwe naszego serwisu]!\nZaloguj sie lub stworz nowe konto, aby uzyskac dostep.");
        loginLabel.setText("Login:");
        passwordLabel.setText("Haslo:");
        loginButton.setText("Zaloguj sie");
        registerLabel.setText("Nie masz jeszcze konta?");
        registerButton.setText("Zarejestruj sie");
    }

    public void loginPressed(ActionEvent actionEvent) throws IOException {
        login();
    }

    //TODO make it actually log you in
    private void login() throws IOException {
        loginStatus.setText("");
        loginStatus.setTextFill(Paint.valueOf("black"));

        String login = loginTextField.getText();
        String password = passwordTextField.getText();
        System.out.println("Login: " + login + "\nPassword: " + password);

        DatabaseHolder databaseHolder = DatabaseHolder.getInstance();
        try(Session session = databaseHolder.getSession()) {
            session.beginTransaction();
            Query query = session.createQuery("select AU from ApplicationUser AU where AU.login LIKE :userLogin").setParameter("userLogin", login);
            @SuppressWarnings("unchecked") List<ApplicationUser> resultList = query.getResultList();
            if(resultList.size() == 0) {
                System.out.println("Nie ma takiego uzytkownika.");
                session.getTransaction().rollback();
                loginStatus.setText("Logowanie nieudane - nie ma takiego uzytkownika");
                loginStatus.setTextFill(Paint.valueOf("red"));
                return;
            }
            else {
                ApplicationUser user = resultList.get(0);
                String expectedPassword = user.getPassword();
                String salt = user.getSalt();
                if(PasswordUtils.verifyPassword(password, expectedPassword, salt)) {
                    System.out.println("Dane poprawne. Mozna zalogowac.");
                    UserHolder userHolder = UserHolder.getInstance();
                    userHolder.setUser(user);
                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/some_scene.fxml"));
                    loginButton.getScene().setRoot(root);
                }
                else {
                    loginStatus.setText("Dane niepoprawne.");
                    loginStatus.setTextFill(Paint.valueOf("red"));
                }
            }
            session.getTransaction().commit();
        }
    }

    public void register(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/register_screen.fxml"));
        registerButton.getScene().setRoot(root);
    }

    public void keyPressed(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            login();
        }
    }
}
