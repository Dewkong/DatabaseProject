package agh.cs.projekt.ui;

import agh.cs.projekt.models.Tour;
import agh.cs.projekt.utils.ImageController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class FXMLTourDetailsController implements Initializable {

    @FXML
    public Label tour_name;
    @FXML
    public ImageView image_view;
    @FXML
    public Label tour_country;
    @FXML
    public Label tour_description;
    @FXML
    public Label tour_date;
    @FXML
    public Label tour_price;
    @FXML
    public Label tour_places;
    @FXML
    public Button return_button;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void displayTour(Tour tour){
        if (tour == null){
            System.err.println("Trying to display a null tour!");
            //TODO revert to previous screen ~W
        }

        ImageController.loadFromSource(image_view, tour.getImage());
        tour_name.setText(tour.getName());
        tour_country.setText(tour.getCountry().toString());
        tour_description.setText(tour.getDescription());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        tour_date.setText(sdf.format(tour.getTourDate()));
        tour_price.setText(String.format("%.2f", tour.getPrice()) + "z\u0142"); // \u0142 - unicode for ł
        tour_places.setText("\u0141adowanie..."); // \u0141 - unicode for uppercase ł
        new Thread(() -> {
            //fetch places
            int availablePlaces = tour.getAvailablePlaces();
            Platform.runLater(() -> { //run on FX thread
                if (availablePlaces < 0){
                    tour_places.setText("B\u0142ąd"); // \u0142 - unicode for ł
                } else if (availablePlaces == 0){
                    tour_places.setText("Brak wolnych miejsc");
                } else {
                    tour_places.setText(availablePlaces + "/" + tour.getMaxPlaces());
                }
            });
        }).start();

    }

    public void goBack(ActionEvent actionEvent) throws IOException {
        //TODO this should not redirect to login, just a placeholder for now ~W
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login_screen.fxml"));
        return_button.getScene().setRoot(root);
    }

}
