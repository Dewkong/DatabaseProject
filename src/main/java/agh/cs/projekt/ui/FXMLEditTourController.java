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
import org.hibernate.Transaction;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class FXMLEditTourController implements Initializable {
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
    private Button editTourButton;
    @FXML
    private Text editTourStatus;

    private ToggleGroup imageSourceToggleGroup;
    private FileChooser imageChooser;
    private Tour tour;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        returnButton.setText("Powrot");
        title.setText("Edytuj szczegóły wycieczki");
        nameLabel.setText("Nazwa wycieczki");
        countryLabel.setText("Kraj");
        for(CountryEnum country : CountryEnum.values()) {
            countryChoiceBox.getItems().add(country);
        }
        dateLabel.setText("Data");
        maxPlacesLabel.setText("Maksymalna ilosc miejsc");
        priceLabel.setText("Cena");
        descriptionLabel.setText("Opis");
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
        editTourButton.setText("Zapisz zmiany");
        Platform.runLater(() -> nameTextField.requestFocus());
    }

    public void loadTour(Tour tour) {
        this.tour = tour;
        nameTextField.setText(tour.getName());
        countryChoiceBox.setValue(tour.getCountry());
        datePicker.setValue(tour.getTourDate().toLocalDate());
        maxPlacesTextField.setText(Integer.toString(tour.getMaxPlaces()));
        priceTextField.setText(Float.toString(tour.getPrice()));
        descriptionTextArea.setText(tour.getDescription());
        if(tour.getImage() instanceof HttpImageSource) {
            imageSourceToggleGroup.selectToggle(httpImageRadioButton);
            try {
                imageTextField.setText(tour.getImage().getName());
            } catch (IOException e) {
                imageTextField.setText("");
            }
        }
    }

    public void goBack(ActionEvent actionEvent) {
        NavigationService.getInstance().goBack();
    }


    public void editTourPressed(ActionEvent actionEvent) {
        editTour();
    }

    public void editTour() {
        System.out.println("Zapisywanie zmian...");
        editTourStatus.setText("");
        editTourStatus.setFill(Paint.valueOf("black"));

        String invalidData = getInvalidData();
        if(!invalidData.equals("")) {
            editTourStatus.setText("Dane niepoprawne:\n" + invalidData);
            editTourStatus.setFill(Paint.valueOf("red"));
            return;
        }

        try(Session session = DatabaseHolder.getInstance().getSession()) {
            Transaction transaction = session.beginTransaction();
            tour.setName(nameTextField.getText());
            tour.setCountry(countryChoiceBox.getValue());
            tour.setTourDate(Date.valueOf(datePicker.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            tour.setMaxPlaces(Integer.parseInt(maxPlacesTextField.getText()));
            tour.setPrice(Float.parseFloat(priceTextField.getText()));
            tour.setDescription(descriptionTextArea.getText());

            String imagePath = imageTextField.getText();
            ImageSource imageSource;

            if(imageSourceToggleGroup.getSelectedToggle() != null && !imagePath.equals("")) {
                if(imageSourceToggleGroup.getSelectedToggle().getUserData().equals(ImageSourceEnum.LOCAL)) {
                    imageSource = new LocalImageSource(imagePath);
                    tour.setImage(imageSource);
                }
                else if (imageSourceToggleGroup.getSelectedToggle().getUserData().equals(ImageSourceEnum.HTTP)) {
                    imageSource = new HttpImageSource(imagePath);
                    tour.setImage(imageSource);
                }
            }
            System.out.println(tour);
            session.update(tour);
            transaction.commit();
        }
        editTourStatus.setText("Zmiany zapisane");
        editTourStatus.setFill(Paint.valueOf("green"));
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
            editTour();
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
            try {
                imageTextField.setText(tour.getImage().getName());
            } catch (IOException e) {
                imageTextField.setText("");
            }
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
