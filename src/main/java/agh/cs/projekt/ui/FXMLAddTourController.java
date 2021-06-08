package agh.cs.projekt.ui;

import agh.cs.projekt.models.CountryEnum;
import agh.cs.projekt.models.ImageSource.HttpImageSource;
import agh.cs.projekt.models.ImageSource.ImageSource;
import agh.cs.projekt.models.ImageSource.ImageSourceEnum;
import agh.cs.projekt.models.ImageSource.LocalImageSource;
import agh.cs.projekt.models.Tour;
import agh.cs.projekt.services.DatabaseHolder;
import agh.cs.projekt.services.NavigationService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.hibernate.Session;
import javax.persistence.Query;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLAddTourController implements Initializable {
    @FXML
    private Button returnButton;
    @FXML
    private Label title;
    @FXML
    private Label nameLabel;
    @FXML
    private TextField nameTextField;
    @FXML
    private Label countryLabel;
    @FXML
    private ChoiceBox<CountryEnum> countryChoiceBox;
    @FXML
    private Label dateLabel;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label maxPlacesLabel;
    @FXML
    private TextField maxPlacesTextField;
    @FXML
    private Label priceLabel;
    @FXML
    private TextField priceTextField;
    @FXML
    private Label descriptionLabel;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Label imageLabel;
    @FXML
    private RadioButton httpImageRadioButton;
    @FXML
    private RadioButton localImageRadioButton;
    @FXML
    private TextField imageTextField;
    @FXML
    private Button imageBrowseButton;
    @FXML
    private Button addTourButton;
    @FXML
    private Text addTourStatus;

    private ToggleGroup imageSourceToggleGroup;
    private FileChooser imageChooser;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        returnButton.setText("Powrot");
        title.setText("Dodaj nowa wycieczke");
        nameLabel.setText("Nazwa wycieczki");
        countryLabel.setText("Kraj");
        for(CountryEnum country : CountryEnum.values()) {
            countryChoiceBox.getItems().add(country);
        }
        dateLabel.setText("Data");
        datePicker.setValue(LocalDate.now().plusDays(1));
        maxPlacesLabel.setText("Maksymalna ilosc miejsc");
        maxPlacesTextField.setText("0");
        priceLabel.setText("Cena");
        priceTextField.setText("0");
        descriptionLabel.setText("Opis");
        descriptionTextArea.setText("");
        imageLabel.setText("Zdjecie pogladowe");
        imageSourceToggleGroup = new ToggleGroup();
        httpImageRadioButton.setToggleGroup(imageSourceToggleGroup);
        httpImageRadioButton.setText("Zdjecie z internetu");
        httpImageRadioButton.setUserData(ImageSourceEnum.HTTP);
        localImageRadioButton.setToggleGroup(imageSourceToggleGroup);
        localImageRadioButton.setText("Zdjecie z komputera");
        localImageRadioButton.setUserData(ImageSourceEnum.LOCAL);
        imageBrowseButton.setText("Przegladaj zdjecia...");
        imageBrowseButton.setDisable(true);
        imageChooser = new FileChooser();
        imageChooser.setTitle("Wybierz zdjecie");
        imageChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("image", "*.jpg"));
        addTourButton.setText("Dodaj wycieczke");
        Platform.runLater(() -> nameTextField.requestFocus());
    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        NavigationService.getInstance().goBack();
    }


    public void addTourPressed(ActionEvent actionEvent) {
        addTour();
    }

    public void addTour() {
        System.out.println("Dodawanie wycieczki...");
        addTourStatus.setText("");
        addTourStatus.setFill(Paint.valueOf("black"));

        String invalidData = getInvalidData();
        if(!invalidData.equals("")) {
            addTourStatus.setText("Dane niepoprawne:\n" + invalidData);
            addTourStatus.setFill(Paint.valueOf("red"));
            return;
        }

        String name = nameTextField.getText();
        CountryEnum country = countryChoiceBox.getValue();
        Date date = Date.valueOf(datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        int maxPlaces = Integer.parseInt(maxPlacesTextField.getText());
        float price = Float.parseFloat(priceTextField.getText());
        String description = descriptionTextArea.getText();
        String imagePath = imageTextField.getText();
        ImageSource imageSource;
        System.out.println(imageTextField.getText());
        if(imageSourceToggleGroup.getSelectedToggle() == null) {
            imageSource = new HttpImageSource("https://picsum.photos/1600/900");
        }
        else if(imageSourceToggleGroup.getSelectedToggle().getUserData().equals(ImageSourceEnum.LOCAL)) {
            imageSource = new LocalImageSource(imagePath);
        }
        else if (imageSourceToggleGroup.getSelectedToggle().getUserData().equals(ImageSourceEnum.HTTP)) {
            imageSource = new HttpImageSource(imagePath);
        }
        else {
            imageSource = new HttpImageSource("https://picsum.photos/1600/900");
        }


        Tour newTour = new Tour(name, country, date, maxPlaces, price, description, imageSource);
        System.out.println(newTour);
        try {
            System.out.println(newTour.getImage().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        DatabaseHolder databaseHolder = DatabaseHolder.getInstance();
        try(Session session = databaseHolder.getSession()) {
            session.beginTransaction();
            Query query = session.createQuery("select T from Tour T where T.id = :tourID").setParameter("tourID", newTour.getId());
            @SuppressWarnings("unchecked") List<Tour> resultList = query.getResultList();
            if (resultList.size() == 0) {
                session.save(newTour);
                session.getTransaction().commit();
                addTourStatus.setText("Wycieczka dodana");
                addTourStatus.setFill(Paint.valueOf("green"));
            }
            else {
                session.getTransaction().rollback();
                addTourStatus.setText("Dodanie wycieczki nieudane - wycieczka o tym ID juz istnieje");
                addTourStatus.setFill(Paint.valueOf("red"));
            }
        }
    }

    private String getInvalidData() {
        StringBuilder invalidData = new StringBuilder();
        int invalidCount = 0;

        List<List<String>> dataToCheck = new ArrayList<>();
        dataToCheck.add(Arrays.asList(nameTextField.getText(), "^[a-zA-Z0-9][a-zA-Z0-9 ]*[a-zA-Z0-9]$", "nazwa"));
        dataToCheck.add(Arrays.asList(datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), "^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}$", "data"));
        dataToCheck.add(Arrays.asList(maxPlacesTextField.getText(), "^[\\d]*$", "ilosc miejsc"));
        dataToCheck.add(Arrays.asList(priceTextField.getText(), "^[\\d]*(\\.[\\d]{0,2}){0,1}$", "cena"));
        dataToCheck.add(Arrays.asList(descriptionTextArea.getText(), "^[\\w\\dęĘóÓąĄśŚłŁżŻźŹćĆńŃ .,'()!@#$%^&*+/-]+$", "opis"));

        for(List<String> data : dataToCheck) {
            if(!data.get(0).matches(data.get(1))) {
                if(invalidCount >= 1) {
                    invalidData.append(", ");
                }
                invalidData.append(data.get(2));
                invalidCount += 1;
            }
        }

        if (invalidCount > 0) return invalidData.toString();

        if(countryChoiceBox.getValue() == null) {
            return "Nie wybrano kraju";
        }

        Date date = Date.valueOf(datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if(date.before(new Date(System.currentTimeMillis()))) {
            return "Zbyt wczesna data";
        }

        if(Integer.parseInt(maxPlacesTextField.getText()) < 1) {
            return "Zbyt mala ilosc miejsc";
        }

        if(Integer.parseInt(maxPlacesTextField.getText()) > 999) {
            return "Zbyt duza ilosc miejsc";
        }

        if(Float.parseFloat(priceTextField.getText()) < 1) {
            return "Cena zbyt niska";
        }

        if(Float.parseFloat(priceTextField.getText()) > 1000000) {
            return "Cena zbyt wysoka";
        }

        return "";
    }

    public void keyPressedOnTextField(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            addTour();
        }
    }

    public void keyPressedOnChoice(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            countryChoiceBox.show();
        }
    }

    public void keyPressedOnDate(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER)) {
            datePicker.show();
        }
    }

    public void imageSourceChosen(ActionEvent actionEvent) {
        if(imageSourceToggleGroup.getSelectedToggle().getUserData().equals(ImageSourceEnum.LOCAL)) {
            imageBrowseButton.setDisable(false);
            imageTextField.setText("");
        }
        else if (imageSourceToggleGroup.getSelectedToggle().getUserData().equals(ImageSourceEnum.HTTP)) {
            imageBrowseButton.setDisable(true);
            imageTextField.setText("");
        }
    }

    public void browsePressed(ActionEvent actionEvent) {
        Window stage = imageBrowseButton.getScene().getWindow();

        try {
            File file = imageChooser.showOpenDialog(stage);
            imageChooser.setInitialDirectory(file.getParentFile());
            imageTextField.setText(file.getAbsolutePath());
        }
        catch (NullPointerException ignored) {
            // NullPointerExceptions is thrown if the FileChooser is closed with no file chosen
            // there is nothing special to do in that case
            // as such, the exception needs to be caught, but can be ignored
        }
    }

}
