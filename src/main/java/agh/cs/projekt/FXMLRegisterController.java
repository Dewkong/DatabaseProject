package agh.cs.projekt;

import agh.cs.projekt.models.Customer;
import agh.cs.projekt.models.RoleEnum;
import agh.cs.projekt.models.ApplicationUser;
import agh.cs.projekt.utils.PasswordUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.hibernate.Session;

import javax.persistence.Query;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLRegisterController implements Initializable {
    @FXML
    private Button returnButton;
    @FXML
    private Label title;
    @FXML
    private Label loginLabel;
    @FXML
    private TextField loginTextField;
    @FXML
    private Label passwordLabel;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Label roleLabel;
    @FXML
    private ChoiceBox<RoleEnum> roleChoiceBox;
    @FXML
    private Label nameLabel;
    @FXML
    private TextField nameTextField;
    @FXML
    private Label surnameLabel;
    @FXML
    private TextField surnameTextField;
    @FXML
    private Label phoneLabel;
    @FXML
    private TextField phoneTextField;
    @FXML
    private Label emailLabel;
    @FXML
    private TextField emailTextField;
    @FXML
    private Button registerButton;
    @FXML
    private Text registrationStatus;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        returnButton.setText("Powrot");
        title.setText("Stworz nowe konto");
        loginLabel.setText("Login:");
        passwordLabel.setText("Haslo:");
        roleLabel.setText("Rodzaj konta:");
        for(RoleEnum role : RoleEnum.values()) {
            roleChoiceBox.getItems().add(role);
        }
        nameLabel.setText("Imie:");
        surnameLabel.setText("Nazwisko:");
        phoneLabel.setText("Numer telefonu:");
        emailLabel.setText("Adres e-mail:");
        registerButton.setText("Utworz konto");
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login_screen.fxml"));
        returnButton.getScene().setRoot(root);
    }

    public void registerPressed(ActionEvent actionEvent) {
        register();
    }

    public void register() {
        registrationStatus.setText("");
        registrationStatus.setFill(Paint.valueOf("black"));

        String name = nameTextField.getText();
        String surname = surnameTextField.getText();
        String phone = phoneTextField.getText();
        String email = emailTextField.getText();
        Customer newCustomer = new Customer(name, surname, phone, email);
        String login = loginTextField.getText();
        String salt = PasswordUtils.getSalt(20);
        String password = PasswordUtils.encryptPassword(passwordTextField.getText(), salt);
        RoleEnum role = roleChoiceBox.getValue();
        ApplicationUser newApplicationUser = new ApplicationUser(newCustomer, login, password, salt, role);

        String invalidData = getInvalidData(newCustomer, newApplicationUser);
        if(!invalidData.equals("")) {
            registrationStatus.setText("Dane niepoprawne:\n" + invalidData);
            registrationStatus.setFill(Paint.valueOf("red"));
        }
        else {
            DatabaseHolder databaseHolder = DatabaseHolder.getInstance();
            try(Session session = databaseHolder.getSession()) {
                session.beginTransaction();
                Query query = session.createQuery("select AU from ApplicationUser AU where AU.login LIKE :userLogin").setParameter("userLogin", login);
                @SuppressWarnings("unchecked") List<ApplicationUser> resultList = query.getResultList();
                if(resultList.size() == 0) {
                    session.save(newCustomer);
                    session.save(newApplicationUser);
                }
                else {
                    session.getTransaction().rollback();
                    registrationStatus.setText("Rejestracja nieudana - uzytkownik o tym loginie juz istnieje.");
                    registrationStatus.setFill(Paint.valueOf("red"));
                    return;
                }
                session.getTransaction().commit();
            }
        }
    }

    private String getInvalidData(Customer customer, ApplicationUser user) {
        String invalidData = "";
        int invalidCount = 0;

        List<List<String>> dataToCheck = new ArrayList<>();
        dataToCheck.add(Arrays.asList(customer.getName(), "^[A-Z][a-zA-Z]*$", "imie"));
        dataToCheck.add(Arrays.asList(customer.getSurname(), "^[A-Z][a-zA-Z]*$", "nazwisko"));
        dataToCheck.add(Arrays.asList(customer.getPhoneNumber(), "^[0-9]{9}$", "numer telefonu"));
        dataToCheck.add(Arrays.asList(customer.getEmail(), "^\\w+@\\w+(\\.\\w+)+$", "adres e-mail"));
        dataToCheck.add(Arrays.asList(user.getLogin(), "^\\w+$", "login"));

        for(List<String> data : dataToCheck) {
            if(!data.get(0).matches(data.get(1))) {
                if(invalidCount >= 1) {
                    invalidData += ", ";
                }
                invalidData += data.get(2);
                invalidCount += 1;
            }
        }

        return invalidData;
    }

    public void keyPressedOnTextField(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            register();
        }
    }

    public void keyPressedOnChoice(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            roleChoiceBox.show();
        }
    }
}
